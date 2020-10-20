package com.sunland.cpocr.activity.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sunland.cpocr.R;
import com.sunland.cpocr.path_record.record.PathRecord;


public class RecordAdapter extends BaseAdapter {

	private Context mContext;
	private List<PathRecord> mRecordList;

	public RecordAdapter(Context context, List<PathRecord> list) {
		this.mContext = context;
		this.mRecordList = list;
	}

	@Override
	public int getCount() {
		return mRecordList.size();
	}

	@Override
	public Object getItem(int position) {
		return mRecordList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.record_item, null);
			holder.date = convertView.findViewById(R.id.date);
			holder.record = convertView.findViewById(R.id.record);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PathRecord item = mRecordList.get(position);
		holder.date.setText(item.getDate());
		holder.record.setText(item.toString());
		return convertView;
	}

	private class ViewHolder {
		TextView date;
		TextView record;
	}
}
