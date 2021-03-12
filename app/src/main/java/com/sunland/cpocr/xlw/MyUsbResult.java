package com.sunland.cpocr.xlw;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// 结果接收模块
public class MyUsbResult {

    // 数据类型
    public class CAMERA_INFO_TYPE
    {
        public static final int CAMERA_THROB = 0xFFFF0001;            // 心跳包
        public static final int CAMERA_THROB_RESPONSE = 0xFFFF0002;   // 心跳回包
        public static final int CAMERA_IMAGE = 0xF0000001;
        public static final int CAMERA_VIDEO = 0xF0000002;            // H.264视频帧包
        public static final int CAMERA_AUDIO = 0xF0000003;
        public static final int CAMERA_RECORD = 0xF0010001;           // 结果包
        public static final int CAMERA_STRING = 0xF0010002;           // 附加信息包
        public static final int CAMERA_STATE = 0xF0020001;
    }

    private static final int MAX_RESULT = 20;     // 结果队列数量
    private static final int MAX_VIDEO = 100;     // 视频帧队列数量

    public static Activity m_Activity;  // 上下文
    private static USBUtil m_MyUSB = USBUtil.getInstance();  // USB设备管理

    private static List<byte[]> m_ResultList = new ArrayList<byte[]>();  // 结果队列
    private static ReentrantLock m_Mutex = new ReentrantLock();  // 队列锁

    private static List<byte[]> m_VideoList = new ArrayList<byte[]>();  // 视频帧队列
    private static ReentrantLock m_VideoMutex = new ReentrantLock();  // 队列锁

    // 接收结果数据结构
    private static class Result {
        public int iType = 0;
        public int iDataLen = 0;
        public int iXmlLen = 0;
        public int iImageDataLen = 0;
        public byte[] data = null;
    }
    private static Result m_resultData = new Result();   // 结果数据

    // 对外接口::打开设备
    public static int OpenDevice(Context ctx) {
        m_Activity = (Activity)ctx;

        int iRet = -1;

        // 打开USB连接
        iRet = m_MyUSB.openUSB(m_Activity);
        if (iRet != 0) {
            Log.i("USB", "初始化USB失败!");
        }

        // 实现数据获取回调接口
        m_MyUSB.m_ReaderCallBack = new USBUtil.CallBack() {
            @Override
            public void getData(byte[] data) {
                // 如果是个新结果
                int iRet = analyseHead(data);
                if (iRet != 0) {
                    return ;
                }

                // 添加数据
                switch (m_resultData.iType) {
                    case CAMERA_INFO_TYPE.CAMERA_VIDEO:
                        AddVideoList(data);
                        break;
                    default:
                        AddResultList(data);
                        break;
                }

            }
        };

        return 0;
    }

    // 对外接口::关闭设备
    public static int CloseDevice() {
        m_MyUSB.m_ReaderCallBack = null;
        return m_MyUSB.closeUSB();
    }

    // 对外接口::获取当前结果数量
    public static int GetListCount() {
        return m_ResultList.size();
    }

    // 对外接口::获取队列的最前结果
    public static byte[] GetOneResult() {
        byte[] tempResultData = null;

        m_Mutex.lock();
        if (!m_ResultList.isEmpty()) {
            tempResultData = m_ResultList.remove(0);
        }
        m_Mutex.unlock();

        return tempResultData;
    }

    // 对外接口::获取当前视频帧数量
    public static int GetVideoListCount() {
        return m_VideoList.size();
    }

    // 对外接口::获取一帧视频
    public static byte[] GetOneFrame() {
        byte[] tempVideoData = null;

        m_VideoMutex.lock();
        if (!m_VideoList.isEmpty()) {
            tempVideoData = m_VideoList.remove(0);
        }
        m_VideoMutex.unlock();

        return tempVideoData;
    }

    // 对外接口::发送数据
    public static void SendData(byte[] bytes) {
        m_MyUSB.addSendData(bytes);
    }

