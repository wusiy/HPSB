package com.sunland.cpocr.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.services.core.PoiItem;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;
import com.amap.poisearch.searchmodule.ISearchModule;
import com.amap.poisearch.searchmodule.SearchModuleDelegate;
import com.amap.poisearch.util.CityModel;
import com.amap.poisearch.util.FavAddressUtil;
import com.amap.poisearch.util.PoiItemDBHelper;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.google.gson.Gson;
import com.sunland.cpocr.BuildConfig;
import com.sunland.cpocr.R;
import com.sunland.cpocr.activity.navi.CalculateRouteActivity;
import com.sunland.cpocr.db.DbTracks;
import com.sunland.cpocr.path_record.record.PathRecord;
import com.sunland.cpocr.path_record.recorduitl.Util;
import com.sunland.cpocr.utils.CpocrUtils;
import com.sunland.cpocr.utils.DialogHelp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class T1 extends BaseOcrActivity {

    public static final String FAVTYPE_KEY = "favtype";
    public static final String POIITEM_STR_KEY = "poiitem_str";
    public static final String NAVI_TYPE_KEY = "navi";
    public static final String IS_TRACING_KEY = "track";

    //AMap是地图对象
    private AMap aMap;
    private MapView mapView;
    //
    private AMapNaviView mAMapNaviView;
    private AMapNavi mAMapNavi;
    //声明AMapLocationClient类对象，定位发起端
    private AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象，定位参数
    public AMapLocationClientOption mLocationOption = null;
    //声明mListener对象，定位监听器
    private LocationSource.OnLocationChangedListener mListener = null;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    //当前定位经纬度
    private double lat;
    private double lgt;
    //目的地点经纬度
    private double desLat;
    private double desLgt;
    //当前定位地点名
    private String location;
    //目的地点名
    private String deslocation;
    //是否开启轨迹追踪
    private boolean istracing = false;
    //是否继续上次的轨迹录制
    private boolean continue_tracing = false;
    //上一次的巡逻轨迹记录
    private PathRecord lastRecord;
    private PolylineOptions mPolyoptions;
    private Polyline mpolyline;
    private PathRecord record;
    private long mStartTime;
    private long mEndTime;
    private ToggleButton btn;
    private DbTracks DbHepler;
    private List<TraceLocation> mTracelocationlist = new ArrayList<TraceLocation>();
    private List<TraceOverlay> mOverlayList = new ArrayList<TraceOverlay>();
    private List<AMapLocation> recordList = new ArrayList<AMapLocation>();
    private int tracesize = 30;
    private int mDistance = 0;
    private TraceOverlay mTraceoverlay;
    // private TextView mResultShow;
    private Marker mlocMarker;
    private SearchModuleDelegate mSearchModuelDeletage;
    private String startType;
    private Location loc;

    private static int LPRMAP_ACTIVITY_REQUEST_FAV_ADDRESS_CODE = 1;
    private static int LPRMAP_ACTIVITY_REQUEST_CHOOSE_CITY_ADDRESS_CODE = 2;
    private static int LPRMAP_ACTIVITY_START_NAVI_CODE = 3;
    private LocationManager locationManager;
    private String locationProvider;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        lat = 0;
        actionBar.setTitle("");
        //获取地图控件引用
        mapView = (MapView) findViewById(R.id.mapview);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setLoadOfflineData(false);
        aMap.setLoadOfflineData(true);
        mTraceoverlay = new TraceOverlay(aMap);
        initpolyline();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        init();
                    // 通过监听器监听GPS提供的定位信息的改变
                    if (ActivityCompat.checkSelfPermission(T1.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(T1.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location loc) {
                            // 使用GPS提供的定位信息来更新位置
                            updatePosition(loc);
                            if (istracing) {
                                LatLng mylocation = new LatLng(fromGpsToAmap(loc).getLatitude(),
                                        fromGpsToAmap(loc).getLongitude());
                              //  mListener.onLocationChanged(fromGpsToAmap(loc));// 显示系统小蓝点
                                aMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
                                record.addpoint(fromGpsToAmap(loc));
                                mPolyoptions.add(mylocation);
                                mTracelocationlist.add(Util.parseTraceLocation(fromGpsToAmap(loc)));
                                redrawline();
                                if (mTracelocationlist.size() > tracesize - 1) {
                                    trace();
                                }
                            }
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            // 使用GPS提供的定位信息来更新位置
                            if (ActivityCompat.checkSelfPermission(T1.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(T1.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            updatePosition(locationManager.getLastKnownLocation(provider));
                        }
                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                    });

    }

    // 初始化AMap对象
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            // 创建一个设置放大级别的CameraUpdate
            CameraUpdate cu = CameraUpdateFactory.zoomTo(15);
            // 设置地图的默认放大级别
            aMap.moveCamera(cu);
            // 创建一个更改地图倾斜度的CameraUpdate
            CameraUpdate tiltUpdate = CameraUpdateFactory.changeTilt(30);
            // 改变地图的倾斜度
            aMap.moveCamera(tiltUpdate);
        }
    }

    private void updatePosition(Location location) {
        LatLng pos = new LatLng(fromGpsToAmap(location).getLatitude(), fromGpsToAmap(location).getLongitude());
        // 创建一个设置经纬度的CameraUpdate
        CameraUpdate cu = CameraUpdateFactory.changeLatLng(pos);
        // 更新地图的显示区域
        aMap.moveCamera(cu);
        // 清除所有Marker等覆盖物
        aMap.clear();
        // 创建一个MarkerOptions对象
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        // 设置MarkerOptions使用自定义图标
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.point5));
        markerOptions.draggable(true);
        // 添加MarkerOptions（实际上是添加Marker）
        Marker marker = aMap.addMarker(markerOptions);
    }

    private void initpolyline() {
        mPolyoptions = new PolylineOptions();
        mPolyoptions.width(10f);
        mPolyoptions.color(Color.BLUE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_activity_lprmap,menu);
        MenuItem item =menu.findItem(R.id.navi);
        item.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    public AMapLocation fromGpsToAmap(Location location) {
        AMapLocation aMapLocation = new AMapLocation(location);
        CoordinateConverter converter = new CoordinateConverter(this);
        converter.from(CoordinateConverter.CoordType.GPS);
        try {
            converter.coord(new DPoint(location.getLatitude(), location.getLongitude()));
            DPoint desLatLng = converter.convert();
            aMapLocation.setLatitude(desLatLng.getLatitude());
            aMapLocation.setLongitude(desLatLng.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aMapLocation;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.recording_track:
                if(!istracing) {
                    Toast.makeText(getApplicationContext(),"轨迹开始录制",Toast.LENGTH_LONG).show();
                    item.setIcon(getResources().getDrawable(R.drawable.stop));
                    istracing = true;
                    mpolyline = null;
                    initpolyline();
                    if (record != null) {
                        record = null;
                    }
                    record = new PathRecord();
                    mStartTime = System.currentTimeMillis();
                    record.setDate(getcueDate(mStartTime));
                    aMap.clear(true);
                } else{
                    item.setIcon(getResources().getDrawable(R.drawable.start1));
                    istracing = false;
                    mEndTime = System.currentTimeMillis();
                    mOverlayList.add(mTraceoverlay);
                    LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
                   // mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record.getPathline()) , LBSTraceClient.TYPE_AMAP, T1.this);
//                    if(getTotalDistance() < 10){
//                        Toast.makeText(getApplicationContext(),"当前轨迹路径总距离少于10m 保存失败",Toast.LENGTH_SHORT).show();
//                    } else{
//                        saveRecord(record.getPathline(), record.getDate());
//                        Toast.makeText(getApplicationContext(),"轨迹录制已结束,此次总距离" + getTotalDistance() + "m",Toast.LENGTH_SHORT).show();
//                    }
                    saveRecord(record.getPathline(), record.getDate());
                }
                break;

            case R.id.tracks:
                Intent intent;
                intent = new Intent(T1.this, TrackRecordActivity.class);
                startActivity(intent);
                break;

            case R.id.navi:
                if(lat != 0) {
                    mSearchModuelDeletage.setCurrLoc(loc);
                }
                mSearchModuelDeletage.getWidget(this).setVisibility(View.VISIBLE);
                break;

            case R.id.track_records:
                Intent intent1;
                intent1 = new Intent(T1.this, CpRecordActivity.class);
                startActivity(intent1);
                break;

            case R.id.offline_map:
                //在Activity页面调用startActvity启动离线地图组件
                startActivity(new Intent(this.getApplicationContext(), com.amap.api.maps.offlinemap.OfflineMapActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void saveRecord(List<AMapLocation> list, String time) {
        if (list != null && list.size() > 0) {
            DbHepler = new DbTracks(this);
            DbHepler.open();
            String duration = getDuration();
            float distance = getDistance(list);
            String average = getAverage(distance);
            String pathlineSring = getPathLineString(list);
            AMapLocation firstLocaiton = list.get(0);
            AMapLocation lastLocaiton = list.get(list.size() - 1);
            String stratpoint = amapLocationToString(firstLocaiton);
            String endpoint = amapLocationToString(lastLocaiton);
            DbHepler.createrecord(String.valueOf(distance), duration, average,
                    pathlineSring, stratpoint, endpoint, time);
            DbHepler.close();
            Toast.makeText(getApplicationContext(),"轨迹录制已结束,此次总距离" + distance + "m",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(com.sunland.cpocr.activity.T1.this, "没有记录到路径", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private String getDuration() {
        return String.valueOf((mEndTime - mStartTime) / 1000f);
    }

    private String getAverage(float distance) {
        return String.valueOf(distance / (float) (mEndTime - mStartTime));
    }

    private float getDistance(List<AMapLocation> list) {
        float distance = 0;
        if (list == null || list.size() == 0) {
            return distance;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            AMapLocation firstpoint = list.get(i);
            AMapLocation secondpoint = list.get(i + 1);
            LatLng firstLatLng = new LatLng(firstpoint.getLatitude(),
                    firstpoint.getLongitude());
            LatLng secondLatLng = new LatLng(secondpoint.getLatitude(),
                    secondpoint.getLongitude());
            double betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
                    secondLatLng);
            distance = (float) (distance + betweenDis);
        }
        return distance;
    }

    private String getPathLineString(List<AMapLocation> list) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuffer pathline = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            AMapLocation location = list.get(i);
            String locString = amapLocationToString(location);
            pathline.append(locString).append(";");
        }
        String pathLineString = pathline.toString();
        pathLineString = pathLineString.substring(0,
                pathLineString.length() - 1);
        return pathLineString;
    }

    private String amapLocationToString(AMapLocation location) {
        StringBuffer locString = new StringBuffer();
        locString.append(location.getLatitude()).append(",");
        locString.append(location.getLongitude()).append(",");
        locString.append(location.getProvider()).append(",");
        locString.append(location.getTime()).append(",");
        locString.append(location.getSpeed()).append(",");
        locString.append(location.getBearing());
        return locString.toString();
    }

    /**
     * 实时轨迹画线
     */
    private void redrawline() {
        if (mPolyoptions.getPoints().size() > 1) {
            if (mpolyline != null) {
                mpolyline.setPoints(mPolyoptions.getPoints());
            } else {
                mpolyline = aMap.addPolyline(mPolyoptions);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getcueDate(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd  HH:mm:ss ");
        Date curDate = new Date(time);
        String date = formatter.format(curDate);
        return date;
    }

    public void record(View view) {
        Intent intent = new Intent(com.sunland.cpocr.activity.T1.this, TrackRecordActivity.class);
        startActivity(intent);
    }

    private void trace() {
        List<TraceLocation> locationList = new ArrayList<>(mTracelocationlist);
        LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
//        mTraceClient.queryProcessedTrace(1, locationList, LBSTraceClient.TYPE_AMAP, this);
        TraceLocation lastlocation = mTracelocationlist.get(mTracelocationlist.size()-1);
        mTracelocationlist.clear();
        mTracelocationlist.add(lastlocation);
    }

//    /**
//     * 轨迹纠偏失败回调。
//     * @param i
//     * @param s
//     */
//    @Override
//    public void onRequestFailed(int i, String s) {
//        mOverlayList.add(mTraceoverlay);
//        mTraceoverlay = new TraceOverlay(aMap);
//    }

//    /**
//     * 轨迹纠偏成功回调。
//     * @param lineID 纠偏的线路ID
//     * @param linepoints 纠偏结果
//     * @param distance 总距离
//     * @param waitingtime 等待时间
//     */
//    @Override
//    public void onFinished(int lineID, List<LatLng> linepoints, int distance, int waitingtime) {
//        if (lineID == 1) {
//            if (linepoints != null && linepoints.size()>0) {
//            }
//        } else if (lineID == 2) {
//            if (linepoints != null && linepoints.size()>0) {
//                aMap.addPolyline(new PolylineOptions()
//                        .setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.grasp_trace_line))
//                        .width(40).addAll(linepoints));
//            }
//        }
//    }

    /**
     * 最后获取总距离
     * @return
     */
    private int getTotalDistance() {
        int distance = 0;
        for (TraceOverlay to : mOverlayList) {
            distance = distance + to.getDistance();
        }
        return distance;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        //mAMapNaviView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        //mAMapNaviView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
       // mAMapNaviView.onDestroy();
        //mAMapNavi.stopNavi();
    }



    @Override
    protected void updateUi(int frameLeft, int frameTop, int frameRight, int frameButton) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        //params.setMargins(frameRight + mSeekBar.getWidth() - 30, 0, 0, 0);
        params.setMargins(frameRight + mSeekBar.getWidth() + 170, 0, 0, 0);

        mapView.setBackgroundColor(Color.WHITE);
        mapView.setLayoutParams(params);
        //mapView.setVisibility(View.INVISIBLE);

        mSearchModuelDeletage = new SearchModuleDelegate();
        mSearchModuelDeletage.setPoiType(ISearchModule.IDelegate.DEST_POI_TYPE);
        mSearchModuelDeletage.bindParentDelegate(mSearchModuleParentDelegate);
        if(lat != 0) {
            Location loc = null;
            loc.setLatitude(lat);
            loc.setLongitude(lgt);
            mSearchModuelDeletage.setCurrLoc(loc);
        }
        SharedPreferences sp= getApplicationContext().getSharedPreferences("城市", Context.MODE_PRIVATE);
        String cityName=sp.getString("city_name", "");
        if(!cityName.equals("")){
            Gson gson = new Gson();
            CityModel cityModel = gson.fromJson(cityName, CityModel.class);
            mSearchModuelDeletage.setCity(cityModel);
        }
        re_c.addView(mSearchModuelDeletage.getWidget(this));
        mSearchModuelDeletage.getWidget(this).setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams flashLp = (RelativeLayout.LayoutParams) mFlashBtn.getLayoutParams();
        flashLp.addRule(RelativeLayout.LEFT_OF, -1);
        flashLp.setMarginStart(frameRight - dp2px(10));
        mFlashBtn.setLayoutParams(flashLp);
        RelativeLayout.LayoutParams seekBarLp = (RelativeLayout.LayoutParams) mSeekBar.getLayoutParams();
        seekBarLp.setMarginStart(frameRight + 110);
        mSeekBar.setLayoutParams(seekBarLp);
    }

    private SearchModuleDelegate.IParentDelegate mSearchModuleParentDelegate = new ISearchModule.IDelegate.IParentDelegate() {
        @Override
        public void onChangeCityName() {
            showToast("选择城市");
            Intent intent = new Intent();
            intent.setClass(T1.this, CityChooseActivity.class);
            intent.putExtra(CityChooseActivity.CURR_CITY_KEY, mSearchModuelDeletage.getCurrCity().getCity());
            T1.this.startActivityForResult(intent, LPRMAP_ACTIVITY_REQUEST_CHOOSE_CITY_ADDRESS_CODE);
        }

        @Override
        public void onCancel() {
            showToast("取消成功");
        }

        //点击未设置地址的家按钮
        @Override
        public void onSetFavHomePoi() {
            showToast("设置家的地址");
            toSetFavAddressActivity(0);
        }

        //点击未设置地址的公司按钮
        @Override
        public void onSetFavCompPoi() {
            showToast("设置公司地址");
            toSetFavAddressActivity(1);
        }

        //点击已经设置地址的家按钮
        @Override
        public void onChooseFavHomePoi(com.amap.api.services.core.PoiItem poiItemData) {
            clickFavHome(poiItemData);
        }

        //点击已经设置地址的公司按钮
        @Override
        public void onChooseFavCompPoi(com.amap.api.services.core.PoiItem poiItem) {
            clickFavComp(poiItem);
        }

        @Override
        public void onSelPoiItem(com.amap.api.services.core.PoiItem poiItem) {
            saveToCache(poiItem);
            //showToast("选择了检索结果的 " + poiItem.getTitle() + poiItem.getLatLonPoint());
            desLat = poiItem.getLatLonPoint().getLatitude();
            desLgt = poiItem.getLatLonPoint().getLongitude();
            deslocation = poiItem.getTitle();
            chooseRoute();
        }
    };

    private void showToast(String msg) {
        if(msg.equals("取消成功")){
            mSearchModuelDeletage.getWidget(this).setVisibility(View.INVISIBLE);
        }
        else{
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void toSetFavAddressActivity(int type) {
        Intent intent = new Intent();
        intent.setClass(T1.this, SetFavAddressActivity.class);
        intent.putExtra(FAVTYPE_KEY, type);
        Gson gson = new Gson();
        intent.putExtra(SetFavAddressActivity.CURR_CITY_KEY, gson.toJson(mSearchModuelDeletage.getCurrCity()));
        startActivityForResult(intent, LPRMAP_ACTIVITY_REQUEST_FAV_ADDRESS_CODE);
    }

    private void saveToCache(PoiItem poiItem) {
        PoiItemDBHelper.getInstance().savePoiItem(T1.this, poiItem);
    }

    private void clickFavHome(com.amap.api.services.core.PoiItem poiItem){
        final String items[] = {"重新设置家的地址", "导航至家"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择下一步操作")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            toSetFavAddressActivity(0);
                        } else if(which == 1){
                            saveToCache(poiItem);
                            //showToast("选择了检索结果的 " + poiItem.getTitle() + poiItem.getLatLonPoint());
                            desLat = poiItem.getLatLonPoint().getLatitude();
                            desLgt = poiItem.getLatLonPoint().getLongitude();
                            deslocation = poiItem.getTitle();
                            chooseRoute();
                        }
                    }})
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private void clickFavComp(com.amap.api.services.core.PoiItem poiItem){
        final String items[] = {"重新设置公司的地址", "导航至公司"};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择下一步操作")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            toSetFavAddressActivity(1);
                        } else if(which == 1){
                            saveToCache(poiItem);
                            //showToast("选择了检索结果的 " + poiItem.getTitle() + poiItem.getLatLonPoint());
                            desLat = poiItem.getLatLonPoint().getLatitude();
                            desLgt = poiItem.getLatLonPoint().getLongitude();
                            deslocation = poiItem.getTitle();
                            chooseRoute();
                        }
                    }})
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    //开始路径选择
    private void chooseRoute(){
        mSearchModuelDeletage.getWidget(this).setVisibility(View.INVISIBLE);

        AlertDialog.Builder builder = DialogHelp.getConfirmDialog(T1.this,
                "是否由 " + location + " 导航至 " + deslocation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        bundle.putDouble("lat", lat);
                        bundle.putDouble("lgt", lgt);
                        bundle.putDouble("deslat", desLat);
                        bundle.putDouble("deslgt", desLgt);
                        Intent intent;
                        intent = new Intent(T1.this, CalculateRouteActivity.class);
                        intent.putExtras(bundle);
                        continue_tracing = false;
                        if(istracing){
                            intent.putExtra(IS_TRACING_KEY, "true");
                            mEndTime = System.currentTimeMillis();
                            mOverlayList.add(mTraceoverlay);
                            LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
                            //mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record.getPathline()) , LBSTraceClient.TYPE_AMAP, T1.this);
                            saveRecord(record.getPathline(), record.getDate());
                        } else{
                            intent.putExtra(IS_TRACING_KEY, "false");
                        }
                        //startActivityForResult(intent, LPRMAP_ACTIVITY_START_NAVI_CODE);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (LPRMAP_ACTIVITY_REQUEST_FAV_ADDRESS_CODE == requestCode && resultCode == RESULT_OK) {
            String poiitemStr = data.getStringExtra(POIITEM_STR_KEY);
            int favType = data.getIntExtra(FAVTYPE_KEY, -1);

            PoiItem poiItem = new Gson().fromJson(poiitemStr, PoiItem.class);
            if (favType == 0) {
                FavAddressUtil.saveFavHomePoi(this, poiItem);
                mSearchModuelDeletage.setFavHomePoi(poiItem);
            } else if (favType == 1) {
                FavAddressUtil.saveFavCompPoi(this, poiItem);
                mSearchModuelDeletage.setFavCompPoi(poiItem);
            }
        }

        if (LPRMAP_ACTIVITY_REQUEST_CHOOSE_CITY_ADDRESS_CODE == requestCode && resultCode == RESULT_OK) {
            String currCityStr = data.getStringExtra(CityChooseActivity.CURR_CITY_KEY);
            SharedPreferences sp= getApplicationContext().getSharedPreferences("城市", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("city_name", currCityStr);
            editor.commit();
            Gson gson = new Gson();
            CityModel cityModel = gson.fromJson(currCityStr, CityModel.class);
            mSearchModuelDeletage.setCity(cityModel);

        }

//        if (LPRMAP_ACTIVITY_START_NAVI_CODE == requestCode && resultCode == RESULT_OK) {
//            mapView.setVisibility(View.INVISIBLE);
//            mAMapNaviView.setVisibility(View.VISIBLE);
//
//            mAMapNavi = AMapNavi.getInstance(getApplicationContext());
//            mAMapNavi.addAMapNaviListener(this);
//            mAMapNavi.setUseInnerVoice(true);
//            if(data.getStringExtra(NAVI_TYPE_KEY).equals("false")){ //模拟导航
//                //模拟导航时的时速
//                mAMapNavi.setEmulatorNaviSpeed(60);
//                mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode); //模拟导航模式
//            } else if(data.getStringExtra(NAVI_TYPE_KEY).equals("true")){
//                mAMapNavi.startNavi(AMapNavi.GPSNaviMode);
//            }
//
//        } else if(LPRMAP_ACTIVITY_START_NAVI_CODE == requestCode && resultCode == RESULT_CANCELED){
//            mapView.setVisibility(View.VISIBLE);
//            mAMapNaviView.setVisibility(View.INVISIBLE);
//            Toast.makeText(this, "路径出错， 本次导航取消", Toast.LENGTH_SHORT).show();
//        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected boolean isSerialMode() {
        //表示连续识别
        return true;
    }

    @Override
    protected void handleIntent(Intent intent) {
        final String hphm = intent.getStringExtra(BaseOcrActivity.EXTRA_RET_HPHM);
        final String hpysStr = intent.getStringExtra(BaseOcrActivity.EXTRA_RET_HPYS_STR);
        final String[] hpzls = CpocrUtils.getHpzlFromOcr(hphm, hpysStr);
        final String hpzl = hpzls[0];
        final String hpzlStr = hpzls[1];
        Toast.makeText(this,hphm,Toast.LENGTH_SHORT).show();
        //adapter.add(hphm, hpzl, hpzlStr, hpysStr);
    }

    @Override
    //安卓重写返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(mSearchModuelDeletage.getWidget(this).getVisibility() ==  View.VISIBLE){
                mSearchModuelDeletage.getWidget(this).setVisibility(View.INVISIBLE);
            }  else {
                super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }

//    @Override
//    public void activate(OnLocationChangedListener listener) {
//        mListener = listener;
//    }
//
//    @Override
//    public void deactivate() {
//        mListener = null;
//        if (mLocationClient != null) {
//            mLocationClient.stopLocation();
//            mLocationClient.onDestroy();
//        }
//        mLocationClient = null;
//    }
}
