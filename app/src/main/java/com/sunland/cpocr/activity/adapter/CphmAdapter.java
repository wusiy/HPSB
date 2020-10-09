package com.sunland.cpocr.activity.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.sunland.cpocr.R;

import java.util.ArrayList;
import java.util.List;

public class CphmAdapter extends RecyclerView.Adapter<CphmAdapter.ViewHolder> {

    private List<String> mData = new ArrayList<>();
    private OnScrollTargetPositionListener listener;
    private List<String> mContext;
    private OnItemOnClickListener mOnItemOnClickListener;

    public CphmAdapter(List<String> mContext) {
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
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_cphm_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(root);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        final String info = mData.get(position);
        if (info == null) {
            return;
        }

        viewHolder.tvCphm.setText(info);

            if (mOnItemOnClickListener != null) {
                int finalPosition1 = position;
                viewHolder.itemView.setOnLongClickListener(view -> {
                    mOnItemOnClickListener.onItemLongClock(viewHolder.itemView, finalPosition1);
                    return false;
                });

                viewHolder.itemView.setOnClickListener(view -> {
                    mOnItemOnClickListener.onItemDetailBtnClick(viewHolder.itemView, info);
                });
            }


    }

            /**
             * 删除一条信息
             *
             * @param pos
             */
            public void removeItem ( int pos){
                mData.remove(pos);
                notifyDataSetChanged();
                notifyItemRemoved(pos);
            }

    /**
     * 增加一条车牌号码
     *
     * @param hphm    号牌号码
     */
    public void add_one_hphm(String hphm) {

            mData.add(hphm);
            notifyItemInserted(getItemCount() - 1);
            if (listener != null)
                listener.scrollToPosition(getItemCount() - 1);

    }

    /**
     * 增加多条车牌号码
     *
     * @param hphm    号牌号码
     */
    public void add_all_hphm(List<String> hphm) {
        for(int i = 0; i < hphm.size(); i ++){
            add_one_hphm(hphm.get(i));
        }
    }

            @Override
            public int getItemCount () {
                return mData.size();
            }

            static class ViewHolder extends RecyclerView.ViewHolder {
                public TextView tvCphm;
                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                    tvCphm = (TextView) itemView.findViewById(R.id.tv_cphm);

                }
            }

            public interface OnItemOnClickListener {
                void onItemLongClock(View view, int pos);
                void onItemDetailBtnClick(View view, String cphm);
            }
}