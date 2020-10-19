package com.sunland.cpocr.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.sunland.cpocr.R;
import com.sunland.cpocr.activity.adapter.RecordAdapter;
import com.sunland.cpocr.db.DbTracks;
import com.sunland.cpocr.path_record.record.PathRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有轨迹list展示activity
 *
 */
public class TrackRecordActivity extends Activity implements OnItemClickListener {

	private RecordAdapter mAdapter;
	private ListView mAllRecordListView;
	private DbTracks mDataBaseHelper;
	private List<PathRecord> mAllRecord = new ArrayList<PathRecord>();
	public static final String RECORD_ID = "record_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record_list);
		mAllRecordListView = findViewById(R.id.recordlist);
		mDataBaseHelper = new DbTracks(this);
		mDataBaseHelper.open();
		searchAllRecordFromDB();
		mAdapter = new RecordAdapter(this, mAllRecord);
		mAllRecordListView.setAdapter(mAdapter);
		mAllRecordListView.setOnItemClickListener(this);
	}

	private void searchAllRecordFromDB() {
		mAllRecord = mDataBaseHelper.queryRecordAll();
	}

	public void onBackClick(View view) {
		this.finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		PathRecord recorditem = (PathRecord) parent.getAdapter().getItem(
				position);
		Intent intent = new Intent(TrackRecordActivity.this,
				RecordShowActivity.class);
		intent.putExtra(RECORD_ID, recorditem.getId());
		startActivity(intent);
	}
}