    // 分析头信息
    private static int analyseHead(@NotNull byte[] data) {
        // 取12字节头
        if (data.length < 12) {
            return -1;
        }

        // 拆解头信息
        byte[] temp = new byte[4];
        System.arraycopy(data, 0, temp, 0, 4);
        int iType = byteArrayToInt(temp); // 类型

        System.arraycopy(data, 4, temp, 0, 4);
        int iXmlLen = byteArrayToInt(temp); // xml长度

        System.arraycopy(data, 8, temp, 0, 4);
        int iDataLen = byteArrayToInt(temp); // 内容数据长度

        //Log.i("USB", String.format("[USB]解析包头: %08X ", iType) + iXmlLen + " " + iDataLen);

        if (iXmlLen < 0 || iDataLen < 0) {
            return -1;
        }

        // 大于32MB的异常
        if (iXmlLen > 1048576 || iDataLen > 33554432) {
            return -1;
        }

        m_resultData.iXmlLen = iXmlLen;
        m_resultData.iImageDataLen = iDataLen;

        switch (iType) {
            case CAMERA_INFO_TYPE.CAMERA_THROB:
            case CAMERA_INFO_TYPE.CAMERA_THROB_RESPONSE:
            case CAMERA_INFO_TYPE.CAMERA_AUDIO:
            case CAMERA_INFO_TYPE.CAMERA_IMAGE:
            case CAMERA_INFO_TYPE.CAMERA_VIDEO:
            case CAMERA_INFO_TYPE.CAMERA_STRING:
            case CAMERA_INFO_TYPE.CAMERA_STATE:
            case CAMERA_INFO_TYPE.CAMERA_RECORD:
                m_resultData.iType = iType;
                m_resultData.iDataLen = 12 + iXmlLen + iDataLen;
                break;
            default:
                return -1;
        }

        return 0;
    }

    // 添加到结果队列
    private static void AddResultList(byte[] data) {
        m_resultData.data = data;

        // 把符合大小的数据赋值给结果队列
        if (m_resultData.iDataLen == m_resultData.data.length) {
            m_Mutex.lock();
            if (m_ResultList.size() > MAX_RESULT) {
                m_ResultList.remove(0);
            }
            //Log.i("USB", "已经加了一个结果！" + m_resultData.data.length);
            m_ResultList.add(m_resultData.data);
            m_Mutex.unlock();

            // 清空数据
            m_resultData.iType = 0;
            m_resultData.iDataLen = 0;
            m_resultData.data = null;
        }
        else if (m_resultData.iDataLen > m_resultData.data.length) {
            // 清空数据
            m_resultData.iType = 0;
            m_resultData.iDataLen = 0;
            m_resultData.data = null;
        }
    }

    // 添加到视频队列
    private static void AddVideoList(byte[] frameData) {
        m_resultData.data = frameData;

        // 把符合大小的数据赋值给视频队列
        if (m_resultData.iDataLen == m_resultData.data.length) {
            m_VideoMutex.lock();
            if (m_VideoList.size() > MAX_VIDEO) {
                m_VideoList.remove(0);
            }
            Log.i("USB", "已经加了一帧！" + m_resultData.data.length);
            m_VideoList.add(m_resultData.data);
            m_VideoMutex.unlock();

            // 清空数据
            m_resultData.iType = 0;
            m_resultData.iDataLen = 0;
            m_resultData.data = null;
        }
        else if (m_resultData.iDataLen > m_resultData.data.length) {
            // 清空数据
            m_resultData.iType = 0;
            m_resultData.iDataLen = 0;
            m_resultData.data = null;
        }
    }

    // int转byte[]
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)(i & 0xff);
        result[1] = (byte)((i >> 8) & 0xFF);
        result[2] = (byte)((i >> 16) & 0xFF);
        result[3] = (byte)((i >> 24) & 0xFF);
        return result;
    }

    // byte[]转int
    public static int byteArrayToInt(byte[] bytes) {
        int value;
        value = (int) ((bytes[0] & 0xFF)
                | ((bytes[1] & 0xFF) << 8)
                | ((bytes[2] & 0xFF) << 16)
                | ((bytes[3] & 0xFF) << 24));
        return value;
    }

}
