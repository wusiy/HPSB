package com.sunland.cpocr.activity.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunland.cpocr.R;
import com.sunland.cpocr.path_record.record.PathRecord;

import java.util.ArrayList;
import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder> {

    private List<PathRecord> mData = new ArrayList<>();
    private OnScrollTargetPositionListener listener;
    private List<PathRecord> mContext;
    private OnItemOnClickListener mOnItemOnClickListener;

    public TrackAdapter(List<PathRecord> mContext) {
        this.mContext = mContext;
    }

    public interface OnScrollTargetPositionListener {
        void scrollToPosition(int postion);
    }

    public void setmOnItemOnClickListener(OnItemOnClickListener mOnItemOnClickListener) {
        this.mOnItemOnClickListener = mOnItemOnClickListener;
    }

    public void setScrollTargetPositionListener(OnScrollTargetPositionListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_track, viewGroup, false);
        ViewHolder vh = new ViewHolder(root);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        final PathRecord pathRecord = mData.get(position);
        if (pathRecord == null) {
            return;
        }
        viewHolder.tvDate.setText(pathRecord.getDate());
        if(pathRecord.getStrStartPoint() != "" && pathRecord.getStrEndPoint() != null){
            viewHolder.tvStartEnd.setText(pathRecord.getStrStartPoint() + " --> " + pathRecord.getStrEndPoint());
        } else {
            viewHolder.tvStartEnd.setText("");
        }
        viewHolder.tvInfo.setText(pathRecord.toString());

        if (mOnItemOnClickListener != null) {
            int finalPosition1 = position;
            viewHolder.itemView.setOnLongClickListener(view -> {
                mOnItemOnClickListener.onItemLongClock(viewHolder.itemView, pathRecord, finalPosition1);
                return false;
            });
            viewHolder.itemView.setOnClickListener(view -> {
                mOnItemOnClickListener.onItemDetailBtnClick(viewHolder.itemView, pathRecord, finalPosition1);
            });
        }
    }

    /**
     * 增加一条轨迹记录
     *
     * @param pathRecord    轨迹记录
     */
    public void add_one_hphm(PathRecord pathRecord) {
        mData.add(pathRecord);
        notifyItemInserted(getItemCount() - 1);
//            if (listener != null)
//                listener.scrollToPosition(getItemCount() - 1);
    }

    /**
     * 添加多条巡逻轨迹记录
     *
     * @param pathRecords    多条巡逻轨迹记录
     */
    public void add_all_track(List<PathRecord> pathRecords) {
        if(mData != null) {
            for (int i = 0; i < getItemCount(); i++) {
                notifyItemRemoved(0);
                notifyDataSetChanged();
            }
            mData.clear();
        }
        for(int i = 0; i < pathRecords.size(); i ++){
            add_one_hphm(pathRecords.get(i));
        }
    }


    @Override
    public int getItemCount () {
        return mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDate, tvStartEnd, tvInfo ;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStartEnd = itemView.findViewById(R.id.tv_start_end);
            tvInfo = itemView.findViewById(R.id.tv_info);
        }
    }

    public interface OnItemOnClickListener {
        void onItemLongClock(View view, PathRecord track, int pos);
        void onItemDetailBtnClick(View view, PathRecord track, int position);
    }
}
