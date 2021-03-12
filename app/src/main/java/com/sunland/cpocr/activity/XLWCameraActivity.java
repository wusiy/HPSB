package com.sunland.cpocr.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.CoordinateConverter;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
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
import com.sunland.cpocr.R;
import com.sunland.cpocr.activity.navi.CalculateRouteActivity;
import com.sunland.cpocr.db.DbCpHmZp;
import com.sunland.cpocr.db.DbTracks;
import com.sunland.cpocr.path_record.record.PathRecord;
import com.sunland.cpocr.path_record.recorduitl.Util;
import com.sunland.cpocr.utils.CpocrUtils;
import com.sunland.cpocr.utils.DialogHelp;
import com.sunland.cpocr.utils.SensorEventHelper;
import com.sunland.cpocr.xlw.MyUsbResult;
import com.sunland.cpocr.xlw.USBDataParser;
import com.sunland.cpocr.xlw.VideoDecoder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.sunland.cpocr.MainActivity.FAVTYPE_KEY;
import static com.sunland.cpocr.MainActivity.IS_TRACING_KEY;
import static com.sunland.cpocr.MainActivity.NAVI_TYPE_KEY;
import static com.sunland.cpocr.MainActivity.POIITEM_STR_KEY;

/**
 * 离线定位，车牌识别
 */

public class XLWCameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, TraceListener, AMapNaviListener, AMapNaviViewListener {

    private int tracesize = 30;
    //当前定位经纬度
    private double lat, lgt;
    //目的地点经纬度
    private double desLat, desLgt;
    //巡逻轨迹记录的开始时间， 结束时间
    private long mStartTime, mEndTime;
    //此activity的初始化模式
    private String initType;
    //当前定位地点名, 目的地地点名
    private String location, deslocation;
    //是否开启轨迹追踪
    private boolean istracing = false;
    //是否继续上次的轨迹录制
    private boolean continue_tracing = false;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    //记录当前的位置
    private Location loc;
    private DbTracks DbHepler;
    private DbCpHmZp dbCpHmZp;
    //巡逻轨迹记录
    private PathRecord record;
    //上一次的巡逻轨迹记录
    private PathRecord lastRecord;
    private TraceOverlay mTraceoverlay;
    private List<TraceLocation> mTracelocationlist = new ArrayList<TraceLocation>();
    private List<TraceOverlay> mOverlayList = new ArrayList<TraceOverlay>();
    private SensorEventHelper mSensorHelper;
    protected Vibrator mVibrator;

    private ImageButton ib_locate;
    private ImageView mLocating;
    private SurfaceView surfaceView;
    private RelativeLayout re_c;
    private ProgressDialog dialog;
    //地图画线
    private Polyline mpolyline;
    private PolylineOptions mPolyoptions;
    //自定义定位图标marker及定位范围圈
    private Marker mLocMarker;
    private MarkerOptions mLocMarkerOption;
    private Circle mCircle;
    //POI搜索模块View
    private SearchModuleDelegate mSearchModuelDeletage;
    //AMap是地图对象
    private AMap aMap;
    private MapView mapView;
    private AMapNaviView mAMapNaviView;
    private AMapNavi mAMapNavi;
    //声明mListener对象，定位监听器
    private LocationSource.OnLocationChangedListener mListener = null;
    private LocationManager locationManager;

    //此块为xlw所使用的参数
    private boolean m_bOpen = false;
    private boolean m_bExit = false;
    private Thread m_ReaderThread = null;
    private Thread m_VideoReaderThread = null;
    MyUsbResult m_myUSB = new MyUsbResult();
    private boolean firstFrame = true;
    VideoDecoder mDecoder;

