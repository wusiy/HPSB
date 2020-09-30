package com.sunland.cpocr;

import android.content.Intent;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.sunland.cpocr.activity.LprMapActivity;

import java.io.File;

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

        Intent intent;
        intent = new Intent(MainActivity.this, LprMapActivity.class);
        intent.putExtra(NAVI_TYPE_KEY, "");
        startActivity(intent);
        finish();

    }
}