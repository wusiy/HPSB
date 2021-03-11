package com.sunland.cpocr.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.serenegiant.usb.UVCCamera;
import com.sunland.usbcamera.UVCCameraHelper;
import com.sunland.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.sunland.cpocr.R;
import com.lpr.LPR;
import com.sunland.cpocr.db.DbCpHmZp;
import com.sunland.cpocr.event.BusFactory;
import com.sunland.cpocr.event.EventCenter;
import com.sunland.cpocr.utils.CpocrUtils;
import com.sunland.cpocr.view.LPRfinderView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UsbCameraActivity extends AppCompatActivity implements
        CameraDialog.CameraDialogParent, CameraViewInterface.Callback{
    private static final String TAG = "ocr";

    //private Camera mCamera;
    protected RelativeLayout re_c;
    private LPR api = null;
    private int preWidth = 0;
    private int preHeight = 0;
    private int width;
    private int height;
    private Timer mTimer;
    protected LPRfinderView myView = null;
    private boolean bInitKernal = false;
    protected Vibrator mVibrator;
    private boolean bROI = false;
    private int[] m_ROI = {0, 0, 0, 0};
    protected int rotateFlag = 0;//图像的旋转角度
    protected boolean bSerialMode = false; //是否是连续识别模式
    private long mLastCaptureTime = 0;
    protected ActionBar actionBar;
    private DbCpHmZp DbHepler;

    private EditText etSetBrightness;
    private Button btResetBrightness;
    private EditText etSetContrast;
    private Button btResetContrast;
    private CheckBox cbAutoContrast;
    private EditText etSetZoom;
    private Button btResetZoom;
    private EditText etSetFocus;
    private Button btResetFocus;
    private CheckBox cbAutoFocus;
    private EditText etSetHue;
    private Button btResetHue;
    private CheckBox cbAutoHue;
    private EditText etSetSaturation;
    private Button btResetSaturation;
    private EditText etSetSharpness;
    private Button btResetSharpness;
    private EditText etSetGamma;
    private Button btResetGamma;
    private EditText etSetGain;
    private Button btResetGain;

    private View mTextureView;
    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private SeekBar mSeekBrightness;
    private SeekBar mSeekContrast;
    private AlertDialog mDialog;
    private boolean isRequest;
    private boolean isPreview;
    private String pathPic;
    private int EVENT_CP = 1;
    private String carInfo;
    public int l1, r1, t1, b1;

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("fail to connect,please check resolution params");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("connecting");
                // need to wait UVCCamera initialize over
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2500);
                            initSetting();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("摄像头已断开");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_usbcamera);

        actionBar = getSupportActionBar();
        BusFactory.getBus().register(this);
        LPR.getInstance().copyDataBase(this);
        initView();
        initData();
        initListener();

    }

    private void initView() {
        re_c = (RelativeLayout) findViewById(R.id.re_c);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels;
        height = metric.heightPixels;

        if (myView == null) {
            myView = new LPRfinderView(UsbCameraActivity.this, width, height, bSerialMode);
            //re_c.addView(myView);
        }

        mTextureView = findViewById(R.id.camera_view);

        etSetBrightness = findViewById(R.id.et_setbrightness);
        btResetBrightness = findViewById(R.id.bt_resetbrightness);
        etSetContrast = findViewById(R.id.et_setcontrast);
        btResetContrast = findViewById(R.id.bt_resetcontrast);
        cbAutoContrast = findViewById(R.id.cb_autocontrast);
        etSetZoom = findViewById(R.id.et_setzoom);
        btResetZoom = findViewById(R.id.bt_resetzoom);
        etSetFocus = findViewById(R.id.et_setfocus);
        btResetFocus = findViewById(R.id.bt_resetfocus);
        cbAutoFocus = findViewById(R.id.cb_autofocus);
        etSetHue = findViewById(R.id.et_sethue);
        btResetHue = findViewById(R.id.bt_resethue);
        cbAutoHue = findViewById(R.id.cb_autohue);
        etSetSaturation = findViewById(R.id.et_setsaturation);
        btResetSaturation = findViewById(R.id.bt_resetsaturation);
        etSetSharpness = findViewById(R.id.et_setsharpness);
        btResetSharpness = findViewById(R.id.bt_resetsharpness);
        etSetGamma = findViewById(R.id.et_setgamma);
        btResetGamma = findViewById(R.id.bt_resetgamma);
        etSetGain = findViewById(R.id.et_setgain);
        btResetGain = findViewById(R.id.bt_resetgain);

        mSeekBrightness = findViewById(R.id.seekbar_brightness);
        mSeekContrast = findViewById(R.id.seekbar_contrast);
//        mSeekBrightness.setMax(100);
//        mSeekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
//                    mCameraHelper.setModelValue(UVCCameraHelper.UVC_BRIGHTNESS,progress);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//        mSeekContrast.setMax(100);
//        mSeekContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
//                    mCameraHelper.setModelValue(UVCCameraHelper.UVC_BRIGHTNESS,progress);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
    }

    private void initData(){
        bSerialMode = isSerialMode();
        File sd = Environment.getExternalStorageDirectory();
        pathPic = sd.getPath() + "/HPSB_demo/Plate/";
        File destDir = new File(pathPic);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        mUVCCameraView = (CameraViewInterface) mTextureView;
        mUVCCameraView.setCallback(this);
        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);
        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {

                Log.d(TAG, "onPreviewResult: "+nv21Yuv.length);
                byte [] data = nv21Yuv;
                long currentTime = System.currentTimeMillis();
                boolean canCapture = true;
                if (currentTime - mLastCaptureTime <= 100) {//识别延迟  单位 ms
                    canCapture = false;
                }
                if (!pauseOcr() && bInitKernal && canCapture) {
                    final int w = mCameraHelper.getPreviewWidth();  //宽度
                    final int h = mCameraHelper.getPreviewHeight();

                    final YuvImage image = new YuvImage(data, ImageFormat.NV21, w, h, null);
                    ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
                    if(!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)){
                        return ;
                    }
                    byte[] tmp = os.toByteArray();
                    Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0,tmp.length);

                    mLastCaptureTime = System.currentTimeMillis();
                    byte[] result;//[] = new byte[10];
                    String res = "";
                    result = api.VideoRec(data, w, h, rotateFlag);
                    try {
                        res = new String(result, "gb2312");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        res = null;
                    }
                    if (res != null && !"".equals(res.trim())) {
                        Intent intent = new Intent();
                        final String[] hpArray = res.split(",", -1);
                        if (hpArray.length > 1) {
                            final String hphm = hpArray[0];
                            final String hpys = hpArray[1];
                            String hpysStr = "";
                            try {
                                hpysStr = CpocrUtils.getHphzys(hpys);
                            } catch (Exception e) {
                                e.printStackTrace();
                                hpysStr = "";
                            }

                            mVibrator.vibrate(200);
                            Matrix matrix = new Matrix();
                            matrix.postRotate(180);
                            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                            String pathPhoto = saveMyBitmap(bmp);
                            DbHepler = new DbCpHmZp(UsbCameraActivity.this);
                            DbHepler.open();
                            DbHepler.save_carinfo(hphm, hpysStr, pathPhoto);
                            DbHepler.close();
                            carInfo = hphm +  "  " + hpysStr;
                            BusFactory.getBus().post(new EventCenter(EVENT_CP));
                        }
                    }
                }
            }
        });
    }

    private void initSetting(){
        if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
            etSetBrightness.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_BRIGHTNESS));
            etSetContrast.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_CONTRAST));
            cbAutoContrast.setChecked(mCameraHelper.getModelValue(UVCCameraHelper.UVC_AUTO_CONTRAST) == 1);
            etSetZoom.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_ZOOM));
            etSetFocus.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_FOCUS));
            cbAutoFocus.setChecked(mCameraHelper.getModelValue(UVCCameraHelper.UVC_AUTO_FOCUS) == 1);
            etSetHue.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_HUE));
            cbAutoHue.setChecked(mCameraHelper.getModelValue(UVCCameraHelper.UVC_AUTO_HUE) == 1);
            etSetSaturation.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_SATURATION));
            etSetSharpness.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_SHARPNESS));
            etSetGamma.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_GAMMA));
            etSetGain.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_GAIN));

        }
    }

    private void initListener(){
        etSetBrightness.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int brightness = mCameraHelper.setModelValue(UVCCameraHelper.UVC_BRIGHTNESS,Integer.parseInt(s.toString()));
                    showShortMsg("亮度: " + brightness);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int brightness = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_BRIGHTNESS);
                    etSetBrightness.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_BRIGHTNESS));
                    showShortMsg("亮度: " + brightness);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        etSetContrast.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int contrast = mCameraHelper.setModelValue(UVCCameraHelper.UVC_CONTRAST,Integer.parseInt(s.toString()));
                    showShortMsg("对比度: " + contrast);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetContrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int contrast = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_CONTRAST);
                    etSetContrast.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_CONTRAST));
                    showShortMsg("对比度: " + contrast);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        cbAutoContrast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    String s = mCameraHelper.setModelValue(UVCCameraHelper.UVC_AUTO_CONTRAST, cbAutoContrast.isChecked() ? 1 : 0) == 1 ? "开启" : "关闭";
                    showShortMsg("自动对比度: " + s);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        etSetZoom.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int zoom = mCameraHelper.setModelValue(UVCCameraHelper.UVC_ZOOM,Integer.parseInt(s.toString()));
                    showShortMsg("缩放: " + zoom);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int zoom = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_ZOOM);
                    etSetZoom.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_ZOOM));
                    showShortMsg("缩放: " + zoom);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        etSetFocus.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int focus = mCameraHelper.setModelValue(UVCCameraHelper.UVC_FOCUS,Integer.parseInt(s.toString()));
                    showShortMsg("对焦: " + focus);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int focus = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_FOCUS);
                    etSetFocus.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_FOCUS));
                    showShortMsg("对焦: " + focus);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        cbAutoFocus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    String s = mCameraHelper.setModelValue(UVCCameraHelper.UVC_AUTO_FOCUS, cbAutoFocus.isChecked() ? 1 : 0) == 1 ? "开启" : "关闭";
                    showShortMsg("自动对焦: " + s);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        etSetHue.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int hue = mCameraHelper.setModelValue(UVCCameraHelper.UVC_HUE,Integer.parseInt(s.toString()));
                    showShortMsg("色调: " + hue);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetHue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int hue = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_HUE);
                    etSetHue.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_HUE));
                    showShortMsg("色调: " + hue);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        cbAutoHue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    String s = mCameraHelper.setModelValue(UVCCameraHelper.UVC_AUTO_HUE, cbAutoFocus.isChecked() ? 1 : 0) == 1 ? "开启" : "关闭";
                    showShortMsg("色调: " + s);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        etSetSaturation.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int saturation = mCameraHelper.setModelValue(UVCCameraHelper.UVC_SATURATION,Integer.parseInt(s.toString()));
                    showShortMsg("饱和度: " + saturation);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetSaturation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int saturation = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_SATURATION);
                    etSetHue.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_SATURATION));
                    showShortMsg("饱和度: " + saturation);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        etSetSharpness.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int sharpness = mCameraHelper.setModelValue(UVCCameraHelper.UVC_SHARPNESS,Integer.parseInt(s.toString()));
                    showShortMsg("锐度: " + sharpness);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetSharpness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int sharpness = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_SHARPNESS);
                    etSetSharpness.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_SHARPNESS));
                    showShortMsg("饱和度: " + sharpness);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        etSetGamma.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int gamma = mCameraHelper.setModelValue(UVCCameraHelper.UVC_GAMMA,Integer.parseInt(s.toString()));
                    showShortMsg("伽马: " + gamma);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetGamma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int sharpness = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_GAMMA);
                    etSetGamma.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_GAMMA));
                    showShortMsg("伽马: " + sharpness);
                } else
                    showShortMsg("摄像头未开启");
            }
        });

        etSetGain.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int gain = mCameraHelper.setModelValue(UVCCameraHelper.UVC_GAIN,Integer.parseInt(s.toString()));
                    showShortMsg("增益: " + gain);
                } else
                    showShortMsg("摄像头未开启");

            }
        });

        btResetGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                    int gain = mCameraHelper.resetModelValue(UVCCameraHelper.UVC_GAIN);
                    etSetGain.setText(mCameraHelper.getModelValue(UVCCameraHelper.UVC_GAIN));
                    showShortMsg("增益: " + gain);
                } else
                    showShortMsg("摄像头未开启");
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toobar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.track_records:
                Intent intent1;
                intent1 = new Intent(UsbCameraActivity.this, CpRecordActivity.class);
                startActivity(intent1);
                break;

            case R.id.menu_takepic:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("摄像头未打开");
                    return super.onOptionsItemSelected(item);
                }
                String picPath = pathPic + System.currentTimeMillis() + UVCCameraHelper.SUFFIX_JPEG;

                mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
                    @Override
                    public void onCaptureResult(String path) {
                        if(TextUtils.isEmpty(path)) {
                            return;
                        }
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showShortMsg("图片已保存: " + path);
                            }
                        });
                    }
                });
                break;
            case R.id.menu_recording:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("摄像头未打开");
                    return super.onOptionsItemSelected(item);
                }
                if (!mCameraHelper.isPushing()) {
                    String videoPath = pathPic + System.currentTimeMillis() + UVCCameraHelper.SUFFIX_MP4;

//                    FileUtils.createfile(FileUtils.ROOT_PATH + "test666.h264");
                    // if you want to record,please create RecordParams like this
                    RecordParams params = new RecordParams();
                    params.setRecordPath(videoPath);
                    params.setRecordDuration(0);                        // auto divide saved,default 0 means not divided
                    params.setVoiceClose(false);    // is close voice

                    //params.setSupportOverlay(true); // overlay only support armeabi-v7a & arm64-v8a
                    mCameraHelper.startPusher(params, new AbstractUVCCameraHandler.OnEncodeResultListener() {
                        @Override
                        public void onEncodeResult(byte[] data, int offset, int length, long timestamp, int type) {
                            // type = 1,h264 video stream
                            if (type == 1) {
                                FileUtils.putFileStream(data, offset, length);
                            }
                            // type = 0,aac audio stream
                            if(type == 0) {

                            }
                        }

                        @Override
                        public void onRecordResult(String videoPath) {
                            if(TextUtils.isEmpty(videoPath)) {
                                return;
                            }
                            new Handler(getMainLooper()).post(() -> Toast.makeText(com.sunland.cpocr.activity.UsbCameraActivity.this, "save videoPath:"+videoPath, Toast.LENGTH_SHORT).show());
                        }
                    });
                    // if you only want to push stream,please call like this
                    // mCameraHelper.startPusher(listener);
                    showShortMsg("开始录制");
                } else {
                    FileUtils.releaseFile();
                    mCameraHelper.stopPusher();
                    showShortMsg("停止录制");
                }
                break;
            case R.id.menu_resolution:
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                    showShortMsg("摄像头未打开");
                    return super.onOptionsItemSelected(item);
                }
                showResolutionListDialog();
                break;
            case R.id.menu_focus:
//                if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
//                    showShortMsg("摄像头未打开");
//                    return super.onOptionsItemSelected(item);
//                }
//                mCameraHelper.startCameraFoucs();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showResolutionListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(com.sunland.cpocr.activity.UsbCameraActivity.this);
        View rootView = LayoutInflater.from(com.sunland.cpocr.activity.UsbCameraActivity.this).inflate(R.layout.layout_dialog_list, null);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_dialog);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(com.sunland.cpocr.activity.UsbCameraActivity.this, android.R.layout.simple_list_item_1, getResolutionList());
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (mCameraHelper == null || !mCameraHelper.isCameraOpened())
                    return;
                final String resolution = (String) adapterView.getItemAtPosition(position);
                String[] tmp = resolution.split("x");
                if (tmp != null && tmp.length >= 2) {
                    int widht = Integer.valueOf(tmp[0]);
                    int height = Integer.valueOf(tmp[1]);
                    mCameraHelper.updateResolution(widht, height);
                    preWidth = widht;
                    preHeight = height;
                    api.init(UsbCameraActivity.this, 0, 0, preWidth, preHeight, preWidth, preHeight);
                }
                mDialog.dismiss();
            }
        });

        builder.setView(rootView);
        mDialog = builder.create();
        mDialog.show();
    }

    // example: {640x480,320x240,etc}
    private List<String> getResolutionList() {
        List<com.serenegiant.usb.Size> list = mCameraHelper.getSupportedPreviewSizes();
        List<String> resolutions = null;
        if (list != null && list.size() != 0) {
            resolutions = new ArrayList<>();
            for (Size size : list) {
                if (size != null) {
                    resolutions.add(size.width + "x" + size.height);
                }
            }
        }
        return resolutions;
    }

    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("取消操作");
        }
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }

        try {
            preWidth = mCameraHelper.getPreviewWidth();
            preHeight = mCameraHelper.getPreviewHeight();
            Log.d("QQQQQQQQQQQQ", preWidth + "  " + preHeight);

            if (!bROI) {
                int l, t, r, b;
                int roiL, roiT, roiR, roiB;

                if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    l = 10;
                    r = width - 10;
                    int ntmpH = (r - l) * 58 / 88;
                    if (bSerialMode)
                        t = 10;
                    else
                        t = (height - ntmpH) / 2;
                    b = t + ntmpH;
                } else {
                    t = height * 1 / 5;
                    b = height * 4 / 5;
                    int ntmpW = (b - t) * 88 / 68;
                    if (bSerialMode) {
                        l = 10;
                        r = l + ntmpW;
                    } else {
                        l = (width - ntmpW) / 2;
                        r = width - l;
                    }
                }

                double proportion = 0, hproportion = 0;
                if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    proportion = (double) width / (double) preHeight;
                    hproportion = (double) height / (double) preWidth;
                } else {
                    proportion = (double) width / (double) preWidth;
                    hproportion = (double) height / (double) preHeight;
                }

                roiL = (int) (l / proportion);
                roiT = (int) (t / hproportion);
                roiR = (int) (r / proportion);
                roiB = (int) (b / hproportion);