    private static final String LOCATION_MARKER_FLAG = "mylocation";
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private static final int LPRMAP_ACTIVITY_REQUEST_FAV_ADDRESS_CODE = 1;
    private static final int LPRMAP_ACTIVITY_REQUEST_CHOOSE_CITY_ADDRESS_CODE = 2;

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xlw_camera);
        // 屏幕常亮
        XLWCameraActivity.this.runOnUiThread(() -> XLWCameraActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON));
        mVibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);

        initData();
        initUi(savedInstanceState);
        startLocate();

        ib_locate.setOnClickListener(v -> {
            if (loc != null && lat != 0) {
                aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
            } else {
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_SHORT).show();
            }
        });

        // 启动数据读取线程
        m_bExit = false;
        m_ReaderThread = new Thread(new Reader());
        m_ReaderThread.start();

        m_VideoReaderThread = new Thread(new VideoReader());
        m_VideoReaderThread.start();
    }

    private void initData() {
        lat = 0;
        initType = getIntent().getStringExtra(NAVI_TYPE_KEY);
        dialog = new ProgressDialog(this);
        mSensorHelper = new SensorEventHelper(this);
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
        initPolyline();
    }

    private void initPolyline() {
        mPolyoptions = new PolylineOptions();
        mPolyoptions.width(10f);
        mPolyoptions.color(Color.BLUE);
    }

    private void initUi(Bundle savedInstanceState) {

        ib_locate = findViewById(R.id.ib_locate);
        //获取地图控件引用
        mapView = findViewById(R.id.mapview);
        mAMapNaviView = findViewById(R.id.navi_view);
        mLocating = findViewById(R.id.iv_locating);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe);
        re_c = (RelativeLayout) findViewById(R.id.re_c);
        surfaceView.getHolder().addCallback(this);


        mLocating.setVisibility(View.VISIBLE);
        ib_locate.getBackground().setAlpha(210);

        dialog.setMessage("正在使用GPS定位... 请到露天空旷处加速定位");
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        //重新加载离线地图数据
        aMap.setLoadOfflineData(true);

        UiSettings settings = aMap.getUiSettings();
        //是否允许显示缩放按钮
        settings.setZoomControlsEnabled(true);
        //设置缩放按钮的位置
        settings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_BUTTOM);
        //是否显示指南针
        settings.setCompassEnabled(true);

        //是否显示控制比例尺控件
        settings.setScaleControlsEnabled(true);
        //设置logo位置, 默认左下角(不可移除)
        settings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
        mTraceoverlay = new TraceOverlay(aMap);

        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
        //是否打开语音导航
        mAMapNavi.setUseInnerVoice(true);

        if (initType.equals("")) {
            mapView.setVisibility(View.VISIBLE);
            mAMapNaviView.setVisibility(View.INVISIBLE);
        } else if (initType.equals("cancle_navi")) { //路径规划时取消导航
            mapView.setVisibility(View.VISIBLE);
            mAMapNaviView.setVisibility(View.INVISIBLE);
            //是否继续上次的巡逻轨迹记录
            if (getIntent().getStringExtra(IS_TRACING_KEY).equals("true")) {
                continue_tracing = true;
                continueTracing();
            }
        } else if (initType.equals("false")) { //模拟导航
            mapView.setVisibility(View.INVISIBLE);
            mAMapNaviView.setVisibility(View.VISIBLE);
            //是否继续上次的巡逻轨迹记录
            if (getIntent().getStringExtra(IS_TRACING_KEY).equals("true")) {
                continue_tracing = true;
                continueTracing();
            }
            mAMapNavi = AMapNavi.getInstance(getApplicationContext());
            mAMapNavi.addAMapNaviListener(this);
            mAMapNavi.setUseInnerVoice(true);
            mAMapNavi.setEmulatorNaviSpeed(60);
            mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
        } else if (initType.equals("true")) { //实时导航
            mapView.setVisibility(View.INVISIBLE);
            mAMapNaviView.setVisibility(View.VISIBLE);
            //是否继续上次的巡逻轨迹记录
            if (getIntent().getStringExtra(IS_TRACING_KEY).equals("true")) {
                continue_tracing = true;
                continueTracing();
            }
            mAMapNavi = AMapNavi.getInstance(getApplicationContext());
            mAMapNavi.addAMapNaviListener(this);
            mAMapNavi.setUseInnerVoice(true);
            mAMapNavi.startNavi(AMapNavi.GPSNaviMode);
        }

        mapView.setBackgroundColor(Color.WHITE);
        mAMapNaviView.setBackgroundColor(Color.WHITE);

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

    }

    private void startLocate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                public void onLocationChanged(Location mLocation) {
                    List<Address> result = null;
                    try {
                        if (mLocation != null) {
                            Geocoder gc = new Geocoder(XLWCameraActivity.this, Locale.getDefault());
                            result = gc.getFromLocation(mLocation.getLatitude(),
                                    mLocation.getLongitude(), 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.size() > 0 ) {
                        Toast.makeText(XLWCameraActivity.this, fromAddressToString(result.get(0)), Toast.LENGTH_SHORT).show();
                        location = fromAddressToString(result.get(0));
                    } else {
                        location = null;
                    }
                    lat = fromGpsToAmap(mLocation).getLatitude();
                    lgt = fromGpsToAmap(mLocation).getLongitude();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        mLocating.setVisibility(View.GONE);
                    }
                    if (isFirstLoc) {
                        //设置缩放级别
                        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        //将地图移动到定位点
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(lat, lgt)));
                        isFirstLoc = false;
                    }
                    addCircle(new LatLng(lat, lgt), fromGpsToAmap(mLocation).getAccuracy());//添加定位精度圆
                    addMarker(new LatLng(lat, lgt));//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    //aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(lat, lgt)));
                    if (istracing) {
                        LatLng mylocation = new LatLng(lat, lgt);
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
                        record.addpoint(fromGpsToAmap(mLocation));
                        mPolyoptions.add(mylocation);
                        mTracelocationlist.add(Util.parseTraceLocation(fromGpsToAmap(mLocation)));
                        redrawline();
                        if (mTracelocationlist.size() > tracesize - 1) {
                            trace();
                        }
                    }
                    loc = fromGpsToAmap(mLocation);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    switch (status) {
                        // GPS状态为可见时
                        case LocationProvider.AVAILABLE:
                            mLocating.setVisibility(View.GONE);
                            Toast.makeText(XLWCameraActivity.this, "GPS状态:可见", Toast.LENGTH_SHORT).show();
                            break;
                        // GPS状态为服务区外时
                        case LocationProvider.OUT_OF_SERVICE:
                            location = "";
                            lat = 0;
                            mLocating.setVisibility(View.VISIBLE);
                            Toast.makeText(XLWCameraActivity.this, "GPS状态:服务区外", Toast.LENGTH_SHORT).show();
                            break;
                        // GPS状态为暂停服务时
                        case LocationProvider.TEMPORARILY_UNAVAILABLE:
                            location = "";
                            lat = 0;
                            mLocating.setVisibility(View.VISIBLE);
                            Toast.makeText(XLWCameraActivity.this, "GPS状态:服务暂停", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                    location = "";
                }
            });
        } else {
            Toast.makeText(this, "无法定位，请打开定位服务", Toast.LENGTH_SHORT).show();
            Intent i = new Intent();
            i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
        }
    }

    //绘制上次记录的巡逻轨迹并继续
    private void continueTracing(){
        aMap.clear(true);
        mLocMarker = null;
        mpolyline = null;
        initPolyline();
        mStartTime = System.currentTimeMillis();
        DbTracks dbhelper = new DbTracks(this.getApplicationContext());
        dbhelper.open();
        //上一次的巡逻记录
        lastRecord = dbhelper.queryLastRecord();
        dbhelper.close();

        if (record != null) {
            record = null;
        }
        record = new PathRecord();
        record.setDate(lastRecord.getDate());
        record.setStrStartPoint(lastRecord.getStrStartPoint());
        //绘制上一次巡逻记录起点
        record.addpoint(lastRecord.getStartpoint());
        mPolyoptions.add(new LatLng(lastRecord.getStartpoint().getLatitude(),
                lastRecord.getStartpoint().getLongitude()));
        mTracelocationlist.add(Util.parseTraceLocation(lastRecord.getStartpoint()));
        redrawline();
        if (mTracelocationlist.size() > tracesize - 1) {
            trace();
        }
        //绘制上一次巡逻记录的中间点
        for(int i = 0; i < lastRecord.getPathline().size(); i++){
            record.addpoint(lastRecord.getPathline().get(i));
            mPolyoptions.add(new LatLng(lastRecord.getPathline().get(i).getLatitude(),
                    lastRecord.getPathline().get(i).getLongitude()));
            mTracelocationlist.add(Util.parseTraceLocation(lastRecord.getPathline().get(i)));
            redrawline();
            if (mTracelocationlist.size() > tracesize - 1) {
                trace();
            }
        }
        //绘制上一次巡逻记录的最后一个点
        record.addpoint(lastRecord.getEndpoint());
        mPolyoptions.add(new LatLng(lastRecord.getEndpoint().getLatitude(),
                lastRecord.getEndpoint().getLongitude()));
        mTracelocationlist.add(Util.parseTraceLocation(lastRecord.getEndpoint()));
        redrawline();
        if (mTracelocationlist.size() > tracesize - 1) {
            trace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_activity_lprmap,menu);
        Menu mMenu=menu;
        MenuItem item = mMenu.findItem(R.id.recording_track);
        if(continue_tracing){
            Toast.makeText(getApplicationContext(),"正在继续上次的巡逻轨迹录制",Toast.LENGTH_LONG).show();
            item.setIcon(getResources().getDrawable(R.drawable.stop));
            istracing = true;
        }
        mMenu.findItem(R.id.connect).setVisible(true);
        return super.onCreateOptionsMenu(menu);
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
                    initPolyline();
                    if (record != null) {
                        record = null;
                    }
                    record = new PathRecord();
                    mStartTime = System.currentTimeMillis();
                    record.setDate(getcueDate(mStartTime));
                    record.setStrStartPoint(location);
                    aMap.clear(true);
                    mLocMarker = null;
                } else{
                    item.setIcon(getResources().getDrawable(R.drawable.start1));
                    istracing = false;
                    mEndTime = System.currentTimeMillis();
                    mOverlayList.add(mTraceoverlay);
                    //DecimalFormat decimalFormat = new DecimalFormat("0.0");
                    //string  result = decimalFormat.format(getTotalDistance() / 1000d) + "KM";
                    LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
                    mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record.getPathline()) ,
                            LBSTraceClient.TYPE_AMAP, XLWCameraActivity.this);
                    saveRecord(record.getPathline(), record.getDate(), record.getStrStartPoint());
                }
                break;

            case R.id.tracks:
                Intent intent;
                intent = new Intent(XLWCameraActivity.this, TrackRecordActivity.class);
                startActivity(intent);
                break;

            case R.id.navi:
                if(lat == 0) {
                    Toast.makeText(this,"请等待初次定位完成后再使用导航", Toast.LENGTH_SHORT).show();
                } else if(!isNetworkConnected(this)){
                    Toast.makeText(this,"当前无网络连接，无法使用导航", Toast.LENGTH_SHORT).show();
                } else{
                    mSearchModuelDeletage.setCurrLoc(loc);
                    mSearchModuelDeletage.getWidget(this).setVisibility(View.VISIBLE);
                }
                break;

            case R.id.track_records:
                Intent intent1;
                intent1 = new Intent(XLWCameraActivity.this, CpRecordActivity.class);
                startActivity(intent1);
                break;

            case R.id.offline_map:
                //在Activity页面调用startActvity启动离线地图组件
                startActivity(new Intent(this.getApplicationContext(), com.amap.api.maps.offlinemap.OfflineMapActivity.class));
                break;
            case R.id.connect:
                Toast.makeText(XLWCameraActivity.this,"开始获取USB设备信息", Toast.LENGTH_SHORT).show();
                m_myUSB.OpenDevice(XLWCameraActivity.this);
                m_bOpen = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private String fromAddressToString (Address address){
        StringBuffer addressString = new StringBuffer();
//        addressString.append(address.getAdminArea()); //浙江
//        //addressString.append(address.getSubAdminArea()); // null
//        addressString.append(address.getLocality()); //宁波
//        addressString.append(address.getSubLocality()); //区
//        if(address.getMaxAddressLineIndex() != -1){
//            addressString.append(address.getAddressLine(0));
//        }
//        addressString.append(address.getThoroughfare());  // 道路
        addressString.append(address.getFeatureName());
        return addressString.toString();
    }


    //GPS坐标转换高德坐标
    private AMapLocation fromGpsToAmap(Location location) {
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

    private void addCircle(LatLng latlng, double radius) {
        if (mCircle != null) {
            mCircle.remove();
        }
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addMarker(LatLng latlng) {
        if(mLocMarker != null){
            double[] coords = {lat,lgt,latlng.latitude,latlng.longitude};
            List<LatLng> points = new ArrayList<LatLng>();
            for (int i = 0; i < coords.length; i += 2) {
                points.add(new LatLng(coords[i], coords[i + 1]));
            }
            LatLngBounds.Builder b = LatLngBounds.builder();
            for (int i = 0 ; i < points.size(); i++) {
                b.include(points.get(i));
            }
            LatLngBounds bounds = b.build();
            //aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            mLocMarker.setVisible(false);
            mCircle.setVisible(false);
            SmoothMoveMarker moveMarker = new SmoothMoveMarker(aMap);
            // 设置滑动的图标
            moveMarker.setDescriptor(BitmapDescriptorFactory.fromView(this.getLayoutInflater().inflate(R.layout.located_marker,null)));
            moveMarker.setPoints(points);//设置平滑移动的轨迹list
            moveMarker.setTotalDuration(1);//设置平滑移动的总时间
            //moveMarker.getMarker().showInfoWindow();
            moveMarker.setMoveListener(
                    new SmoothMoveMarker.MoveListener() {
                        @Override
                        public void move(final double distance) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(distance == 0){
                                        //若在移动mLocMarkers时map.clear()清空了此marker,则重新添加该marker
                                        if(mLocMarker == null){
                                            mLocMarker = aMap.addMarker(mLocMarkerOption);
                                        }
                                        moveMarker.removeMarker();
                                        mLocMarker.setPosition(latlng);
                                        mLocMarker.setVisible(true);
                                        mCircle.setVisible(true);
                                    }
                                }
                            });
                        }
                    });
            moveMarker.startSmoothMove();
            return;
        }
        mLocMarkerOption = new MarkerOptions();
        mLocMarkerOption.icon(BitmapDescriptorFactory.fromView(this.getLayoutInflater().inflate(R.layout.located_marker,null)));
        mLocMarkerOption.anchor(0.5f, 0.5f);
        mLocMarkerOption.position(latlng);
        mLocMarker = aMap.addMarker(mLocMarkerOption);
        mLocMarker.setTitle(LOCATION_MARKER_FLAG);
    }

    protected void saveRecord(List<AMapLocation> list, String time, String strStartpoint) {
        if (list != null && list.size() > 0) {
            String duration = getDuration();
            float distance = getDistance(list);
            String average = getAverage(distance);
            String pathlineSring = getPathLineString(list);
            AMapLocation firstLocaiton = list.get(0);
            AMapLocation lastLocaiton = list.get(list.size() - 1);
            String stratpoint = amapLocationToString(firstLocaiton);
            String endpoint = amapLocationToString(lastLocaiton);
            if (continue_tracing) {
                duration = String.valueOf(Double.parseDouble(duration.trim()) + Double.valueOf(lastRecord.getDuration()));
                distance = getDistance(list) + Float.parseFloat(lastRecord.getDistance());
                average = String.valueOf(distance / Double.valueOf(getDuration()) + Double.valueOf(lastRecord.getDuration()) * 1000f);
                stratpoint = amapLocationToString(lastRecord.getStartpoint());
                continue_tracing = false;

                DbHepler = new DbTracks(this);
                DbHepler.open();
                DbHepler.updatelastrecord(String.valueOf(distance), duration, average,
                        pathlineSring, stratpoint, endpoint, strStartpoint, location, time);
                DbHepler.close();
            } else{
                DbHepler = new DbTracks(this);
                DbHepler.open();
                DbHepler.createrecord(String.valueOf(distance), duration, average,
                        pathlineSring, stratpoint, endpoint, strStartpoint, location, time);
                DbHepler.close();
            }
            Toast.makeText(getApplicationContext(),"轨迹录制已结束,此次总距离" + distance + "m",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(com.sunland.cpocr.activity.XLWCameraActivity.this, "没有记录到路径", Toast.LENGTH_SHORT)
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
        Intent intent = new Intent(com.sunland.cpocr.activity.XLWCameraActivity.this, TrackRecordActivity.class);
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
     * @param distance 总距离66
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
        firstFrame = true; //必须重置, 否则视频帧解码找到对应输出的surface
        mapView.onResume();
        mAMapNaviView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        mAMapNaviView.onPause();
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
        mAMapNaviView.onDestroy();
        mAMapNavi.stopNavi();
    }


    @Override
    public void onTraceProcessing(int i, int i1, List<LatLng> list) {

    }


    private SearchModuleDelegate.IParentDelegate mSearchModuleParentDelegate = new ISearchModule.IDelegate.IParentDelegate() {
        @Override
        public void onChangeCityName() {
            showToast("选择城市");
            Intent intent = new Intent();
            intent.setClass(XLWCameraActivity.this, CityChooseActivity.class);
            intent.putExtra(CityChooseActivity.CURR_CITY_KEY, mSearchModuelDeletage.getCurrCity().getCity());
            XLWCameraActivity.this.startActivityForResult(intent, LPRMAP_ACTIVITY_REQUEST_CHOOSE_CITY_ADDRESS_CODE);
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
        intent.setClass(XLWCameraActivity.this, SetFavAddressActivity.class);
        intent.putExtra(FAVTYPE_KEY, type);
        Gson gson = new Gson();
        intent.putExtra(SetFavAddressActivity.CURR_CITY_KEY, gson.toJson(mSearchModuelDeletage.getCurrCity()));
        startActivityForResult(intent, LPRMAP_ACTIVITY_REQUEST_FAV_ADDRESS_CODE);
    }

    private void saveToCache(PoiItem poiItem) {
        PoiItemDBHelper.getInstance().savePoiItem(XLWCameraActivity.this, poiItem);
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

        AlertDialog.Builder builder = DialogHelp.getConfirmDialog(XLWCameraActivity.this,
                "是否由 " + location + " 导航至 " + deslocation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle bundle = new Bundle();
                        bundle.putDouble("lat", lat);
                        bundle.putDouble("lgt", lgt);
                        bundle.putDouble("deslat", desLat);
                        bundle.putDouble("deslgt", desLgt);
                        Intent intent;
                        intent = new Intent(XLWCameraActivity.this, CalculateRouteActivity.class);
                        intent.putExtras(bundle);
                        continue_tracing = false;
                        if(istracing){
                            intent.putExtra(IS_TRACING_KEY, "true");
                            mEndTime = System.currentTimeMillis();
                            mOverlayList.add(mTraceoverlay);
                            LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
                            mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record.getPathline()) ,
                                    LBSTraceClient.TYPE_AMAP, XLWCameraActivity.this);
                            saveRecord(record.getPathline(), record.getDate(), record.getStrStartPoint());
                        } else{
                            intent.putExtra(IS_TRACING_KEY, "false");
                        }
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void handleIntent(Intent intent) {
        final String hphm = intent.getStringExtra(BaseOcrActivity.EXTRA_RET_HPHM);
        final String hpysStr = intent.getStringExtra(BaseOcrActivity.EXTRA_RET_HPYS_STR);
        final String[] hpzls = CpocrUtils.getHpzlFromOcr(hphm, hpysStr);
        final String hpzl = hpzls[0];
        final String hpzlStr = hpzls[1];
        Toast.makeText(this,hphm,Toast.LENGTH_SHORT).show();
        //adapter.add(hphm, hpzl, hpzlStr, hpysStr);
    }

    // 数据读取线程
    private class Reader implements Runnable {
        USBDataParser dataParser = new USBDataParser(); // 数据解析器

        @Override
        public void run() {
            while (!m_bExit) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!m_bOpen) {
                    continue;
                }

                if (MyUsbResult.GetListCount() < 1) {
                    continue;
                }

                // 取数据
                byte[] resultData = MyUsbResult.GetOneResult();
                if (resultData.length <= 0) {
                    continue;
                }

                // 解析数据成结果
                if (dataParser.putInResultData(resultData) == -1) {
                    continue;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time_Date = sdf.format(new Date(dataParser.m_result.m_lTime));
                //Log.i("USB", String.format("结果ID:%s\n车牌号:%s\n时间:%s", dataParser.m_result.m_strID, dataParser.m_result.m_strPlateNum, time_Date));

                runOnUiThread(() -> {
                    for (int i = 1; i < dataParser.m_result.m_BigImageList.size(); i++) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(dataParser.m_result.m_BigImageList.get(i).imageData, 0, dataParser.m_result.m_BigImageList.get(i).imageData.length);
                        //ImageView imageView = findViewById(R.id.imageView);
                        //imageView.setImageBitmap(bitmap);
                        String carNum = dataParser.m_result.m_strPlateNum.substring(1);
                        //String carType = carColor2CarType(dataParser.m_result.m_strPlateNum.substring(0, 0));
                        mVibrator.vibrate(300);
                        String pathPhoto = saveMyBitmap(bitmap);
                        dbCpHmZp = new DbCpHmZp(XLWCameraActivity.this);
                        dbCpHmZp.open();
                        dbCpHmZp.save_carinfo(carNum, dataParser.m_result.m_strPlateNum.substring(0,0) + "色", pathPhoto);
                        dbCpHmZp.close();
                        Toast.makeText(XLWCameraActivity.this, dataParser.m_result.m_strPlateNum,Toast.LENGTH_LONG).show();
                        break;
                    }
                });
            }

        }
    } // end 数据读取线程

    // 视频帧读取线程
    private class VideoReader implements Runnable {
        USBDataParser dataParser = new USBDataParser(); // 数据解析器

        @Override
        public void run() {
            while (!m_bExit) {
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!m_bOpen) {
                    continue;
                }

                if (MyUsbResult.GetVideoListCount() < 1) {
                    continue;
                }

                // 取数据
                byte[] videoData = MyUsbResult.GetOneFrame();
                if (videoData == null && videoData.length <= 0) {
                    continue;
                }

                // 解析数据成H264帧
                USBDataParser.OBCVideo obcVideo = dataParser.putInVideoData(videoData);
                if (obcVideo == null) {
                    continue;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time_Date = sdf.format(new Date(obcVideo.m_lTime));
                //Log.i("USB", String.format("视频帧时间:%s  宽度：%d  高度：%d", time_Date, obcVideo.m_iWidth, obcVideo.m_iHeight));

                // TODO:送到 MediaCodec 等解码播放
                if (firstFrame) {
                    //Log.d("SSSS", "de");
                    firstFrame = false;
                    mDecoder.configure(surfaceView.getHolder().getSurface(),
                            obcVideo.m_iWidth,
                            obcVideo.m_iHeight,
                            obcVideo.m_VideoData,
                            0,
                            obcVideo.m_VideoData.length);
                } else {
                    mDecoder.decodeSample(obcVideo.m_VideoData,
                            0,
                            obcVideo.m_VideoData.length,
                            obcVideo.m_lTime,
                            obcVideo.m_bKey ? 1 : 0);
                }
            }
        }
    } // end 数据读取线程

    //车牌颜色转车牌类型(ex: "蓝" -> "02")
    private String carColor2CarType(String carColor) {
        switch (carColor) {
            case ("蓝"):
                return "02";

            case ("绿"):
                return "52";

            case ("黄"):
                return "16";

            case ("白"):
                return "22";

            default:
                return "02";
        }
    }

    //保存文件到指定路径
    private String saveMyBitmap(Bitmap bitmap) {

        File sd = Environment.getExternalStorageDirectory();
        File destDir = new File(sd.getPath() + "/HPSB/");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        String tmpfile = sd.getPath() + "/HPSB/" + System.currentTimeMillis() + ".jpg";

        BufferedOutputStream bos = null;
        try {
            try {
                bos = new BufferedOutputStream(new FileOutputStream(tmpfile));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            bitmap.recycle();
        } catch (Exception e) {
            Log.e("vvv", "保存失败");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (bos != null)
                try {
                    bos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return tmpfile;
    }

    private boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    @Override
    //安卓重写返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK) {
            if (mSearchModuelDeletage.getWidget(this).getVisibility() ==  View.VISIBLE){
                mSearchModuelDeletage.getWidget(this).setVisibility(View.INVISIBLE);
            } else if (mAMapNaviView.getVisibility() == View.VISIBLE) {
                onNaviCancel();
            } else if (mapView.getVisibility() == View.VISIBLE) {
                if(istracing){
                    Toast.makeText(this, "当前正在记录巡逻轨迹，请先结束记录轨迹", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            } else {
                super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mDecoder = new VideoDecoder();
        mDecoder.start();
        firstFrame = true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
//        mDecoder = new VideoDecoder();
//        mDecoder.start();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mDecoder.stop();
    }


    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInitNaviSuccess() {
    }

    @Override
    public void onStartNavi(int type) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

    }

    @Override
    public void onGetNavigationText(int type, String text) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {
        mapView.setVisibility(View.VISIBLE);
        mAMapNaviView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onArriveDestination() {
        mapView.setVisibility(View.VISIBLE);
        mAMapNaviView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCalculateRouteFailure(int errorInfo) {
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int wayID) {

    }

    @Override
    public void onGpsOpenStatus(boolean enabled) {
    }

    @Override
    public void onNaviSetting() {
        Toast.makeText(this,"全蓝", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNaviMapMode(int isLock) {

    }

    @Override
    public void onNaviCancel() {
        mapView.setVisibility(View.VISIBLE);
        mAMapNaviView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNaviTurnClick() {
        Toast.makeText(this,"左上角转向", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {
    }

    @Deprecated
    @Override
    public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {
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
    public void onNaviInfoUpdate(NaviInfo naviinfo) {
    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

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
    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {

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
        if(b){
            //Toast.makeText(this,"GPS信号弱", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLockMap(boolean isLock) {
    }

    @Override
    public void onNaviViewLoaded() {
    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }
}