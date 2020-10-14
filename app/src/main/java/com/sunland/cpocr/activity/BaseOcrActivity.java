package com.sunland.cpocr.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.sunland.cpocr.R;
import com.lpr.LPR;
import com.sunland.cpocr.db.DbCpHmZp;
import com.sunland.cpocr.utils.CpocrUtils;
import com.sunland.cpocr.utils.VerticalSeekBar;
import com.sunland.cpocr.view.LPRfinderView;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public abstract class BaseOcrActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "ocr";
    /**
     * 号牌号码
     */
    public static final String EXTRA_RET_HPHM = "cphm";

    /**
     * 号牌颜色代码
     */
    public static final String EXTRA_RET_HPYS_CODE = "cpysbh";
    /**
     * 号牌颜色中文
     */
    public static final String EXTRA_RET_HPYS_STR = "cpysmc";
    /**
     * 号牌种类
     */
    public static final String EXTRA_RET_HPZL = "hpzl";
    /**
     * 号牌种类中文
     */
    public static final String EXTRA_RET_HPZL_STR = "hpzl_str";

    //    private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/";
    private Camera mCamera;
    protected SurfaceView surfaceView;
    protected RelativeLayout re_c;
    private SurfaceHolder surfaceHolder;
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
    protected ImageButton mFlashBtn;//闪光灯
    protected ImageButton mBackBtn;//返回按钮
    private Camera.Parameters mCameraParameters;
    protected int rotateFlag = 0;//图像的旋转角度
    protected boolean bSerialMode = false; //是否是连续识别模式
    protected VerticalSeekBar mSeekBar;
    private long mLastCaptureTime = 0;
    protected ActionBar actionBar;
    private DbCpHmZp DbHepler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

         actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.hide();
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        LPR.getInstance().copyDataBase(this);
        bSerialMode = isSerialMode();
        setContentView(R.layout.activity_base_ocr);
       // mapView.setVisibility(View.GONE);
        findView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findView() {

        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe);
        re_c = (RelativeLayout) findViewById(R.id.re_c);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels;
        height = metric.heightPixels;

        if (myView == null) {
            myView = new LPRfinderView(BaseOcrActivity.this, width, height, bSerialMode);
            Log.d("qqqqqq","ccc");
            re_c.addView(myView);
        }

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(BaseOcrActivity.this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setFocusable(true);
        mFlashBtn = (ImageButton) findViewById(R.id.photoflash);
        mFlashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor settings = getSharedPreferences("camera_params", 0).edit();
                String flashMode = mCameraParameters.getFlashMode();
                //闪光灯只亮一下(持续为 Parameters.FLASH_MODE_TORCH)
                if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON) ||
                        flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    mFlashBtn.setImageResource(R.drawable.cpocr_flash_off);
                    mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    settings.putString("FLASH_MODE", Camera.Parameters.FLASH_MODE_OFF);
                } /*else if (flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)){//自动
                    flashBtn.setImageResource(R.drawable.cpocr_flash_off);
                    mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    settings.putString("FLASH_MODE", Camera.Parameters.FLASH_MODE_OFF);
                }*/ else {  //默认关闭
                    mFlashBtn.setImageResource(R.drawable.cpocr_flash_on);
                    mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    settings.putString("FLASH_MODE", Camera.Parameters.FLASH_MODE_TORCH);
                }
                settings.commit();
                mCamera.setParameters(mCameraParameters);
            }
        });
        mBackBtn = (ImageButton) findViewById(R.id.orc_back_btn);
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mSeekBar = (VerticalSeekBar) findViewById(R.id.vertical_Seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    mCameraParameters.setZoom(progress);
                    mCamera.setParameters(mCameraParameters);
                } catch (Exception e) {
                    Camera.Parameters parameters = mCamera.getParameters();// 得到摄像头的参数
                    mCamera.setParameters(parameters);
                }
            }
        });
    }

    private void showFlashBtn() {
        // 读取注册表信息
        SharedPreferences sharedata = getSharedPreferences("camera_params", 0);
        String flashMode = sharedata.getString("FLASH_MODE", Camera.Parameters.FLASH_MODE_OFF);
        //闪光灯只亮一下(持续为 Parameters.FLASH_MODE_TORCH)
        if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON) || flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH))
            mFlashBtn.setImageResource(R.drawable.cpocr_flash_on);
        /*else if (flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO))//自动
            flashBtn.setImageResource(R.drawable.cpocr_flash_auto);*/
        else  //默认关闭
            mFlashBtn.setImageResource(R.drawable.cpocr_flash_off);
        mCameraParameters.setFlashMode(flashMode);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                mCamera.setPreviewDisplay(holder);
                initCamera();

                if (mTimer == null) {
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (mCamera != null) {
                                try {
                                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                        public void onAutoFocus(boolean success, Camera camera) {
                                            Log.d("autoFucus", "onAutoFocus: ");
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, 500, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String mess = "打开摄像头失败";
                Toast.makeText(getApplicationContext(), mess, Toast.LENGTH_LONG).show();
                return;
            }
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
            nRet = api.init(this, preWidth - m_ROI[2], m_ROI[1], preWidth - m_ROI[0], m_ROI[3], preWidth, preHeight);
            rotateFlag = 3;
        } else if("P999-XD".equalsIgnoreCase(Build.MODEL)) {
            nRet = api.init(this, preWidth - m_ROI[2], preHeight - m_ROI[3],  preWidth - m_ROI[0], preHeight - m_ROI[1], preWidth, preHeight);
            rotateFlag = 3;
        } else
         {
            nRet = api.init(this, preHeight - m_ROI[2], m_ROI[1], preHeight - m_ROI[0], m_ROI[3], preWidth, preHeight);
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

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseRes();
    }

    private void releaseRes() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
        }
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

    /**
     * 更新UI界面
     *
     * @param frameLeft   屏幕上显示框的左边绝对坐标
     * @param frameTop    屏幕上识别框顶端绝对坐标
     * @param frameRight  屏幕上识别框右侧绝对坐标
     * @param frameButton 屏幕识别框底部绝对坐标
     */
    protected abstract void updateUi(int frameLeft, int frameTop, int frameRight, int frameButton);

    /**
     * 该功能是否为连续识别模式
     *
     * @return true-表示该功能是连续识别模式 false 表示是单次识别
     */
    protected abstract boolean isSerialMode();

    @TargetApi(14)
    private void initCamera() {
        mCameraParameters = mCamera.getParameters();
        List<Size> list = mCameraParameters.getSupportedPreviewSizes();
        Size size;
        Size tmpsize = null;

        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏环境
            tmpsize = CpocrUtils.getOptimalPreviewSize(list, height, width);
        } else {
            //横屏环境
            tmpsize = CpocrUtils.getOptimalPreviewSize(list, width, height);
        }

        int length = list.size();
        int previewWidth = list.get(0).width;
        int previewheight = list.get(0).height;
        int second_previewWidth = 0;
        int second_previewheight = 0;
        previewWidth = tmpsize.width;
        previewheight = tmpsize.height;
        if (length == 1) {
            preWidth = previewWidth;
            preHeight = previewheight;
        } else {
            second_previewWidth = previewWidth;
            second_previewheight = previewheight;
            for (int i = 0; i < length; i++) {
                size = list.get(i);
                if (size.height > 700 && size.height < previewheight) {
                    if (size.width * previewheight == size.height * previewWidth && size.height < second_previewheight) {
                        second_previewWidth = size.width;
                        second_previewheight = size.height;
                    }
                }
            }
            preWidth = second_previewWidth ;
            preHeight = second_previewheight;
        }

        mCameraParameters.setPictureFormat(PixelFormat.JPEG);
        mCameraParameters.setPreviewSize(preWidth, preHeight);

        if (mCameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        if (mCameraParameters.isZoomSupported()) {
            mCameraParameters.setZoom(2);
        }
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
            m_ROI[0] = roiL;
            m_ROI[1] = roiT;
            m_ROI[2] = roiR;
            m_ROI[3] = roiB;
            Log.d(TAG, "left = " + roiL + ",top = " + roiT + ",right = " + roiR + "，button = " + roiB);
            //	m_ROI[0]=0;
            //	m_ROI[1]=0;
            //	m_ROI[2]=preHeight;
            //	m_ROI[3]=preWidth;
            bROI = true;
            updateUi(l, t, r, b);

//            if (bSerialMode) {
//                ViewGroup.MarginLayoutParams marginParams = null;
//                ViewGroup.LayoutParams lp = llTest.getLayoutParams();
//                if (lp instanceof ViewGroup.MarginLayoutParams) {
//                    marginParams = (ViewGroup.MarginLayoutParams) lp;
//                } else {
//                    marginParams = new ViewGroup.MarginLayoutParams(lp);
//                }
//
//                marginParams.setMargins(marginParams.leftMargin, b + 20, marginParams.rightMargin, marginParams.bottomMargin);
//                llTest.setLayoutParams(marginParams);
//                llTest.setVisibility(View.VISIBLE);
//            } else {
//                llTest.setVisibility(View.GONE);
//            }

        }
        if (mCameraParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        if (mCameraParameters.isZoomSupported()) {
            mCameraParameters.setZoom(2);
        }
        mCamera.setPreviewCallback(BaseOcrActivity.this);
        mCamera.setParameters(mCameraParameters);
        mCamera.setDisplayOrientation(getPreviewDegree(this, 0));

        mCamera.startPreview();
        showFlashBtn();
    }

    private int getPreviewDegree(Activity activity, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 处理返回数据
     *
     * @param intent 号牌识别的数据结构
     */
    protected abstract void handleIntent(Intent intent);

    /**
     * 截取每帧视频流
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        long currentTime = System.currentTimeMillis();
        boolean canCapture = true;
        if (currentTime - mLastCaptureTime <= 800) {//识别延迟  单位 ms
            canCapture = false;
        }
        if (!pauseOcr() && bInitKernal && canCapture) {
            Size size = mCamera.getParameters().getPreviewSize(); //获取预览大小
            final int w = size.width;  //宽度
            final int h = size.height;
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
            result = api.VideoRec(data, preWidth, preHeight, rotateFlag);
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

                    mVibrator.vibrate(300);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    String pathPhoto = saveMyBitmap(bmp);
                    DbHepler = new DbCpHmZp(this);
                    DbHepler.open();
                    DbHepler.save_carinfo(hphm,pathPhoto);
                    DbHepler.close();

                    intent.putExtra(EXTRA_RET_HPHM, hphm);
                    intent.putExtra(EXTRA_RET_HPYS_CODE, hpys);
                    intent.putExtra(EXTRA_RET_HPYS_STR, hpysStr);
                    handleIntent(intent);
                }
            }
        }
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

    protected int dp2px(int dpValue) {
        return (int) getResources().getDisplayMetrics().density * dpValue;
    }
}
