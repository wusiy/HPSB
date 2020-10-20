package com.sunland.cpocr.activity.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.sunland.cpocr.R;
import com.sunland.cpocr.utils.CpocrUtils;

import java.util.ArrayList;
import java.util.List;

public class CphmAdapter extends RecyclerView.Adapter<CphmAdapter.ViewHolder> {

    private List<String> mData = new ArrayList<>();
    private OnScrollTargetPositionListener listener;
    private int selected_position = -1, cancel_selected_position = -1;
    private Drawable selected_drawable, empty_drawable;
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

        String[] splitCpInfo= info.split("\\|");
        viewHolder.tvCphm.setText(splitCpInfo[0]);
        viewHolder.tvCpzl.setText(CpocrUtils.getHpzlFromOcr(splitCpInfo[0], splitCpInfo[1])[1]);

        if(position == cancel_selected_position){
            viewHolder.tvCpzl.setForeground(empty_drawable);
            cancel_selected_position = -1;

        }
        if(position == selected_position){
            viewHolder.tvCpzl.setForeground(selected_drawable);
        }

        String cpzl = CpocrUtils.getHpzlFromOcr(splitCpInfo[0], splitCpInfo[1])[0];
        if (cpzl.equals("01") || splitCpInfo[1].equals("07") || cpzl.equals("15")
                || cpzl.equals("16") || cpzl.equals("17")) {
            // 黄底黑字：大型汽车，普通摩托车，挂车，教练汽车，教练摩托车
            viewHolder.tvCphm.setBackgroundColor(Color.parseColor("#FEBD02"));//Color.YELLOW
            viewHolder.tvCphm.setTextColor(Color.BLACK);
        } else if (cpzl.equals("02") || cpzl.equals("08")) {// 蓝底白字：小型汽车，轻便摩托车
            viewHolder.tvCphm.setBackgroundColor(Color.parseColor("#111183"));//Color.BLUE
            viewHolder.tvCphm.setTextColor(Color.WHITE);
        } else if (cpzl.equals("22") || cpzl.equals("23") || cpzl.equals("24")) {// 白底黑字红“警”：临时行驶车，警用汽车，警用摩托
            viewHolder.tvCphm.setBackgroundColor(Color.WHITE);
            viewHolder.tvCphm.setTextColor(Color.BLACK);
        } else if (cpzl.equals("20") || cpzl.equals("21")) {// 白底红字：临时入境汽车，临时入境摩托车
            viewHolder.tvCphm.setBackgroundColor(Color.WHITE);
            viewHolder.tvCphm.setTextColor(Color.RED);
        } else if (cpzl.equals("03") || cpzl.equals("04") || cpzl.equals("05") || cpzl.equals("06")
                || cpzl.equals("09") || cpzl.equals("10") || cpzl.equals("11") || cpzl.equals("12")
                || cpzl.equals("26") || cpzl.equals("27")) {
            // 黑底白字 ：使馆汽车，领馆汽车，境外汽车，外籍汽车，使馆摩托车，领馆摩托车，境外摩托车，外籍摩托车，香港入出境车，澳门入出境车
            viewHolder.tvCphm.setBackgroundColor(Color.BLACK);
            viewHolder.tvCphm.setTextColor(Color.WHITE);
        } else if (cpzl.equals("51")) {// 浅绿底黑字：大型新能源汽车（头黄底）
            viewHolder.tvCphm.setBackgroundColor(Color.parseColor("#84BD7A"));
            viewHolder.tvCphm.setTextColor(Color.BLACK);
        } else if (cpzl.equals("52")) {// 浅绿底黑字：小型新能源汽车
            viewHolder.tvCphm.setBackgroundColor(Color.parseColor("#BCDAAB"));
            viewHolder.tvCphm.setTextColor(Color.BLACK);
        } else {
            viewHolder.tvCphm.setTextColor(Color.BLACK);
            viewHolder.tvCphm.setBackgroundColor(Color.WHITE);
        }

            if (mOnItemOnClickListener != null) {
                int finalPosition1 = position;
                viewHolder.itemView.setOnLongClickListener(view -> {
                    mOnItemOnClickListener.onItemLongClock(viewHolder.itemView, info, finalPosition1);
                    return false;
                });
                viewHolder.itemView.setOnClickListener(view -> {
                    mOnItemOnClickListener.onItemDetailBtnClick(viewHolder.itemView, info, finalPosition1);
                });
            }
    }

    /**
     * 增加一条车牌号码
     *
     * @param hphmzl    号牌号码及种类
     */
    public void add_one_hphm(String hphmzl) {
            mData.add(hphmzl);
            notifyItemInserted(getItemCount() - 1);
            if (listener != null)
                listener.scrollToPosition(getItemCount() - 1);
    }

    /**
     * 增加多条车牌号码
     *
     * @param hphmzl    号牌号码及种类， "|"分割
     */
    public void add_all_hphm(List<String> hphmzl) {
        if(mData != null) {
            for (int i = 0; i < getItemCount(); i++) {
                notifyItemRemoved(0);
                notifyDataSetChanged();
            }
            mData.clear();
        }
        for(int i = 0; i < hphmzl.size(); i ++){
            add_one_hphm(hphmzl.get(i));
        }
    }

    public void select_one_cp(int position,  Drawable grayCover, Drawable emptyCover){
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
        return mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCphm, tvCpzl;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCphm = itemView.findViewById(R.id.tv_cphm);
            tvCpzl = itemView.findViewById(R.id.tv_hpzl);
        }
    }

    public interface OnItemOnClickListener {
        void onItemLongClock(View view, String cphmzl, int pos);
        void onItemDetailBtnClick(View view, String cphmzl, int position);
    }
}