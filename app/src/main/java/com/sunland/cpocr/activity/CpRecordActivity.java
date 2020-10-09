package com.sunland.cpocr.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.sunland.cpocr.R;
import com.sunland.cpocr.activity.adapter.CphmAdapter;
import com.sunland.cpocr.activity.adapter.CpzpAdapter;
import com.sunland.cpocr.db.DbCpHmZp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class CpRecordActivity extends AppCompatActivity {

    private RecyclerView recyclerView_cphm;
    private RecyclerView recyclerView_cpzp;
    private ImageView   iv_cp;
    private CphmAdapter cphmAdapter;
    private DbCpHmZp DbHepler;
    private String[] splitZp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp_records);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        iv_cp = (ImageView) findViewById(R.id.iv_cp);
        recyclerView_cphm = (RecyclerView) findViewById(R.id.recycler_cplist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_cphm.setLayoutManager(layoutManager);
        recyclerView_cpzp = (RecyclerView) findViewById(R.id.recycler_cpphotolist);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        recyclerView_cpzp.setLayoutManager(layoutManager1);

        DbHepler = new DbCpHmZp(this);
        DbHepler.open();
        List<String> cphm = DbHepler.queryAllCarNum();
        DbHepler.close();

        cphmAdapter = new CphmAdapter(cphm);
        cphmAdapter.setScrollTargetPositionListener(new CphmAdapter.OnScrollTargetPositionListener() {
            @Override
            public void scrollToPosition(int postion) {
                recyclerView_cphm.scrollToPosition(postion);
            }
        });
        cphmAdapter.setmOnItemOnClickListener(new CphmAdapter.OnItemOnClickListener() {
            @Override
            public void onItemLongClock(View view, int pos) {
                Log.d("WWW", pos + "");
            }
            @Override
            public void onItemDetailBtnClick(View view, String cphm) {
                DbHepler.open();
                String record = DbHepler.queryRecordByCarNum(cphm);
                DbHepler.close();
                String[] splitZp= record.split("\\|");
                CpzpAdapter cpzpAdapter = new CpzpAdapter(splitZp);
                cpzpAdapter.setScrollTargetPositionListener(new CpzpAdapter.OnScrollTargetPositionListener() {
                    @Override
                    public void scrollToPosition(int postion) {
                        recyclerView_cpzp.scrollToPosition(postion);
                    }
                });
                cpzpAdapter.setmOnItemOnClickListener(new CpzpAdapter.OnItemOnClickListener() {
                    @Override
                    public void onItemLongClock(View view, int pos) {
                        Log.d("WWWwww", pos + "");
                    }

                    @Override
                    public void onItemDetailBtnClick(View view, String cpzp) {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(cpzp);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Bitmap bitmap  = BitmapFactory.decodeStream(fis);
                        iv_cp.setVisibility(View.VISIBLE);
                        iv_cp.setImageBitmap(bitmap);
                    }
                });
                recyclerView_cpzp.setAdapter(cpzpAdapter);
                cpzpAdapter.add_all_zp(splitZp);
                iv_cp.setVisibility(View.INVISIBLE);
            }
        });

        recyclerView_cphm.setAdapter(cphmAdapter);
        cphmAdapter.add_all_hphm(cphm);
    }
}
