package com.sunland.cpocr.activity.navi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.sunland.cpocr.R;
import com.sunland.cpocr.activity.LprMapActivity;
import com.sunland.cpocr.bean.StrategyBean;
import com.sunland.cpocr.utils.GdNaviUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static com.sunland.cpocr.MainActivity.IS_TRACING_KEY;
import static com.sunland.cpocr.MainActivity.NAVI_TYPE_KEY;

/**
 * 驾车路径规划并展示对应的路线标签
 */
public class CalculateRouteActivity extends Activity implements AMapNaviListener, View.OnClickListener, AMap.OnMapLoadedListener {
    private StrategyBean mStrategyBean;
    private static final float ROUTE_UNSELECTED_TRANSPARENCY = 0.3F;
    private static final float ROUTE_SELECTED_TRANSPARENCY = 1F;

    /**
     * 导航对象(单例)
     */
    private AMapNavi mAMapNavi;
    private MapView mMapView;
    private AMap mAMap;
    private NaviLatLng endLatlng = new NaviLatLng(39.90759,116.392582);
    private NaviLatLng startLatlng = new NaviLatLng(39.993537, 116.472875);
    private List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
    /**
     * 途径点坐标集合
     */
    private List<NaviLatLng> wayList = new ArrayList<NaviLatLng>();
    /**
     * 终点坐标集合［建议就一个终点］
     */
    private List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();
    /*
     * strategyFlag转换出来的值都对应PathPlanningStrategy常量，用户也可以直接传入PathPlanningStrategy常量进行算路。
     * 如:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
     */
    int strategyFlag = 0;

