package com.sunland.cpocr.activity.adapter;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sunland.cpocr.R;
import com.sunland.cpocr.bean.Info;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class OcrInfoListAdapter extends RecyclerView.Adapter<OcrInfoListAdapter.ViewHolder> {

    private List<Info> mData = new ArrayList<>();
    private OnScrollTargetPositionListener listener;
    private Context mContext;
    private OnItemOnClickListener mOnItemOnClickListener;

    public OcrInfoListAdapter(Context mContext) {
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
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_info_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(root);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        position =  viewHolder.getAdapterPosition();
        final Info info = mData.get(position);
        if (info == null) {
            return;
        }
        viewHolder.tvVehCsys.setVisibility(View.VISIBLE);

        viewHolder.tvHphm.setText(info.getHphm());
        viewHolder.tvHpzl.setText(info.getHpzlStr());
        viewHolder.tvHpzl.setTag(info.getHpzl());
        if (info.getHpzl().equals("01") || info.getHpzl().equals("07") || info.getHpzl().equals("15")
                || info.getHpzl().equals("16") || info.getHpzl().equals("17")) {
            // 黄底黑字：大型汽车，普通摩托车，挂车，教练汽车，教练摩托车
            viewHolder.tvHphm.setBackgroundColor(Color.parseColor("#FEBD02"));//Color.YELLOW
            viewHolder.tvHphm.setTextColor(Color.BLACK);
        } else if (info.getHpzl().equals("02") || info.getHpzl().equals("08")) {// 蓝底白字：小型汽车，轻便摩托车
            viewHolder.tvHphm.setBackgroundColor(Color.parseColor("#111183"));//Color.BLUE
            viewHolder.tvHphm.setTextColor(Color.WHITE);
        } else if (info.getHpzl().equals("22") || info.getHpzl().equals("23") || info.getHpzl().equals("24")) {// 白底黑字红“警”：临时行驶车，警用汽车，警用摩托
            viewHolder.tvHphm.setBackgroundColor(Color.WHITE);
            viewHolder.tvHphm.setTextColor(Color.BLACK);
        } else if (info.getHpzl().equals("20") || info.getHpzl().equals("21")) {// 白底红字：临时入境汽车，临时入境摩托车
            viewHolder.tvHphm.setBackgroundColor(Color.WHITE);
            viewHolder.tvHphm.setTextColor(Color.RED);
        } else if (info.getHpzl().equals("03") || info.getHpzl().equals("04") || info.getHpzl().equals("05") || info.getHpzl().equals("06")
                || info.getHpzl().equals("09") || info.getHpzl().equals("10") || info.getHpzl().equals("11") || info.getHpzl().equals("12")
                || info.getHpzl().equals("26") || info.getHpzl().equals("27")) {
            // 黑底白字 ：使馆汽车，领馆汽车，境外汽车，外籍汽车，使馆摩托车，领馆摩托车，境外摩托车，外籍摩托车，香港入出境车，澳门入出境车
            viewHolder.tvHphm.setBackgroundColor(Color.BLACK);
            viewHolder.tvHphm.setTextColor(Color.WHITE);
        } else if (info.getHpzl().equals("51")) {// 浅绿底黑字：大型新能源汽车（头黄底）
            viewHolder.tvHphm.setBackgroundColor(Color.parseColor("#84BD7A"));
            viewHolder.tvHphm.setTextColor(Color.BLACK);
        } else if (info.getHpzl().equals("52")) {// 浅绿底黑字：小型新能源汽车
            viewHolder.tvHphm.setBackgroundColor(Color.parseColor("#BCDAAB"));
            viewHolder.tvHphm.setTextColor(Color.BLACK);
        } else {
            viewHolder.tvHphm.setTextColor(Color.BLACK);
            viewHolder.tvHphm.setBackgroundColor(Color.WHITE);
        }

        if (mOnItemOnClickListener != null) {
            final int finalPosition1 = position;
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnItemOnClickListener.onItemLongClock(viewHolder.itemView, finalPosition1);
                    return false;
                }
            });
        }


    }

    /**
     * 删除一条信息
     *
     * @param pos
     */
    public void removeItem(int pos) {
        mData.remove(pos);
        notifyDataSetChanged();
//        notifyItemRemoved(pos);
    }

    /**
     * 增加一条识别信息
     *
     * @param hphm    号牌号码
     * @param hpzl    号牌种类
     * @param hpzlStr 号牌种类描述
     * @param hpys    号牌颜色
     */
    public void add(String hphm, String hpzl, String hpzlStr, String hpys) {
        boolean isExist = false;
        Iterator<Info> iterator = mData.iterator();
        while (iterator.hasNext()) {
            final Info info = iterator.next();
            if (info == null)
                continue;
            if (info.getHphm().equals(hphm) && info.getHpzl().equals(hpzl)) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            Info i = new Info();
            i.setHphm(hphm);
            i.setHpzl(hpzl);
            i.setHpzlStr(hpzlStr);
            i.setHpys(hpys);
            i.setCode(1);
            i.setMessage("正在查询中...");
            mData.add(i);
            int currentPos = mData.size() - 1;
            queryVeh(currentPos, hphm, hpzl);
            notifyItemInserted(getItemCount() - 1);
            if (listener != null)
                listener.scrollToPosition(getItemCount() - 1);
        }
    }

    private void queryVeh(int pos, String hphm, String hpzl) {

//        HttpHelper.getInstance().queryVeh(mContext, pos, hpzl, hphm, new HttpHelper.OnOkhttpResultListener<ResultQueryVeh>() {
//            @Override
//            public void onSuccess(ResultQueryVeh successResult, int pos) {
//                Info info = mData.get(pos);
//                info.setQueryResult(successResult);
//                info.setCode(0);
//                info.setMessage(successResult.getMsg());
//                notifyItemChanged(pos);
//            }

//            @Override
//            public void onFailed(String resultStr, int pos) {
//                Info info = mData.get(pos);
//                info.setCode(-1);
//                info.setMessage(resultStr);
//                notifyItemChanged(pos);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvHphm;
        public TextView tvHpzl;
        public TextView tvVehCsys;


        public Button btnDetail;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHphm = (TextView) itemView.findViewById(R.id.tv_hphm);
            tvHpzl = (TextView) itemView.findViewById(R.id.tv_hpzl);
            tvVehCsys = (TextView) itemView.findViewById(R.id.tv_csys);
        }
    }

    public interface OnItemOnClickListener {
        void onItemLongClock(View view, int pos);

        void onItemDetailBtnClick(View view, Info info);
    }
}
