package com.sunland.cpocr.xlw;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// USB管理模块
public class USBUtil {
    private static final String TAG = "USBUtil";
    private static final String ACTION_USB_PERMISSION = "com.OBC.USB_PERMISSION";
    private static final int ID_VENDOR = 829;
    private static final int ID_PRODUCT = 21226;
    private static final int INTERFACE_ZERO = 0;
    private static final int OUT_ENDPOINT = 0;
    private static final int IN_ENDPOINT = 1;

    // 单例模式
    private static USBUtil m_Instance = null;

    private Activity m_Activity;  // 上下文
    private static UsbManager m_UsbManager; // usb管理器
    private static PendingIntent m_PermissionIntent; // 权限意图
    private static UsbInterface m_UsbInterface;    // 连接接口
    private static UsbEndpoint m_UsbEndpointIn;    // 入口点
    private static UsbEndpoint m_UsbEndpointOut;   // 出口点
    private static UsbDeviceConnection m_UsbDeviceConnection;  // 连接管理

    private byte[] m_ReadData = null;  // 读取的数据汇总

    private boolean m_bExit = false;
    private Thread m_ReaderThread = null;  // 数据接收线程(usb半双工，所以发送一起)

    private List<byte[]> m_SendDataList = new ArrayList<byte[]>();  // 发送数据队列
    private static ReentrantLock m_Mutex = new ReentrantLock();  // 队列锁

    // 对外接口::单例模式获取
    public static USBUtil getInstance() {
        if (m_Instance == null) {
            synchronized (USBUtil.class) {
                if (m_Instance == null) {
                    m_Instance = new USBUtil();
                }
            }
        }
        return m_Instance;
    }

    // 对外接口::打开USB
    public int openUSB(@NotNull Context ctx) {
        m_Activity = (Activity) ctx;

        // 获取USB管理器
        m_UsbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        if (m_UsbManager == null) {
            Log.e(TAG, "[USB]USB管理器未初始化成功");
            Toast.makeText(ctx, "USB管理器未初始化成功", Toast.LENGTH_SHORT).show();
            return -1;
        }

        // 初始化权限设定
        registerReceiver(m_Activity);

        // 获取设备列表
        getDeviceList();

        // 获取对应设备
        //UsbDevice m_UsbDevice = getSWDeviceList(7531, 260); // test U盘2
        UsbDevice m_UsbDevice = getSWDeviceList(ID_VENDOR, ID_PRODUCT);
        if (m_UsbDevice == null) {
            return -1;
        }

        // 判断使用权限
        requestPermission(m_UsbDevice);

        // 打开USB数据连接
        if (!OpenPort(m_UsbDevice)) {
            return -1;
        }

        // 启动数据读取线程
        m_bExit = false;
        m_ReaderThread = new Thread(new UsbReader());
        m_ReaderThread.start();

        return 0;
    }

    // 对外接口::关闭设备监听
    public int closeUSB() {
        m_bExit = true;
        ClosePort(1000);
        unRegisterReceiver(m_Activity);

        while (m_ReaderThread.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        m_Mutex.lock();
        m_SendDataList.clear();
        m_Mutex.unlock();

        return 0;
    }

    // 对外接口::获取所有设备列表
    public List<UsbDevice> getDeviceList() {
        HashMap<String, UsbDevice> deviceList = m_UsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        List<UsbDevice> usbDevices = new ArrayList<>();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            usbDevices.add(device);

            String strLog = "设备名：" + device.getDeviceName()
                    + "\t\t制造商：" + device.getManufacturerName()
                    + "\t\t产品名：" + device.getProductName()
                    + "\n类型：" + device.getDeviceClass()
                    + "\t\t子类型：" + device.getDeviceSubclass()
                    + "\t\tID：" + device.getDeviceId()
                    + "\t\t协议：" + device.getDeviceProtocol()
                    + "\n序列号：" + device.getSerialNumber()
                    + "\n厂家ID(V_ID):" + device.getVendorId()
                    + "\t\t产品ID(P_ID)：" + device.getProductId()
                    + "\t\t是否拥有权限：" + hasPermission(device);
            Toast.makeText(m_Activity, strLog, Toast.LENGTH_SHORT).show();
            Log.i(TAG, strLog);
        }

        Toast.makeText(m_Activity, "USB设备数量: " + usbDevices.size(), Toast.LENGTH_SHORT).show();
        Log.i(TAG, "[USB]USB设备数量:"+ usbDevices.size());
        return usbDevices;
    }