    private Button mStartNaviButton;
    private LinearLayout mRouteLineLayoutOne, mRouteLinelayoutTwo, mRouteLineLayoutThree;
    private View mRouteViewOne, mRouteViewTwo, mRouteViewThree;
    private TextView mRouteTextStrategyOne, mRouteTextStrategyTwo, mRouteTextStrategyThree;
    private TextView mRouteTextTimeOne, mRouteTextTimeTwo, mRouteTextTimeThree;
    private TextView mRouteTextDistanceOne, mRouteTextDistanceTwo, mRouteTextDistanceThree;
    private TextView mCalculateRouteOverView, naviWay;
    private ImageView mImageTraffic, mImageStrategy, mImageNaviWay;
    private ProgressDialog dialog;
    private int routeID = -1;
    //是否记录巡逻轨迹
    private String istracing;
    //起末两点距离
    private double distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_route);

        Bundle bundle = this.getIntent().getExtras(); //读取intent的数据给bundle对象
        startLatlng = new NaviLatLng(bundle.getDouble("lat"),bundle.getDouble("lgt"));
        endLatlng = new NaviLatLng(bundle.getDouble("deslat"), bundle.getDouble("deslgt"));
        istracing = getIntent().getStringExtra(IS_TRACING_KEY);
        distance = getDistance(startLatlng.getLongitude(), startLatlng.getLatitude(), endLatlng.getLongitude(), endLatlng.getLatitude());
        mMapView = (MapView) findViewById(R.id.navi_view);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        initView();
        init();
        initNavi();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.calculate_route_start_navi:
                startNavi();
                break;
            case R.id.route_line_one:
                focuseRouteLine(true, false, false);
                break;
            case R.id.route_line_two:
                focuseRouteLine(false, true, false);
                break;
            case R.id.route_line_three:
                focuseRouteLine(false, false, true);
                break;
            case R.id.map_traffic:
                setTraffic();
                break;
            case R.id.strategy_choose:
                strategyChoose();
                break;
            case R.id.navi_way:
                final String items[] = {"驾车", "骑行", "步行"};
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("选择出行方式")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0){
                                    mImageStrategy.setVisibility(View.VISIBLE);
                                    mImageNaviWay.setImageResource(R.drawable.drive);
                                    naviWay.setText("驾车");
                                    calculateDriveRoute();
                                } else if(which == 1){
                                    calculateRideRoute();
                                } else if(which == 2){
                                  calculateWalkRoute();
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
            default:
                break;
        }

    }

    /**
     * 驾车路径规划计算
     */
    private void calculateDriveRoute() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("正在规划路径...");
        dialog.show();
        try {
            strategyFlag = mAMapNavi.strategyConvert(mStrategyBean.isCongestion(), mStrategyBean.isCost(), mStrategyBean.isAvoidhightspeed(), mStrategyBean.isHightspeed(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
    }

    /**
     * 骑行路径规划计算
     */
    private void calculateRideRoute() {
        if(distance < 1000) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("正在规划路径...");
            dialog.show();
            mImageStrategy.setVisibility(View.INVISIBLE);
            mImageNaviWay.setImageResource(R.drawable.ride);
            naviWay.setText("骑行");
            mAMapNavi.calculateRideRoute(startLatlng, endLatlng);
        } else{
            Toast.makeText(this,"距离超过1000KM，路线规划失败",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 步行路径规划计算
     */
    private void calculateWalkRoute() {
        if(distance < 5) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("正在规划路径...");
            dialog.show();
            mImageStrategy.setVisibility(View.INVISIBLE);
            mImageNaviWay.setImageResource(R.drawable.walk2);
            naviWay.setText("步行");
            mAMapNavi.calculateWalkRoute(startLatlng, endLatlng);
        } else{
            Toast.makeText(this,"距离超过5KM，路线规划失败",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 接收驾车偏好设置项
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GdNaviUtils.ACTIVITY_RESULT_CODE == resultCode) {
            boolean congestion = data.getBooleanExtra(GdNaviUtils.INTENT_NAME_AVOID_CONGESTION, false);
            mStrategyBean.setCongestion(congestion);
            boolean cost = data.getBooleanExtra(GdNaviUtils.INTENT_NAME_AVOID_COST, false);
            mStrategyBean.setCost(cost);
            boolean avoidhightspeed = data.getBooleanExtra(GdNaviUtils.INTENT_NAME_AVOID_HIGHSPEED, false);
            mStrategyBean.setAvoidhightspeed(avoidhightspeed);
            boolean hightspeed = data.getBooleanExtra(GdNaviUtils.INTENT_NAME_PRIORITY_HIGHSPEED, false);
            mStrategyBean.setHightspeed(hightspeed);
            calculateDriveRoute();
        }
    }

    /**
     * 导航初始化
     */
    private void initNavi() {
        mStrategyBean = new StrategyBean(false, false, false, false);
        startList.add(startLatlng);
        endList.add(endLatlng);
        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
    }

    private void initView() {
        mStartNaviButton = findViewById(R.id.calculate_route_start_navi);
        mStartNaviButton.setOnClickListener(this);

        mImageTraffic = findViewById(R.id.map_traffic);
        mImageTraffic.setOnClickListener(this);
        mImageStrategy = findViewById(R.id.strategy_choose);
        mImageStrategy.setOnClickListener(this);
        mImageNaviWay = findViewById(R.id.navi_way);
        mImageNaviWay.setOnClickListener(this);

        naviWay = findViewById(R.id.navi_way_text);
        mCalculateRouteOverView = findViewById(R.id.calculate_route_navi_overview);

        mRouteLineLayoutOne = findViewById(R.id.route_line_one);
        mRouteLineLayoutOne.setOnClickListener(this);
        mRouteLinelayoutTwo = findViewById(R.id.route_line_two);
        mRouteLinelayoutTwo.setOnClickListener(this);
        mRouteLineLayoutThree = findViewById(R.id.route_line_three);
        mRouteLineLayoutThree.setOnClickListener(this);

        mRouteViewOne = findViewById(R.id.route_line_one_view);
        mRouteViewTwo = findViewById(R.id.route_line_two_view);
        mRouteViewThree = findViewById(R.id.route_line_three_view);

        mRouteTextStrategyOne = findViewById(R.id.route_line_one_strategy);
        mRouteTextStrategyTwo = findViewById(R.id.route_line_two_strategy);
        mRouteTextStrategyThree = findViewById(R.id.route_line_three_strategy);

        mRouteTextTimeOne =  findViewById(R.id.route_line_one_time);
        mRouteTextTimeTwo = findViewById(R.id.route_line_two_time);
        mRouteTextTimeThree = findViewById(R.id.route_line_three_time);

        mRouteTextDistanceOne = findViewById(R.id.route_line_one_distance);
        mRouteTextDistanceTwo = findViewById(R.id.route_line_two_distance);
        mRouteTextDistanceThree = findViewById(R.id.route_line_three_distance);
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            mAMap.setTrafficEnabled(false);
            mAMap.setOnMapLoadedListener(this);
            mImageTraffic.setImageResource(R.drawable.map_traffic_white);
            UiSettings uiSettings = mAMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(false);
        }
    }

    /**
     * 绘制路径规划结果
     *
     * @param routeId 路径规划线路ID
     * @param path    AMapNaviPath
     */
    private void drawRoutes(int routeId, AMapNaviPath path) {
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAMap, path, this);
            routeOverLay.setWidth(60f);

        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);
        dialog.dismiss();
    }

    /**
     * 开始导航
     */
    private void startNavi() {
        if (routeID != -1){
            mAMapNavi.selectRouteId(routeID);
            Intent intent = new Intent(CalculateRouteActivity.this, LprMapActivity.class);
            final String items[] = {"模拟导航", "实时导航"};
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("选择导航方式")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0){
                                intent.putExtra(NAVI_TYPE_KEY, "false");
                                intent.putExtra(IS_TRACING_KEY, istracing);
                                startActivity(intent);
                            } else if(which == 1){
                                intent.putExtra(NAVI_TYPE_KEY, "true");
                                intent.putExtra(IS_TRACING_KEY, istracing);
                                startActivity(intent);
                            }
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();
        } else{
            Intent intent = new Intent();
            intent.putExtra(NAVI_TYPE_KEY, "cancel_cal");
            startActivity(intent);
        }
    }

    /**
     * 路线tag选中设置
     *
     * @param lineOne
     * @param lineTwo
     * @param lineThree
     */
    private void focuseRouteLine(boolean lineOne, boolean lineTwo, boolean lineThree) {
        Log.d("LG", "lineOne:" + lineOne + " lineTwo:" + lineTwo + " lineThree:" + lineThree);
        setLinelayoutOne(lineOne);
        setLinelayoutTwo(lineTwo);
        setLinelayoutThree(lineThree);
    }

    /**
     * 地图实时交通开关
     */
    private void setTraffic() {
        if (mAMap.isTrafficEnabled()) {
            mImageTraffic.setImageResource(R.drawable.map_traffic_white);
            mAMap.setTrafficEnabled(false);
        } else {
            mImageTraffic.setImageResource(R.drawable.map_traffic_hl_white);
            mAMap.setTrafficEnabled(true);
        }
    }

    private void cleanRouteOverlay() {
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            RouteOverLay overlay = routeOverlays.get(key);
            overlay.removeFromMap();
            overlay.destroy();
        }
        routeOverlays.clear();
    }

    /**
     * 跳转到驾车偏好设置页面
     */
    private void strategyChoose() {
        Intent intent = new Intent(this, StrategyChooseActivity.class);
        intent.putExtra(GdNaviUtils.INTENT_NAME_AVOID_CONGESTION, mStrategyBean.isCongestion());
        intent.putExtra(GdNaviUtils.INTENT_NAME_AVOID_COST, mStrategyBean.isCost());
        intent.putExtra(GdNaviUtils.INTENT_NAME_AVOID_HIGHSPEED, mStrategyBean.isAvoidhightspeed());
        intent.putExtra(GdNaviUtils.INTENT_NAME_PRIORITY_HIGHSPEED, mStrategyBean.isHightspeed());
        startActivityForResult(intent, GdNaviUtils.START_ACTIVITY_REQUEST_CODE);
    }

    /**
     * @param paths 多路线回调路线
     * @param ints  多路线回调路线ID
     */
    private void setRouteLineTag(HashMap<Integer, AMapNaviPath> paths, int[] ints) {
        if (ints.length < 1) {
            visiableRouteLine(false, false, false);
            return;
        }
        int indexOne = 0;
        String stragegyTagOne = paths.get(ints[indexOne]).getLabels();
        setLinelayoutOneContent(ints[indexOne], stragegyTagOne);
        if (ints.length == 1) {
            visiableRouteLine(true, false, false);
            focuseRouteLine(true, false, false);
            return;
        }

        int indexTwo = 1;
        String stragegyTagTwo = paths.get(ints[indexTwo]).getLabels();
        setLinelayoutTwoContent(ints[indexTwo], stragegyTagTwo);
        if (ints.length == 2) {
            visiableRouteLine(true, true, false);
            focuseRouteLine(true, false, false);
            return;
        }

        int indexThree = 2;
        String stragegyTagThree = paths.get(ints[indexThree]).getLabels();
        setLinelayoutThreeContent(ints[indexThree], stragegyTagThree);
        if (ints.length >= 3) {
            visiableRouteLine(true, true, true);
            focuseRouteLine(true, false, false);
        }
    }

    private void visiableRouteLine(boolean lineOne, boolean lineTwo, boolean lineThree) {
        setLinelayoutOneVisiable(lineOne);
        setLinelayoutTwoVisiable(lineTwo);
        setLinelayoutThreeVisiable(lineThree);
    }

    private void setLinelayoutOneVisiable(boolean visiable) {
        if (visiable) {
            mRouteLineLayoutOne.setVisibility(View.VISIBLE);
        } else {
            mRouteLineLayoutOne.setVisibility(View.GONE);
        }
    }

    private void setLinelayoutTwoVisiable(boolean visiable) {
        if (visiable) {
            mRouteLinelayoutTwo.setVisibility(View.VISIBLE);
        } else {
            mRouteLinelayoutTwo.setVisibility(View.GONE);
        }
    }

    private void setLinelayoutThreeVisiable(boolean visiable) {
        if (visiable) {
            mRouteLineLayoutThree.setVisibility(View.VISIBLE);
        } else {
            mRouteLineLayoutThree.setVisibility(View.GONE);
        }
    }

    /**
     * 设置第一条线路Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutOneContent(int routeID, String strategy) {
        mRouteLineLayoutOne.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        overlay.zoomToSpan();
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyOne.setText(strategy);
        String timeDes = GdNaviUtils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeOne.setText(timeDes);
        String disDes = GdNaviUtils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceOne.setText(disDes);
    }

    /**
     * 设置第二条路线Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutTwoContent(int routeID, String strategy) {
        mRouteLinelayoutTwo.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyTwo.setText(strategy);
        String timeDes = GdNaviUtils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeTwo.setText(timeDes);
        String disDes = GdNaviUtils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceTwo.setText(disDes);
    }

    /**
     * 设置第三条路线Tab 内容
     *
     * @param routeID  路线ID
     * @param strategy 策略标签
     */
    private void setLinelayoutThreeContent(int routeID, String strategy) {
        mRouteLineLayoutThree.setTag(routeID);
        RouteOverLay overlay = routeOverlays.get(routeID);
        AMapNaviPath path = overlay.getAMapNaviPath();
        mRouteTextStrategyThree.setText(strategy);
        String timeDes = GdNaviUtils.getFriendlyTime(path.getAllTime());
        mRouteTextTimeThree.setText(timeDes);
        String disDes = GdNaviUtils.getFriendlyDistance(path.getAllLength());
        mRouteTextDistanceThree.setText(disDes);
    }

    /**
     * 第一条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutOne(boolean focus) {
        if (mRouteLineLayoutOne.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            RouteOverLay overlay = routeOverlays.get((int)mRouteLineLayoutOne.getTag());
            if (focus) {
                routeID = (int) mRouteLineLayoutOne.getTag();
                mCalculateRouteOverView.setText(GdNaviUtils.getRouteOverView(overlay.getAMapNaviPath()));
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewOne.setVisibility(View.VISIBLE);
                mRouteTextStrategyOne.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeOne.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceOne.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewOne.setVisibility(View.INVISIBLE);
                mRouteTextStrategyOne.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeOne.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceOne.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第二条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutTwo(boolean focus) {
        if (mRouteLinelayoutTwo.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            RouteOverLay overlay = routeOverlays.get((int) mRouteLinelayoutTwo.getTag());
            if (focus) {
                routeID = (int) mRouteLinelayoutTwo.getTag();
                mCalculateRouteOverView.setText(GdNaviUtils.getRouteOverView(overlay.getAMapNaviPath()));
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewTwo.setVisibility(View.VISIBLE);
                mRouteTextStrategyTwo.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeTwo.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceTwo.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewTwo.setVisibility(View.INVISIBLE);
                mRouteTextStrategyTwo.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeTwo.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceTwo.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第三条路线是否focus
     *
     * @param focus focus为true 突出颜色显示，标示为选中状态，为false则标示非选中状态
     */
    private void setLinelayoutThree(boolean focus) {
        if (mRouteLineLayoutThree.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            RouteOverLay overlay = routeOverlays.get((int) mRouteLineLayoutThree.getTag());
            if (overlay == null) {
                return;
            }
            if (focus) {
                routeID = (int) mRouteLineLayoutThree.getTag();
                mCalculateRouteOverView.setText(GdNaviUtils.getRouteOverView(overlay.getAMapNaviPath()));
                mAMapNavi.selectRouteId(routeID);
                overlay.setTransparency(ROUTE_SELECTED_TRANSPARENCY);
                mRouteViewThree.setVisibility(View.VISIBLE);
                mRouteTextStrategyThree.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextTimeThree.setTextColor(getResources().getColor(R.color.colorBlue));
                mRouteTextDistanceThree.setTextColor(getResources().getColor(R.color.colorBlue));
            } else {
                overlay.setTransparency(ROUTE_UNSELECTED_TRANSPARENCY);
                mRouteViewThree.setVisibility(View.INVISIBLE);
                mRouteTextStrategyThree.setTextColor(getResources().getColor(R.color.colorDark));
                mRouteTextTimeThree.setTextColor(getResources().getColor(R.color.colorBlack));
                mRouteTextDistanceThree.setTextColor(getResources().getColor(R.color.colorDark));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (mAMapNavi != null) {
            mAMapNavi.destroy();
        }
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Toast.makeText(this.getApplicationContext(),"错误码"+i,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        cleanRouteOverlay();
        HashMap<Integer, AMapNaviPath> paths = mAMapNavi.getNaviPaths();
        for (int i = 0; i < ints.length; i++) {
            AMapNaviPath path = paths.get(ints[i]);
            if (path != null) {
                drawRoutes(ints[i], path);
            }
        }
        setRouteLineTag(paths, ints);
        mAMap.setMapType(AMap.MAP_TYPE_NAVI);
    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }

    @Override
    public void onMapLoaded() {
        calculateDriveRoute();
    }

    @Override
    //安卓重写返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            Intent intent= new Intent(CalculateRouteActivity.this, LprMapActivity.class);
            intent.putExtra(NAVI_TYPE_KEY, "cancle_navi");
            intent.putExtra(IS_TRACING_KEY, istracing);
            startActivity(intent);
            finish();
        }
        return true;
    }

    private static final double EARTH_RADIUS = 6378137.0;
    public static double getDistance(double longitude,double latitue,double longitude2,double latitue2){
        double lat1 = rad(latitue);
        double lat2 = rad(latitue2);
        double a = lat1 - lat2;
        double b = rad(longitude)-rad(longitude2);
        double s = 2*Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin(b/2),2)));
        s=s*EARTH_RADIUS;
        s=Math.round(s*10000)/10000;
        return s/1000;
    }
    private static double rad(double d){
        return d*Math.PI/180.0;
    }
}