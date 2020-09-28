package com.sunland.cpocr.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
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
import com.amap.api.navi.model.NaviLatLng;
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
import com.google.gson.Gson;
import com.sunland.cpocr.MainActivity;
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


public class LprMapActivity extends BaseOcrActivity implements LocationSource, AMapLocationListener, TraceListener {

    //AMap是地图对象
    private AMap aMap;
    private MapView mapView;
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

    private static int MAIN_ACTIVITY_REQUEST_FAV_ADDRESS_CODE = 1;
    private static int MAIN_ACTIVITY_REQUEST_CHOOSE_CITY_ADDRESS_CODE = 2;

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

//        aMap.setLoadOfflineData(false);
//        aMap.setLoadOfflineData(true);
        //设置显示定位按钮 并且可以点击
        UiSettings settings = aMap.getUiSettings();
        aMap.setLocationSource((LocationSource) this);//设置了定位的监听
        // aMap.reloadMap();

        // 是否显示定位按钮
        settings.setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);//显示定位层并且可以触发定位,默认是flase

        mTraceoverlay = new TraceOverlay(aMap);
        //开始定位
        location();
        initpolyline();
    }

    private void location() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    private void initpolyline() {
        mPolyoptions = new PolylineOptions();
        mPolyoptions.width(10f);
        mPolyoptions.color(Color.BLUE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_activity_lprmap,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.recoeding_track:
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
                    //DecimalFormat decimalFormat = new DecimalFormat("0.0");
                    //string  result = decimalFormat.format(getTotalDistance() / 1000d) + "KM";
                    LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
                    mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record.getPathline()) , LBSTraceClient.TYPE_AMAP, LprMapActivity.this);
                    if(getTotalDistance() < 10){
                        Toast.makeText(getApplicationContext(),"当前轨迹路径总距离少于10m 保存失败",Toast.LENGTH_SHORT).show();
                    } else{
                        saveRecord(record.getPathline(), record.getDate());
                        Toast.makeText(getApplicationContext(),"轨迹录制已结束,此次总距离" + getTotalDistance() + "m",Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.tracks:
                Intent intent;
                intent = new Intent(LprMapActivity.this, TrackRecordActivity.class);
                startActivity(intent);
                break;

            case R.id.navi:
                mSearchModuelDeletage.getWidget(this).setVisibility(View.VISIBLE);
                break;

            case R.id.track_records:
                Toast.makeText(getApplicationContext(),"跳转巡逻记录",Toast.LENGTH_SHORT).show();
                Intent intent1;
                intent1 = new Intent(LprMapActivity.this, CpRecordActivity.class);
                startActivity(intent1);
                break;

            case R.id.offline_map:
                //在Activity页面调用startActvity启动离线地图组件
                startActivity(new Intent(this.getApplicationContext(), com.amap.api.maps.offlinemap.OfflineMapActivity.class));
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                aMapLocation.getLatitude();//获取纬度
                aMapLocation.getLongitude();//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
                aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getCountry();//国家信息
                aMapLocation.getProvince();//省信息
                aMapLocation.getCity();//城市信息
                aMapLocation.getDistrict();//城区信息
                aMapLocation.getStreet();//街道信息
                aMapLocation.getStreetNum();//街道门牌号信息
                aMapLocation.getCityCode();//城市编码
                aMapLocation.getAdCode();//地区编码

                //Toast.makeText(this,aMapLocation.getAddress(),Toast.LENGTH_LONG).show();
                //获取定位信息
                StringBuffer buffer = new StringBuffer();
                buffer.append(aMapLocation.getCountry() + ""
                        + aMapLocation.getProvince() + ""
                        + aMapLocation.getCity() + ""
                        + aMapLocation.getProvince() + ""
                        + aMapLocation.getDistrict() + ""
                        + aMapLocation.getStreet() + ""
                        + aMapLocation.getStreetNum());
                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                    //添加图钉
                    // aMap.addMarker(getMarkerOptions(amapLocation));

                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                    mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点

                }
                location = buffer.toString();
                lat = aMapLocation.getLatitude();
                lgt = aMapLocation.getLongitude();
                LatLng mylocation = new LatLng(aMapLocation.getLatitude(),
                        aMapLocation.getLongitude());
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点

                if (istracing) {
                    mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
                    record.addpoint(aMapLocation);
                    mPolyoptions.add(mylocation);
                    mTracelocationlist.add(Util.parseTraceLocation(aMapLocation));
                    redrawline();
                    if (mTracelocationlist.size() > tracesize - 1) {
                        trace();
                    }
                }

            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
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
        } else {
            Toast.makeText(com.sunland.cpocr.activity.LprMapActivity.this, "没有记录到路径", Toast.LENGTH_SHORT)
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
        Intent intent = new Intent(com.sunland.cpocr.activity.LprMapActivity.this, TrackRecordActivity.class);
        startActivity(intent);
    }

    private void trace() {
        List<TraceLocation> locationList = new ArrayList<>(mTracelocationlist);
        LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
        mTraceClient.queryProcessedTrace(1, locationList, LBSTraceClient.TYPE_AMAP, this);
        TraceLocation lastlocation = mTracelocationlist.get(mTracelocationlist.size()-1);
        mTracelocationlist.clear();
        mTracelocationlist.add(lastlocation);
    }

    /**
     * 轨迹纠偏失败回调。
     * @param i
     * @param s
     */
    @Override
    public void onRequestFailed(int i, String s) {
        mOverlayList.add(mTraceoverlay);
        mTraceoverlay = new TraceOverlay(aMap);
    }

    /**
     * 轨迹纠偏成功回调。
     * @param lineID 纠偏的线路ID
     * @param linepoints 纠偏结果
     * @param distance 总距离
     * @param waitingtime 等待时间
     */
    @Override
    public void onFinished(int lineID, List<LatLng> linepoints, int distance, int waitingtime) {
        if (lineID == 1) {
            if (linepoints != null && linepoints.size()>0) {

            }
        } else if (lineID == 2) {
            if (linepoints != null && linepoints.size()>0) {
                aMap.addPolyline(new PolylineOptions()
                        .setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.grasp_trace_line))
                        .width(40).addAll(linepoints));
            }
        }

    }

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
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
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
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;

    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();

        }
        mLocationClient = null;
    }

    @Override
    public void onTraceProcessing(int i, int i1, List<LatLng> list) {

    }

    @Override
    protected void updateUi(int frameLeft, int frameTop, int frameRight, int frameButton) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        //params.setMargins(frameRight + mSeekBar.getWidth() - 30, 0, 0, 0);
        params.setMargins(frameRight + mSeekBar.getWidth() + 170, 0, 0, 0);

        mSearchModuelDeletage = new SearchModuleDelegate();
        mSearchModuelDeletage.setPoiType(ISearchModule.IDelegate.DEST_POI_TYPE);
        mSearchModuelDeletage.bindParentDelegate(mSearchModuleParentDelegate);