    // 获取信路威无线转发设备
    @Nullable
    private UsbDevice getSWDeviceList(int vendorId, int productId) {
        HashMap<String, UsbDevice> deviceList = m_UsbManager.getDeviceList();

        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == vendorId && device.getProductId() == productId) {
                String strLog = "设备名：" + device.getDeviceName()
                        + "\t\t制造商：" + device.getManufacturerName()
                        + "\t\t产品名：" + device.getProductName()
                        + "\n类型：" + device.getDeviceClass()
                        + "\t\t子类型：" + device.getDeviceSubclass()
                        + "\t\tID：" + device.getDeviceId()
                        + "\t\t协议：" + device.getDeviceProtocol()
                        + "\n序列号：" + device.getSerialNumber()
                        + "\n厂家ID(V_ID):" + device.getVendorId()
                        + "\t\t产品ID(P_ID)：" + device.getProductId()
                        + "\t\t是否拥有权限：" + hasPermission(device);
                Toast.makeText(m_Activity, "找到对应设备:\n" + strLog, Toast.LENGTH_LONG).show();
                Log.i(TAG, strLog);
                return device;
            }
        }

        Toast.makeText(m_Activity, "未找到对应设备", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "[USB]未找到对应设备");
        return null;
    }

    // 判断是否有权限
    private boolean hasPermission(UsbDevice device) {
        return m_UsbManager.hasPermission(device);
    }

    // 请求权限
    private void requestPermission(UsbDevice device) {
        if (device != null) {
            if (m_UsbManager.hasPermission(device)) {
                Toast.makeText(m_Activity, "已经获取到权限", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "已经获取到权限");
            } else {
                if (m_PermissionIntent != null) {
                    m_UsbManager.requestPermission(device, m_PermissionIntent);
                    Toast.makeText(m_Activity, "请求USB权限", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "请求USB权限");
                } else {
                    Toast.makeText(m_Activity, "请注册USB广播", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "请注册USB广播");
                }
            }
        }
    }

    // 权限广播
    private final BroadcastReceiver m_UsbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NotNull Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                // 获取权限结果的广播
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        // 获取设备权限
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            Log.e(TAG, "[USB]获取权限成功：" + device.getDeviceName());
                            Toast.makeText(m_Activity, "获取权限成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "[USB]获取权限失败：" + device.getDeviceName());
                            Toast.makeText(m_Activity, "获取权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                // 有新的设备插入，可以在这里做自动open设备的初始化
                USBUtil.getInstance().openUSB(USBUtil.getInstance().m_Activity);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // 有设备拔出了
                USBUtil.getInstance().closeUSB();
            }

        }
    };

    // 注册权限广播
    private void registerReceiver(Activity context) {
        m_PermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        context.registerReceiver(m_UsbPermissionReceiver, filter);
    }

    // 注销权限广播
    private void unRegisterReceiver(@NotNull Activity context) {
        context.unregisterReceiver(m_UsbPermissionReceiver);
        m_PermissionIntent = null;
    }

    // 打开USB数据连接
    private boolean OpenPort(@NotNull UsbDevice device) {
        // 获取设备接口，一般取第一个0
        m_UsbInterface = device.getInterface(INTERFACE_ZERO);
//        m_UsbInterface = device.getInterface(1);
        Log.i(TAG, device.getInterfaceCount() + ":" + m_UsbInterface.getId() + ":" + m_UsbInterface.toString());
        Log.i(TAG, "接口名称：" + m_UsbInterface.getName());
        Log.i(TAG, "接口类型：" + m_UsbInterface.getInterfaceClass());
        Log.i(TAG, "接口子类型：" + m_UsbInterface.getInterfaceSubclass());
        Log.i(TAG, "接口协议：" + m_UsbInterface.getInterfaceProtocol());

        // 判断是否有权限
        if (hasPermission(device)) {
            // 打开设备，获取 UsbDeviceConnection 对象，连接设备，用于后面的通讯
            m_UsbDeviceConnection = m_UsbManager.openDevice(device);

            if (m_UsbDeviceConnection == null) {
                Log.e(TAG, "设备未打开");
                return false;
            }
            if (m_UsbDeviceConnection.claimInterface(m_UsbInterface, true)) {
                Toast.makeText(m_Activity, "找到USB设备接口", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "[USB]找到USB设备接口");
            } else {
                m_UsbDeviceConnection.close();
                Toast.makeText(m_Activity, "未找到USB设备接口", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "[USB]未找到USB设备接口");
                return false;
            }
        } else {
            Toast.makeText(m_Activity, "没有USB权限", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "[USB]没有USB权限");
            return false;
        }

        // 获取接口上的两个端点，分别对应 OUT 和 IN
        for (int i = 0; i < m_UsbInterface.getEndpointCount(); ++i) {
            UsbEndpoint end = m_UsbInterface.getEndpoint(i);
            if (end.getDirection() == UsbConstants.USB_DIR_IN) {
                m_UsbEndpointIn = end;
            } else {
                m_UsbEndpointOut = end;
            }
        }
        return true;
    }

    // 关闭USB数据连接
    private void ClosePort(int timeout) {
        if (m_UsbDeviceConnection == null) {
            return;
        }
        try {
            Thread.sleep((long) timeout);
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }
        try {
            m_UsbDeviceConnection.close();
            m_UsbDeviceConnection.releaseInterface(m_UsbInterface);
            m_UsbDeviceConnection = null;
            m_UsbEndpointIn = null;
            m_UsbEndpointOut = null;
            m_UsbManager = null;
            m_UsbInterface = null;
            Log.d(TAG, "[USB]Device closed!");
        } catch (Exception var3) {
            Log.e(TAG, "[USB]Exception: " + var3.getMessage());
        }
    }

    // 数据读取线程
    private class UsbReader implements Runnable {
        @Override
        public void run() {
            // 同步通信, Host to Device:Out, Device to Host:In
            byte[] buffer = new byte[1024 * 1024];
            while (!m_bExit) {
                // 发送数据
                if (!m_SendDataList.isEmpty()) {
                    m_Mutex.lock();
                    byte[] tempSendData = m_SendDataList.remove(0);
                    m_Mutex.unlock();
                    sendMessage(tempSendData);
                }

                // 接收数据
                int iLen = m_UsbDeviceConnection.bulkTransfer(
                        m_UsbEndpointIn, buffer, 16384, 1000); // android P版本前限制每次最大只能传输16K 16384，P后才能任意
                if (iLen > 0) {
                    Log.i(TAG, String.format("[USB]接收到数据:%d", iLen));

                    // 判断数据头
                    if (iLen == 10) {
                        byte[] temp = new byte[10];
                        System.arraycopy(buffer, 0, temp, 0, 10);
                        String strHeadTag = new String(temp);

                        if (strHeadTag.equals("SignalwayS")) {
                            Log.i(TAG, strHeadTag);

                            // 数据start，清空准备开始接收
                            m_ReadData = null;
                            continue;
                        }
                        else if (strHeadTag.equals("SignalwayE")) {
                            Log.i(TAG, strHeadTag);

                            // 数据End，回调出数据
                            if (m_ReaderCallBack != null && m_ReadData != null) {
                                m_ReaderCallBack.getData(m_ReadData);

                                // 发送测试数据
//                                String strTemp = "ABCD";
//                                byte[] temp2 = strTemp.getBytes();
//                                sendMessage(temp2);
                            }

                            // 清空重新开始
                            m_ReadData = null;
                            continue;
                        }
                    }
                    else if (iLen == 4) {
                        continue;
                    }

                    int iOldLen = (m_ReadData == null) ? 0 : m_ReadData.length;
                    // 数据量异常判断
                    if (iOldLen > (1024 << 16)) {
                        // 清空重新开始
                        m_ReadData = null;
                        continue;
                    }

                    // 拼装数据
                    byte[] appendData = new byte[iOldLen + iLen];
                    if (m_ReadData != null) {
                        System.arraycopy(m_ReadData, 0, appendData, 0, iOldLen);
                    }
                    System.arraycopy(buffer, 0, appendData, iOldLen, iLen);
                    m_ReadData = appendData;

                } else {
                    Log.e(TAG, "[USB]未接收到数据:" + iLen);

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // async, need API level >= 26
//            UsbRequest request = new UsbRequest();
//            request.initialize(mUsbDeviceConnection, mUsbEndpoint);
//            ByteBuffer buffer = ByteBuffer.allocate(mUsbEndpoint.getMaxPacketSize());
//            while (!mStopTransfer) {
//                request.queue(buffer);
//                try {
//                    UsbRequest result = mUsbDeviceConnection.requestWait(1000);
//                    if (result != request || mUsbEndpoint != request.getEndpoint())
//                        continue;
//                    Log.i(TAG, String.format("get %d bytes data",
//                            buffer.limit()-buffer.position()));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }

        }
    } // end 数据读取线程

    // 对外接口::数据发送
    public int addSendData(byte[] bytes) {
        m_Mutex.lock();
        if (m_SendDataList.size() > 20) {
            m_SendDataList.remove(0);
        }
        m_SendDataList.add(bytes);
        m_Mutex.unlock();

        return 0;
    }

    // 数据发送
    private int sendMessage(byte[] bytes) {
        if (m_UsbDeviceConnection == null || null == m_UsbEndpointOut) {
            return -1;
        }

        // 发送头
        String strTemp = "SignalwayS";
        byte[] temp = strTemp.getBytes();
        int iRet = m_UsbDeviceConnection.bulkTransfer(m_UsbEndpointOut, temp, temp.length, 200);
        Log.i(TAG, String.format("[USB]发送数据:%d", iRet));
        if (iRet <= 0) {
            return iRet;
        }

        // 发送数据大小，4字节
        temp = intToByteArray(bytes.length);
        iRet = m_UsbDeviceConnection.bulkTransfer(m_UsbEndpointOut, temp, temp.length, 200);
        Log.i(TAG, String.format("[USB]发送数据:%d", iRet));
        if (iRet <= 0) {
            return iRet;
        }

        // 发送实际数据
        int iRetLen = m_UsbDeviceConnection.bulkTransfer(m_UsbEndpointOut, bytes, bytes.length, 500);
        Log.i(TAG, String.format("[USB]发送数据:%d", iRetLen));
        if (iRetLen <= 0) {
            return iRetLen;
        }

        // 发送结束
        strTemp = "SignalwayE";
        temp = strTemp.getBytes();
        iRet = m_UsbDeviceConnection.bulkTransfer(m_UsbEndpointOut, temp, temp.length, 200);
        Log.i(TAG, String.format("[USB]发送数据:%d", iRet));
        if (iRet <= 0) {
            return iRet;
        }

        // 返回实际发送数据
        return iRetLen;
    }

    // 定义数据对外输出回调接口
    public static interface CallBack {
        public void getData(byte[] data);
    }
    public static CallBack m_ReaderCallBack = null; // 回调接口

    // int转byte[]
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)(i & 0xff);
        result[1] = (byte)((i >> 8) & 0xFF);
        result[2] = (byte)((i >> 16) & 0xFF);
        result[3] = (byte)((i >> 24) & 0xFF);
        return result;
    }

}