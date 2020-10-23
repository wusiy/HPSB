package com.sunland.cpocr.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunland.cpocr.R;
import com.sunland.cpocr.activity.adapter.CphmAdapter;
import com.sunland.cpocr.activity.adapter.TrackAdapter;
import com.sunland.cpocr.db.DbTracks;
import com.sunland.cpocr.path_record.record.PathRecord;
import com.sunland.cpocr.utils.CpocrUtils;
import com.sunland.cpocr.utils.DialogHelp;

import java.io.File;
import java.util.List;

/**
 * 所有轨迹list展示activity
 *
 */
public class TrackRecordActivity extends AppCompatActivity {
    private RecyclerView recyclerView_track;
    private TrackAdapter trackAdapter;
    private DbTracks DbHepler;
    private List<PathRecord> trackAll;
    public static final String RECORD_DATE = "record_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_record);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        recyclerView_track = findViewById(R.id.recycler_tracklist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_track.setLayoutManager(layoutManager);


        DbHepler = new DbTracks(this);
        DbHepler.open();
        trackAll = DbHepler.queryRecordAll();
        DbHepler.close();
        trackAdapter = new TrackAdapter(trackAll);
        trackAdapter.setScrollTargetPositionListener(new TrackAdapter.OnScrollTargetPositionListener() {
            @Override
            public void scrollToPosition(int postion) {
                recyclerView_track.scrollToPosition(postion);
            }
        });
        trackAdapter.setmOnItemOnClickListener(new TrackAdapter.OnItemOnClickListener() {
            @Override
            public void onItemLongClock(View view, PathRecord track, int pos) {
                        deleteOneTrack(track);
            }

            @Override
            public void onItemDetailBtnClick(View view, PathRecord track, int position) {
                		Intent intent = new Intent(TrackRecordActivity.this, TrackShowActivity.class);
		                intent.putExtra(RECORD_DATE, track.getDate());
		                startActivity(intent);
            }
        });
        recyclerView_track.setAdapter(trackAdapter);
        trackAdapter.add_all_track(trackAll);
    }



    private void deleteOneTrack(PathRecord track){
        AlertDialog.Builder builder = DialogHelp.getConfirmDialog(TrackRecordActivity.this,
                " 是否删除 " + track.getDate() + " 的巡逻记录", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean deleted = false;
                        DbHepler.open();
                        deleted = DbHepler.deleteOneTrack(track.getDate());
                        if(deleted){
                            //更新车牌列表
                            trackAll = DbHepler.queryRecordAll();
                            for (int i = 0; i < trackAll.size() + 1; i++) {
                                trackAdapter.notifyItemRemoved(0);
                                trackAdapter.notifyDataSetChanged();
                            }
                            trackAdapter.add_all_track(trackAll);
                            Toast.makeText(com.sunland.cpocr.activity.TrackRecordActivity.this,
                                    track.getDate() + " 的巡逻轨迹已删除",Toast.LENGTH_LONG).show();
                        } else{
                            Toast.makeText(com.sunland.cpocr.activity.TrackRecordActivity.this,track.getDate() + " 轨迹删除出错",Toast.LENGTH_LONG).show();
                        }
                        DbHepler.close();
                    }
                });
        builder.show();



    }


    public void onBackClick(View view) {
		this.finish();
	}

}
