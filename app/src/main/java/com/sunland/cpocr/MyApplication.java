package com.sunland.cpocr;

import com.sunland.cpocr.crash.CrashApplication;

import java.util.logging.Logger;

public class MyApplication extends CrashApplication {

    private Logger logger = Logger.getLogger("MyApplication");
//    public PermissionManager mPermissionMgr;

    @Override
    public void onCreate() {
        super.onCreate();

//        mPermissionMgr = PermissionManager.getInstance(this);
//        mPermissionMgr.checkAllPermission();
//        mPermissionMgr.getAllPermission();

    }



}
