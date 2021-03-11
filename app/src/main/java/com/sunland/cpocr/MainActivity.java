package com.sunland.cpocr;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.MarkerOptions;
import com.sunland.cpocr.activity.LprMapActivity;
import com.sunland.cpocr.activity.OfflineLprMapActivity;
import com.sunland.cpocr.activity.UsbCameraActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 号牌识别
 */

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{
    public static final String FAVTYPE_KEY = "favtype";
    public static final String POIITEM_STR_KEY = "poiitem_str";
    public static final String NAVI_TYPE_KEY = "navi";
    public static final String IS_TRACING_KEY = "track";
    private static final int num = 123;//用于验证获取的权
    private String permissions[] = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requireSomePermission();

        File sd = Environment.getExternalStorageDirectory();
        File destDir = new File(sd.getPath() + "/HPSB/");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        final String items[] = {"在线模式", "离线模式", "UsbCamera"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择模式")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            Intent intent;
                            intent = new Intent(MainActivity.this, LprMapActivity.class);
                            intent.putExtra(NAVI_TYPE_KEY, "");
                            startActivity(intent);
                            finish();
                        } else if(which == 1){
                            Intent intent;
                            intent = new Intent(MainActivity.this, OfflineLprMapActivity.class);
                            intent.putExtra(NAVI_TYPE_KEY, "");
                            startActivity(intent);
                            finish();
                        } else if(which == 2){
                            Intent intent;
                            intent = new Intent(MainActivity.this, UsbCameraActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    @AfterPermissionGranted(num)
    private void requireSomePermission() {
        String[] perms = {
                // 把你想要申请的权限放进这里就行，注意用逗号隔开
                Manifest.permission.MODIFY_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,

        };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ..
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "应用相关权限",
                    num, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //Toast.makeText(this, "Permissions Granted!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        //Toast.makeText(this, "Permissions Denied!", Toast.LENGTH_LONG).show();
    }
}
