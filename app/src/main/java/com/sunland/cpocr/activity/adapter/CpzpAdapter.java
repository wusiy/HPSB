package com.sunland.cpocr.activity.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sunland.cpocr.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class CpzpAdapter extends RecyclerView.Adapter<CpzpAdapter.ViewHolder> {

    private int selected_position = -1, cancel_selected_position = -1;
    private Drawable selected_drawable, empty_drawable;
    private String[] mData = null;
    private OnScrollTargetPositionListener listener;
    private List<String> mContext;
    private OnItemOnClickListener mOnItemOnClickListener;

    public CpzpAdapter(String [] mContext) {
        this.mContext = Arrays.asList(mContext);
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
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_cpzp, viewGroup, false);
        ViewHolder vh = new ViewHolder(root);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        final String info = mData[position];
        if (info == null) {
            return;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(info);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap  = BitmapFactory.decodeStream(fis);
        viewHolder.ivCpzp.setImageBitmap(bitmap);
        if(position == cancel_selected_position){
            viewHolder.ivCpzp.setForeground(empty_drawable);
            cancel_selected_position = -1;
        }

        if(position == selected_position){
            viewHolder.ivCpzp.setForeground(selected_drawable);
        }

        if (mOnItemOnClickListener != null) {
            int finalPosition1 = position;
            viewHolder.itemView.setOnLongClickListener(view -> {
                mOnItemOnClickListener.onItemLongClock(viewHolder.itemView, finalPosition1);
                return false;
            });

            viewHolder.itemView.setOnClickListener(view -> {
                mOnItemOnClickListener.onItemDetailBtnClick(viewHolder.itemView, info, finalPosition1);
            });
        }
    }

    /**
     * 增加一张车牌图片显示
     *
     * @param zp    车牌照片URL
   */
    public void add_one_zp(String zp) {
            notifyItemInserted(getItemCount() - 1);
//            if (listener != null)
//                listener.scrollToPosition(getItemCount() - 1);
    }

    /**
     * 增加多张车牌照片
     *
     * @param zplist    照片url list
     */
    public void add_all_zp(String[] zplist) {
        if(mData!= null) {
            for (int i = 0; i < getItemCount(); i++) {
                notifyItemRemoved(0);
                notifyDataSetChanged();
            }
        }

        mData = zplist;
        notifyDataSetChanged();
        for(int i = 0; i < zplist.length; i ++){
            add_one_zp(zplist[i]);
        }
    }

    /**
     * 清空照片列表
     *
     */
    public void clear_all() {
        if(mData!= null) {
            for (int i = 0; i < getItemCount(); i++) {
                notifyItemRemoved(0);
                notifyDataSetChanged();
            }
        }
        mData = null;
        notifyDataSetChanged();
    }

    public void select_one_zp(int position, Drawable grayCover, Drawable emptyCover){
        selected_drawable = grayCover;
        empty_drawable = emptyCover;
        if(selected_position != -1){
            cancel_selected_position = selected_position;
            notifyItemChanged(cancel_selected_position);
        }
        selected_position = position;
        notifyItemChanged(selected_position);
    }

    @Override
    public int getItemCount () {
        return mData.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCpzp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCpzp = (ImageView) itemView.findViewById(R.id.iv_zp);
        }
    }

    public interface OnItemOnClickListener {
        void onItemLongClock(View view, int pos);

        void onItemDetailBtnClick(View view, String cpzp, int position);
    }

}