package com.sunland.cpocr;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.sunland.cpocr.activity.LprMapActivity;
import com.sunland.cpocr.activity.T1;
import com.sunland.cpocr.db.DbTracks;
import com.sunland.cpocr.utils.DialogHelp;

import java.io.File;

import static com.sunland.cpocr.activity.LprMapActivity.IS_TRACING_KEY;
import static com.sunland.cpocr.activity.LprMapActivity.NAVI_TYPE_KEY;

public class MainActivity extends AppCompatActivity {

    private PermissionManager mPermissionMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mPermissionMgr = PermissionManager.getInstance(this);
//        mPermissionMgr.checkAllPermission();
//        mPermissionMgr.getAllPermission();

        File sd = Environment.getExternalStorageDirectory();
        File destDir = new File(sd.getPath() + "/HPSB/");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        final String items[] = {"模拟导航", "实时导航"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择导航方式")
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
                            intent = new Intent(MainActivity.this, T1.class);
                            intent.putExtra(NAVI_TYPE_KEY, "");
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


//        Intent intent;
//        intent = new Intent(MainActivity.this, LprMapActivity.class);
//        intent.putExtra(NAVI_TYPE_KEY, "");
//        startActivity(intent);
//        finish();

    }
}