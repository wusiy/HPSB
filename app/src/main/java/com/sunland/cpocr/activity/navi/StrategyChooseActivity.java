package com.sunland.cpocr.activity.navi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;


import com.sunland.cpocr.R;
import com.sunland.cpocr.activity.adapter.StrategyChooseAdapter;
import com.sunland.cpocr.bean.StrategyStateBean;
import com.sunland.cpocr.utils.GdNaviUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 驾车偏好设置
 */
public class StrategyChooseActivity extends Activity implements View.OnClickListener {
    private boolean congestion, cost, hightspeed, avoidhightspeed;
    List<StrategyStateBean> mStrategys = new ArrayList<StrategyStateBean>();
    private ListView mStrategyChooseListView;
    private StrategyChooseAdapter mStrategyAdapter;
    private LinearLayout mBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityIntent();
        mStrategys.add(new StrategyStateBean(GdNaviUtils.AVOID_CONGESTION, congestion));
        mStrategys.add(new StrategyStateBean(GdNaviUtils.AVOID_COST, cost));
        mStrategys.add(new StrategyStateBean(GdNaviUtils.AVOID_HIGHSPEED, avoidhightspeed));
        mStrategys.add(new StrategyStateBean(GdNaviUtils.PRIORITY_HIGHSPEED, hightspeed));
        setContentView(R.layout.activity_strategy_choose);
        mStrategyAdapter = new StrategyChooseAdapter(this.getApplicationContext(), mStrategys);
        mStrategyChooseListView = (ListView) findViewById(R.id.strategy_choose_list);
        mStrategyChooseListView.setAdapter(mStrategyAdapter);
        mBackLayout = (LinearLayout) findViewById(R.id.title_lly_back);
        mBackLayout.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResultIntent();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setResultIntent(){
        Intent intent = new Intent();
        for (StrategyStateBean bean : mStrategys) {
            if (bean.getStrategyCode() == GdNaviUtils.AVOID_CONGESTION) {
                intent.putExtra(GdNaviUtils.INTENT_NAME_AVOID_CONGESTION, bean.isOpen());
            }

            if (bean.getStrategyCode() == GdNaviUtils.AVOID_COST) {
                intent.putExtra(GdNaviUtils.INTENT_NAME_AVOID_COST, bean.isOpen());
            }

            if (bean.getStrategyCode() == GdNaviUtils.AVOID_HIGHSPEED) {
                intent.putExtra(GdNaviUtils.INTENT_NAME_AVOID_HIGHSPEED, bean.isOpen());
            }

            if (bean.getStrategyCode() == GdNaviUtils.PRIORITY_HIGHSPEED) {
                intent.putExtra(GdNaviUtils.INTENT_NAME_PRIORITY_HIGHSPEED, bean.isOpen());
            }
        }
        this.setResult(GdNaviUtils.ACTIVITY_RESULT_CODE, intent);
    }

    private void getActivityIntent() {
        Intent intent = this.getIntent();
        if (intent == null) {
            return;
        }
        congestion = intent.getBooleanExtra(GdNaviUtils.INTENT_NAME_AVOID_CONGESTION, false);
        cost = intent.getBooleanExtra(GdNaviUtils.INTENT_NAME_AVOID_COST, false);
        avoidhightspeed = intent.getBooleanExtra(GdNaviUtils.INTENT_NAME_AVOID_HIGHSPEED, false);
        hightspeed = intent.getBooleanExtra(GdNaviUtils.INTENT_NAME_PRIORITY_HIGHSPEED, false);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.title_lly_back){
            setResultIntent();
            this.finish();
        }
    }
}
