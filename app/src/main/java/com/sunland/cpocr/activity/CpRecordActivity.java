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
import android.widget.Toast;

import com.sunland.cpocr.R;
import com.sunland.cpocr.activity.adapter.CphmAdapter;
import com.sunland.cpocr.activity.adapter.CpzpAdapter;
import com.sunland.cpocr.db.DbCpHmZp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class CpRecordActivity extends AppCompatActivity {

    private RecyclerView recyclerView_cphm;
    private RecyclerView recyclerView_cpzp;
    private ImageView   iv_cp;
    private CphmAdapter cphmAdapter;
    private CpzpAdapter cpzpAdapter;
    private DbCpHmZp DbHepler;
    private List<String> cphmAll;
    //正在图片展示的车牌号码
    private String showingCphm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp_records);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        iv_cp = findViewById(R.id.iv_cp);
        recyclerView_cphm = findViewById(R.id.recycler_cplist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_cphm.setLayoutManager(layoutManager);
        recyclerView_cpzp = findViewById(R.id.recycler_cpphotolist);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        recyclerView_cpzp.setLayoutManager(layoutManager1);

        DbHepler = new DbCpHmZp(this);
        DbHepler.open();
        cphmAll = DbHepler.queryAllCarNum();
        DbHepler.close();

        cphmAdapter = new CphmAdapter(cphmAll);
        cphmAdapter.setScrollTargetPositionListener(new CphmAdapter.OnScrollTargetPositionListener() {
            @Override
            public void scrollToPosition(int postion) {
                recyclerView_cphm.scrollToPosition(postion);
            }
        });
        cphmAdapter.setmOnItemOnClickListener(new CphmAdapter.OnItemOnClickListener() {
            @Override
            public void onItemLongClock(View view, String cphm, int pos) {
                deleteOneCp(cphm);
            }
            @Override
            public void onItemDetailBtnClick(View view, String cphm) {
                showingCphm = cphm;
                DbHepler.open();
                String record = DbHepler.queryRecordByCarNum(cphm);
                DbHepler.close();
                String[] splitZp= record.split("\\|");
                cpzpAdapter = new CpzpAdapter(splitZp);
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
        cphmAdapter.add_all_hphm(cphmAll);
    }

    private void deleteOneCp(String cphm){
        boolean deleted = false;
        DbHepler.open();
        String record = DbHepler.queryRecordByCarNum(cphm);
        String[] splitZp= record.split("\\|");
        //删除相关照片文件
        for (String s : splitZp) {
            File file = new File(s);
            if (file.isFile() && file.exists()) {
                deleted = file.delete();
                if (!deleted) {
                    break;
                }
            }
        }
        if(deleted){
            deleted = DbHepler.deleteOneCpHmZp(cphm);
        }
        if(deleted){
            //更新车牌列表
            cphmAll = DbHepler.queryAllCarNum();
            DbHepler.close();
            for (int i = 0; i < cphmAll.size() + 1; i++) {
                cphmAdapter.notifyItemRemoved(0);
                cphmAdapter.notifyDataSetChanged();
            }
            cphmAdapter.add_all_hphm(cphmAll);
            //若被删除的车牌照片正在展示，则清空展示的照片
            if(showingCphm != null && showingCphm.equals(cphm)){
                iv_cp.setVisibility(View.INVISIBLE);
                for (int i = 0; i < splitZp.length; i++) {
                    cpzpAdapter.notifyItemRemoved(0);
                    cpzpAdapter.notifyDataSetChanged();
                }
            }
            Toast.makeText(this,cphm + " 相关数据已删除",Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(this,cphm + " 数据删除出错",Toast.LENGTH_LONG).show();
        }
    }
}