//        contentView.addView(mSearchModuelDeletage.getWidget(this));

        mapView.setBackgroundColor(Color.WHITE);

        mapView.setLayoutParams(params);
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
            intent.setClass(LprMapActivity.this, CityChooseActivity.class);
            intent.putExtra(CityChooseActivity.CURR_CITY_KEY, mSearchModuelDeletage.getCurrCity().getCity());
            LprMapActivity.this.startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CHOOSE_CITY_ADDRESS_CODE);
        }

        @Override
        public void onCancel() {
            showToast("取消成功");
        }

        private void toSetFavAddressActivity(int type) {
            Intent intent = new Intent();
            intent.setClass(LprMapActivity.this, SetFavAddressActivity.class);
            intent.putExtra(FAVTYPE_KEY, type);
            Gson gson = new Gson();
            intent.putExtra(SetFavAddressActivity.CURR_CITY_KEY, gson.toJson(mSearchModuelDeletage.getCurrCity()));
            startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_FAV_ADDRESS_CODE);
        }
        @Override
        public void onSetFavHomePoi() {
            showToast("设置家的地址");
            toSetFavAddressActivity(0);
        }

        @Override
        public void onSetFavCompPoi() {
            showToast("设置公司地址");
            toSetFavAddressActivity(1);
        }

        @Override
        public void onChooseFavHomePoi(com.amap.api.services.core.PoiItem poiItemData) {

        }

        @Override
        public void onChooseFavCompPoi(com.amap.api.services.core.PoiItem poiItem) {

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

        private void saveToCache(PoiItem poiItem) {
            PoiItemDBHelper.getInstance().savePoiItem(LprMapActivity.this, poiItem);
        }
    };

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if(msg.equals("取消成功")){
            mSearchModuelDeletage.getWidget(this).setVisibility(View.INVISIBLE);
        }
    }

    //开始路径选择
    private void chooseRoute(){
        mSearchModuelDeletage.getWidget(this).setVisibility(View.INVISIBLE);

        AlertDialog.Builder builder = DialogHelp.getConfirmDialog(LprMapActivity.this,
                        "是否由 " + location + " 导航至 " + deslocation, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle bundle = new Bundle();
                                bundle.putDouble("lat", lat);
                                bundle.putDouble("lgt", lgt);
                                bundle.putDouble("deslat", desLat);
                                bundle.putDouble("deslgt", desLgt);
                                Intent intent;
                                intent = new Intent(LprMapActivity.this, CalculateRouteActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);

                            }
                        });
                builder.show();
    }

    public static final String FAVTYPE_KEY = "favtype";
    public static final String POIITEM_STR_KEY = "poiitem_str";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (MAIN_ACTIVITY_REQUEST_FAV_ADDRESS_CODE == requestCode && resultCode == RESULT_OK) {
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

        if (MAIN_ACTIVITY_REQUEST_CHOOSE_CITY_ADDRESS_CODE == requestCode && resultCode == RESULT_OK) {
            String currCityStr = data.getStringExtra(CityChooseActivity.CURR_CITY_KEY);
            Gson gson = new Gson();
            CityModel cityModel = gson.fromJson(currCityStr, CityModel.class);
            mSearchModuelDeletage.setCity(cityModel);
        }

        super.onActivityResult(requestCode, resultCode, data);
        mSearchModuelDeletage.getWidget(this).setVisibility(View.VISIBLE);
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
            } else{
                super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }
}