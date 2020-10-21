package com.sunland.cpocr.activity;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
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
import com.sunland.cpocr.utils.CpocrUtils;
import com.sunland.cpocr.utils.DialogHelp;

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
    private Drawable grayCover, emptyCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cp_records);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Resources resources = getResources();
        grayCover = resources.getDrawable(R.drawable.gray);
        grayCover.setAlpha(80);
        emptyCover = resources.getDrawable(R.drawable.empty);

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
        DbHepler.open();
        cphmAdapter = new CphmAdapter(cphmAll);
        cphmAdapter.setScrollTargetPositionListener(new CphmAdapter.OnScrollTargetPositionListener() {
            @Override
            public void scrollToPosition(int postion) {
                recyclerView_cphm.scrollToPosition(postion);
            }
        });
        cphmAdapter.setmOnItemOnClickListener(new CphmAdapter.OnItemOnClickListener() {
            @Override
            public void onItemLongClock(View view, String cphmzl, int pos) {
                deleteOneCp(cphmzl.split("\\|")[0], cphmzl.split("\\|")[1]);
            }
            @Override
            public void onItemDetailBtnClick(View view, String cphmzl, int position) {
                cphmAdapter.select_one_cp(position, grayCover, emptyCover);
                showingCphm = cphmzl;
                String[] splitCphmzl= cphmzl.split("\\|");
                DbHepler.open();
                String record = DbHepler.queryRecordByCarNum(splitCphmzl[0],splitCphmzl[1]);
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
                    public void onItemDetailBtnClick(View view, String cpzp, int position) {
                        cpzpAdapter.select_one_zp(position, grayCover, emptyCover);
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

    private void deleteOneCp(String cphm, String cpzl){
        AlertDialog.Builder builder = DialogHelp.getConfirmDialog(CpRecordActivity.this,
                " 是否删除 " + CpocrUtils.getHpzlFromOcr(cphm, cpzl)[1] + " " + cphm + " 的所有数据", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean deleted = false;
                        DbHepler.open();
                        String record = DbHepler.queryRecordByCarNum(cphm, cpzl);
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
                            deleted = DbHepler.deleteOneCpHmZp(cphm, cpzl);
                        }
                        if(deleted){
                            //更新车牌列表
                            cphmAll = DbHepler.queryAllCarNum();
                            for (int i = 0; i < cphmAll.size() + 1; i++) {
                                cphmAdapter.notifyItemRemoved(0);
                                cphmAdapter.notifyDataSetChanged();
                            }
                            cphmAdapter.add_all_hphm(cphmAll);
                            //若被删除的车牌照片正在展示，则清空展示的照片
                            if(showingCphm != null && showingCphm.equals(cphm + "|" + cpzl)){
                                iv_cp.setVisibility(View.INVISIBLE);
                                for (int i = 0; i < splitZp.length; i++) {
                                    cpzpAdapter.notifyItemRemoved(0);
                                    cpzpAdapter.notifyDataSetChanged();
                                }
                                cpzpAdapter.notifyDataSetChanged();
                            }
                            Toast.makeText(com.sunland.cpocr.activity.CpRecordActivity.this,
                                    CpocrUtils.getHpzlFromOcr(cphm, cpzl)[1] + " " + cphm + " 的相关数据已删除",Toast.LENGTH_LONG).show();
                        } else{
                            Toast.makeText(com.sunland.cpocr.activity.CpRecordActivity.this,cphm + " 数据删除出错",Toast.LENGTH_LONG).show();
                        }
                        DbHepler.close();
                    }
                });
        builder.show();

    }
}
