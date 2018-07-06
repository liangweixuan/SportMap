package com.wx.sportmap.map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.wx.sportmap.utils.DateUtils;
import com.wx.sportmap.utils.LogUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LocationService extends Service {

    private String TAG = "LocationService";
    private Context mContext = null;
    public LocationClient mLocationClient = null;
    public static final String LOCATION_GET_DATA_MESSAGE = "LOCATION_GET_DATA_MESSAGE";
    public static boolean isStartLocation = false;
    private Object lock = new Object();
    private MyLocationListenner myListener = new MyLocationListenner();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private ArrayList<Zuobiao> mZuobiaos ;
    public HashMap<Integer,Double> mDistances;
    private double distance = 0;//米
    private float speed1 = 0;
    public double mSaveSpeed = 0;

    private boolean isFirstLoc = true;
    private List<LatLng> points = null;
    private LatLng last = null;

    private double mAllDistance = 0;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(myListener);
        initLocation();

        //注册监听函数
        mLocationClient.start();
        mLocationClient.requestLocation();

        mZuobiaos = new ArrayList<>();
        mDistances = new HashMap();
        points = new ArrayList<>();
        mAllDistance = 0;
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span=5000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        option.disableCache(true);    //禁止启用缓存定位

        mLocationClient.setLocOption(option);

    }
    @Override
    public void onDestroy() {
        if (null != mLocationClient) {
            mLocationClient.stop();
            mLocationClient = null;
        }
        mAllDistance = 0;
        super.onDestroy();
    }
    private int outOfDistance = 0;
    private class Task implements Callable<String> {

        private BDLocation location;
        public Task(BDLocation location){
            this.location = location;
        }

        @Override
        public String call() throws Exception {
            synchronized (lock) {
                int loctype = location.getLocType();
                LogUtils.e("type="+location.getLocType());
                if (loctype == BDLocation.TypeGpsLocation|| loctype == BDLocation.TypeNetWorkLocation) {//只要gps点
                    Zuobiao zuobiao = new Zuobiao();
                    zuobiao.setLangtde(location.getLongitude());
                    zuobiao.setLatitude(location.getLatitude());
                    zuobiao.setTime(location.getTime());
                    zuobiao.setSpeed(location.getSpeed());
                    double distace1 = 0;
                    if (isFirstLoc) {//首次定位
                        outOfDistance = 0;
                        LatLng ll = getMostAccuracyLocation(location);
                        if(ll != null){
                            mZuobiaos.add(zuobiao);
                            isFirstLoc = false;
                            LoacationBean  locationBean = new LoacationBean();//处理后需要的数据
                            locationBean.setLangtde(location.getLongitude());
                            locationBean.setLatitude(location.getLatitude());
                            locationBean.setTime(location.getTime());
                            locationBean.setSpeed(0);
                            locationBean.setWay(0);
                            Intent intent = new Intent();
                            //设置intent的动作为com.example.broadcast，可以任意定义
                            intent.setAction(LOCATION_GET_DATA_MESSAGE);
                            intent.putExtra("locationdatamsg",locationBean);
                            //发送无序广播
                            mContext.sendBroadcast(intent);
                        }
                    }else {
                        if (loctype != BDLocation.TypeGpsLocation) return null;
                        Zuobiao nowZb1 = mZuobiaos.get(mZuobiaos.size()-1);
                        LatLng last =  new LatLng(nowZb1.getLatitude(), nowZb1.getLangtde());
                        LatLng currentLatLng=  new LatLng(zuobiao.getLatitude(), zuobiao.getLangtde());
                        distace1 = DistanceUtil.getDistance(last, currentLatLng);
                        //过滤值与采集频率有关 2017-10-10 11:17:54
            /*            long sTime =  DateUtils.getSs(nowZb1.getTime(),zuobiao.getTime());
                        LogUtils.e("最新坐标的波动距离"+distace1+"==时间=="+sTime);
                        if (distace1 <  5 || distace1 > 50) {
                            LogUtils.e("最新坐标与上个坐标<5保证>50");
                        }else {
                            mZuobiaos.add(zuobiao);
                        }
                        */
                        if (distace1 <  5 || distace1 > 50) {
                            outOfDistance ++;
                        }else {
                            outOfDistance = 0;
                        }
                        if (outOfDistance > 2 || outOfDistance == 0) {
                            mZuobiaos.add(zuobiao);
                            outOfDistance = 0;
                        }
                    }
                    if (loctype != BDLocation.TypeGpsLocation) return null;
                    int zbSize = mZuobiaos.size();
                    if ( zbSize > 1) {//这里起始坐标会有点问题
                        LoacationBean  locationBean = new LoacationBean();//处理后需要的数据
                        locationBean.setLangtde(location.getLongitude());
                        locationBean.setLatitude(location.getLatitude());
                        locationBean.setTime(location.getTime());
                        Zuobiao nowZb1 = mZuobiaos.get(zbSize-1);
                        Zuobiao nowZb2 = mZuobiaos.get(zbSize-2);
                        long timeWay = DateUtils.getSs(nowZb1.getTime(),nowZb2.getTime());
                        double way1 = distace1/timeWay *3.6;//我算的速度
                        speed1 = location.getSpeed();
                        boolean isStop = true;
                        for (int i = 0; i < zbSize; i++) {
                            Zuobiao zb = mZuobiaos.get(i);
                            if (zb.getSpeed() != 0) {
                                isStop = false;
                            }
                        }
                        if (isStop){
                            mSaveSpeed = 0;
                        }else if (speed1 > 0) {//百度速度
                            mSaveSpeed = speed1;
                        }else {
                            if(mSaveSpeed - way1 > 8){
                                mSaveSpeed = mSaveSpeed - new Random().nextInt(3)-new Random().nextInt(9)*0.1;
                            }else if(mSaveSpeed - way1 <-8){
                                mSaveSpeed = mSaveSpeed + new Random().nextInt(3)+new Random().nextInt(9)*0.1;
                            }else if(mSaveSpeed - way1 > 5){
                                mSaveSpeed = mSaveSpeed - new Random().nextInt(1)-new Random().nextInt(9)*0.1;
                            }else if(mSaveSpeed - way1 <-5){
                                mSaveSpeed = mSaveSpeed + new Random().nextInt(1)+new Random().nextInt(9)*0.1;
                            }else if(mSaveSpeed - way1 > 3){
                                mSaveSpeed = mSaveSpeed - new Random().nextInt(9)*0.1;
                            }else if(mSaveSpeed - way1 <-3){
                                mSaveSpeed = mSaveSpeed + new Random().nextInt(9)*0.1;
                            }else {
                                mSaveSpeed = speed1;
                            }
                        }
                        if (mSaveSpeed >= 0) {
                            if (isStartLocation) {
                                mAllDistance = mAllDistance+distace1;
                            }else {
                                mSaveSpeed = 0;
                                mAllDistance = 0;
                            }
                            double way2 = new BigDecimal(mSaveSpeed).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            double distace2 = new BigDecimal(mAllDistance).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            locationBean.setSpeed(way2);
                            locationBean.setWay(distace2);
                            Intent intent = new Intent();
                            //设置intent的动作为com.example.broadcast，可以任意定义
                            intent.setAction(LOCATION_GET_DATA_MESSAGE);
                            intent.putExtra("locationdatamsg",locationBean);
                            //发送无序广播
                            mContext.sendBroadcast(intent);
                        }
                        mZuobiaos.remove(0);
                    }
                }else {
                    LogUtils.e("未在gps类型下");
                }
                return null;
            }
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location != null &&(location.getLatitude() != 4.9E-324)&&(location.getLongitude() != 4.9E-324)){
                executor.submit(new Task(location));
            }
            //获取定位结果
            StringBuffer sb = new StringBuffer(256);
            sb.append("\terror code : ");
            sb.append(location.getLocType());    //获取类型类型
            sb.append("\tlatitude : ");
            sb.append(location.getLatitude());    //获取纬度信息
            sb.append("\tlontitude : ");
            sb.append(location.getLongitude());    //获取经度信息
            sb.append("\tradius : ");
            sb.append(location.getRadius());    //获取定位精准度
            sb.append("\tspeed : ");
            sb.append(location.getSpeed());    //获取定位精准度
            sb.append("\ttime : ");
            sb.append(location.getTime());    //获取定位精准度
            LogUtils.edd(TAG,sb.toString());
        }
    }
    private LatLng getMostAccuracyLocation(BDLocation location){
        LogUtils.e("radius="+location.getRadius());
        /*if (location.getRadius()>25) {//gps位置精度大于25米的点直接弃用
            ToastUtils.getInstance().showToast("gps位置精度大于25米的点直接弃用");
            return null;
        }*/
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());

        if (DistanceUtil.getDistance(last, ll ) > 5) {//保证第一个坐标稳定
            last = ll;
            points.clear();//有两点位置大于5，重新来过
            LogUtils.e("有两点位置大于5，重新来过,保证第一个坐标稳定");
            return null;
        }
        points.add(ll);
        last = ll;
        //有5个连续的点之间的距离小于5，认为gps已稳定，以最新的点为起始点
        if(points.size() >= 3){
            points.clear();
            return ll;
        }
        return null;
    }
}