//                m_ROI[0] = roiL;
//                m_ROI[1] = roiT;
//                m_ROI[2] = roiR;
//                m_ROI[3] = roiB;

//                m_ROI[0] = 30;
//                m_ROI[1] = 300;
//                m_ROI[2] = 495;
//                m_ROI[3] = 700;

                m_ROI[0] = 0;
                m_ROI[1] = 0;
                m_ROI[2] = preWidth;
                m_ROI[3] = preHeight;
                Log.d(TAG, "left = " + roiL + ",top = " + roiT + ",right = " + roiR + "，button = " + roiB);

                bROI = true;
            }
            if (mTimer == null) {
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                    }
                }, 500, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (api == null) {
            int code = initPlatOcrApi();
            if (code != 0) {
                String err = "";
                switch (code) {
                    case -1:
                        err = "识别ROI区域有问题，超过图像的宽高";
                        break;
                    case -2:
                        err = "ROI区域的左坐标大于右坐标，或者上坐标大于下坐标";
                        break;
                    case -3:
                        err = "ROI区域超过算法支持最大宽度和高度，目前算法最大支持宽高都为3000像素";
                        break;
                    case -4:
                        err = "秘钥文件不存在";
                        break;
                    case -5:
                        err = "秘钥验证失败";
                        break;
                    case -6:
                        err = "秘钥过期";
                        break;
                    default:
                        err = "激活失败";
                        break;
                }
                Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
                bInitKernal = false;
            } else {
                bInitKernal = true;
            }
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventCenter eventCenter) {
        if (null != eventCenter) {
            int eventCode = eventCenter.getEventCode();
            eventComing(eventCode);
        }
    }

    private void eventComing(int eventCode){
        if(eventCode == EVENT_CP){
        Toast.makeText(this, carInfo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusFactory.getBus().unregister(this);
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }


    /**
     * 初始化号牌识别
     *
     * @return 0--表示成功 其他表示失败
     */
    private int initPlatOcrApi() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            return initPlatOcrApiProtrait();
        else
            return initPlatOcrApiLandScape();
    }

    /**
     * 横屏下初始化号牌识别相关参数
     */
    private int initPlatOcrApiLandScape() {
        int nRet = 0;
        api = LPR.getInstance();
        //如果是索信8核机器
        if ("P990-XD(7.0)".equalsIgnoreCase(Build.MODEL)) {
            //nRet = api.init(this, preWidth - m_ROI[2], m_ROI[1], preWidth - m_ROI[0], m_ROI[3], preWidth, preHeight);
            nRet = api.init(this, 0, 0, preWidth, preHeight, preWidth, preHeight);

            rotateFlag = 3;
        } else if("P999-XD".equalsIgnoreCase(Build.MODEL)) {
            //nRet = api.init(this, preWidth - m_ROI[2], preHeight - m_ROI[3],  preWidth - m_ROI[0], preHeight - m_ROI[1], preWidth, preHeight);
            nRet = api.init(this, 0, 0, preWidth, preHeight, preWidth, preHeight);

            rotateFlag = 3;
        } else
        {
            //nRet = api.init(this, preHeight - m_ROI[2], m_ROI[1], preHeight - m_ROI[0], m_ROI[3], preWidth, preHeight);
            nRet = api.init(this, 0, 0, preWidth, preHeight, preWidth, preHeight);
            rotateFlag = 3;
        }
        return nRet;
    }

    /**
     * 竖屏状态下初始化OCR
     */
    private int initPlatOcrApiProtrait() {
        int nRet = 0;
        //索信P990八核机器
        api = LPR.getInstance();
        if ("P990-XD(7.0)".equalsIgnoreCase(Build.MODEL)) {
            rotateFlag = 4;//图片旋转角度表示
            nRet = api.init(this, m_ROI[0], preWidth - m_ROI[3], m_ROI[2], preWidth - m_ROI[1], preHeight, preWidth);
        } else {
            rotateFlag = 1;
            nRet = api.init(this, m_ROI[0], m_ROI[1], m_ROI[2], m_ROI[3], preHeight, preWidth);
        }
        return nRet;
    }

    /**
     * 是否对OCR进行暂停OCR
     *
     * @return 返回true表示暂停识别 false表示继续识别
     */
    protected boolean pauseOcr() {
        return false;
    }

    private void releaseRes() {
//        try {
//            if (mCamera != null) {
//                mCamera.setPreviewCallback(null);
//                mCamera.stopPreview();
//                mCamera.release();
//                mCamera = null;
//            }
//        } catch (Exception e) {
//        }
        if (bInitKernal) {
            bInitKernal = false;
            api = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            releaseRes();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isSerialMode() {
        return true;
    }

    //保存文件到指定路径
    private String saveMyBitmap(Bitmap bitmap) {
        File sd = Environment.getExternalStorageDirectory();
        File destDir = new File(sd.getPath() + "/HPSB/");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        String tmpfile = sd.getPath() + "/HPSB/" + System.currentTimeMillis() + ".jpg";
        BufferedOutputStream bos = null;
        try {
            try {
                bos = new BufferedOutputStream(new FileOutputStream(tmpfile));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            bitmap.recycle();
        } catch (Exception e) {
            Log.e("vvv", "保存失败");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (bos != null)
                try {
                    bos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return tmpfile;
    }

}
