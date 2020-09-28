package com.sunland.cpocr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;



import java.util.ArrayList;
import java.util.List;


/**
 * Created by long
 */

public class PermissionManager {
    public final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    public final static int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    public final static int MY_PERMISSIONS_RECORD_AUDIO = 4;

    //log TAG
    private String MYTAG = "XHLIVE";//MainActivity.xhlogtag;

    //data
    private static Context mContext;
    private boolean mWritePermission = false;   //写权限
    private boolean mReadPermission = false;    //读权限
    private boolean mCameraPermission = false;  //摄像头权限
    private boolean mRecordAudioPermission = false;       //麦克风权限

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
    };

    List<String> mPermissionList = new ArrayList<>();

    //检查所有权限（摄像头相关）
    public void checkAllPermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(mContext, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
    }

    //获取所有权限（摄像头相关）
    public void getAllPermission() {
        if (mPermissionList.isEmpty()) {
            //未授予的权限为空，表示都授予了

        } else {
            //请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions((Activity) mContext, permissions, 1);

        }

    }

    private static class SingletonHolder {
        public static PermissionManager instance = new PermissionManager();
    }

    private PermissionManager() {
    }

    public static PermissionManager getInstance(Context context) {
        mContext = context;
        return SingletonHolder.instance;
    }


    //检查: 写 权限
    public boolean checkWritePermission() {
        int permission = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.e(MYTAG, "No, we do not have WRITE permission");
            mWritePermission = false;
        } else {
            Log.e(MYTAG, "Yes, we have WRITE permission");
            mWritePermission = true;
        }
        return mWritePermission;
    }

    //检查: 读 权限
    public boolean checkReadPermission() {
        int permission = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.e(MYTAG, "No, we do not have READ permission");
            mReadPermission = false;
        } else {
            Log.e(MYTAG, "Yes, we have READ permission");
            mReadPermission = true;
        }

        return mReadPermission;
    }

    //检查: 摄像头 权限
    public boolean checkCameraPermission() {
        int permission = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.e(MYTAG, "No, we do not have CAMERA permission");
            mCameraPermission = false;
        } else {
            Log.e(MYTAG, "Yes, we have CAMERA permission");
            mCameraPermission = true;
        }

        return mCameraPermission;
    }

    //获取 存储 权限
    public void getWritePermission() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

    }

    //获取 读 权限
    public void getReadPermission() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    //获取 摄像头 权限
    public void getCameraPermission() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                Manifest.permission.CAMERA)) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    public void setWritPermissionStatus(boolean status) {
        mWritePermission = status;
    }

    public void setReadPermissionStatus(boolean status) {
        mReadPermission = status;
    }

    public void setCameraPermissionStatus(boolean status) {
        mCameraPermission = status;
    }

    public boolean isWritePermissionOK() {
        return mWritePermission;
    }

    public boolean isReadPermissionOK() {
        return mReadPermission;
    }

    public boolean isCameraPermissionOK() {
        return mCameraPermission;
    }

}