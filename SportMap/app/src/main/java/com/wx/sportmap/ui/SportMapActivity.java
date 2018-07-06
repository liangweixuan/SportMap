package com.wx.sportmap.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;
import com.wx.sportmap.R;
import com.wx.sportmap.map.LoacationBean;
import com.wx.sportmap.map.LocationService;
import com.wx.sportmap.map.trace.CommonUtil;
import com.wx.sportmap.map.trace.MapViewUtil;
import com.wx.sportmap.threads.ThreadPoolManager;
import com.wx.sportmap.ui.common.NormalTipDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.FutureTask;

import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * Created by corn on 2018/7/6.
 */

public class SportMapActivity extends AppCompatActivity implements View.OnClickListener{


    private TextView tv_title;
    private MapView mMapView;
    private Intent mLocationIntent;
    private double mCurrentWay = 0.0;
    private double mLatitude = 0;//纬度
    private double mLangtde = 0;//经度
    private volatile double mCurrentSpeed = 0.0;
    private boolean isStartLoc = false;

    private SensorManager mSensorManager = null;
    private Double lastX = 0.0;//角度
    private int mCurrentDirection = 0;
    private BlueToothStateReceiver mBlueToothStateReceiver;
    private final int UPDATE_SPEED = 0x855;    // 刷新速度
    private TextView tv_start;
    private TextView tv_stop;
    private TextView tv_distance;
    private TextView tv_speed;
    private TextView tv_latitude;
    private TextView tv_langtde;
    private TextView tv_direction;
    private DecimalFormat dfomat;
    private DecimalFormat dfomatDistance;

    private boolean isFinishTimer = false;//停止计时器
    private boolean isfinish = false;//界面销毁标识

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_map);
        initView();
        initData();
        initListener();
        initReceiver();
        openGPSSettings();
        initStartWork();
    }

    private void initData() {
        if (dfomat == null) {
            dfomat = new DecimalFormat("#0.0");
        }
        if (dfomatDistance == null)
            dfomatDistance = new DecimalFormat("#0.00");
    }


    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(R.string.run);
        mMapView = findViewById(R.id.mapView);

        tv_start = findViewById(R.id.tv_start);
        tv_stop = findViewById(R.id.tv_stop);
        tv_distance = findViewById(R.id.tv_distance);
        tv_speed = findViewById(R.id.tv_speed);
        tv_latitude = findViewById(R.id.tv_latitude);
        tv_langtde = findViewById(R.id.tv_Langtde);
        tv_direction = findViewById(R.id.tv_Direction);
    }

    private void initListener() {
        tv_start.setOnClickListener(this);
        tv_stop.setOnClickListener(this);
        tv_title.setOnClickListener(this);
    }

    private void initReceiver() {
        MapViewUtil.getInstance().init(mMapView);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);


        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationService.LOCATION_GET_DATA_MESSAGE);
        mBlueToothStateReceiver = new BlueToothStateReceiver();
        registerReceiver(mBlueToothStateReceiver, filter);
    }


    /**
     * 初始化工作线程
     */
    private void initStartWork() {
        try {
            ThreadPoolManager.getInstance().execte(new FutureTask<Object>(mSpeedRunnable, null));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
            MapViewUtil.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
            MapViewUtil.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        isStartLoc = false;
        LocationService.isStartLocation = false;
        if (mSensorManager != null && mSensorEventListener != null)
            mSensorManager.unregisterListener(mSensorEventListener);
        MapViewUtil.getInstance().stopTrace();
        MapViewUtil.getInstance().onDestory();
        super.onDestroy();
    }

    @Override
    public void finish() {
        isfinish = true;
        isFinishTimer = true;
        super.finish();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_start:
                startSport();
                break;
            case R.id.tv_stop :
                stopSport();
                break;
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what){
                case UPDATE_SPEED://更新速度
                    if (tv_speed != null) {
                        if (mCurrentSpeed < 0 || mCurrentSpeed < 0.01) {
                            mCurrentSpeed = 0;
                            needDeclineSpeed = false;
                        }
                        tv_speed.setText(dfomat.format(mCurrentSpeed) + "");
                    }
                    break;
            }

        }
    };







    private void startSport(){
        //开始画地图
        isStartLoc = true;
        LocationService.isStartLocation = true;
        MapViewUtil.getInstance().clear();
    }

    private void stopSport(){
        isStartLoc = false;
        LocationService.isStartLocation = false;
        MapViewUtil.getInstance().stopTrace();
        if (mLocationIntent != null) {
            stopService(mLocationIntent);
        }
    }


    /**
     * 方向传感器
     */
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //每次方向改变，重新给地图设置定位数据，用上一次的经纬度
            double x = sensorEvent.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX) > 1.0) {// 方向改变大于1度才设置，以免地图上的箭头转动过于频繁
                mCurrentDirection = (int) x;
                if (!CommonUtil.isZeroPoint(mLatitude, mLangtde)) {
                    MapViewUtil.getInstance().updateMapDirecte(mLatitude, mLangtde, mCurrentDirection);
                }
            }
            lastX = x;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };



    /**
     * 速度管理线程
     */
    private volatile boolean sportStopTip = true;    // 一开始为false，则不提醒用户，所以一旦用户开始运动了，则提示不要停止
    private volatile int stopSportPeriod = 0;        // 停止运动周期
    private volatile int userReadUIPeriod = 0;        // 让用户看UI的周期
    private boolean needDeclineSpeed;    // 判别是否需要开启倒计时线程
    private Runnable mSpeedRunnable = new Runnable() {
        @Override
        public void run() {
            Random r = new Random();
            while (!isFinishTimer && !isfinish) {
                userReadUIPeriod++;

                if (needDeclineSpeed && mCurrentSpeed != 0) {
                    if (userReadUIPeriod > 3) {
                        if (mCurrentSpeed > 0.6) {
                            mCurrentSpeed = mCurrentSpeed * (r.nextDouble() * 0.3 + 0.5);    // 下降的百分比为0.5~0.8之间的随机数
                        } else if (mCurrentSpeed <= 0.6 && mCurrentSpeed > 0) {
                            mCurrentSpeed -= (mCurrentSpeed * (r.nextDouble() * 0.2 + 0.8));            // 下降的百分比为1.0~0.8之间的随机数
                            if (mCurrentSpeed < 0) {
                                mCurrentSpeed = 0;
                                needDeclineSpeed = false;
                            }
                        } else if (mCurrentSpeed < 0) {
                            mCurrentSpeed = 0;
                            needDeclineSpeed = false;
                        }
                    }

                    mHandler.sendEmptyMessage(UPDATE_SPEED);
                }
                if (!needDeclineSpeed && mCurrentSpeed == 0) {
                    stopSportPeriod++;
                    if (stopSportPeriod > 5) {
                        // 停止了5个周期
                        if (!sportStopTip) {
//							LogUtils.e("停止了5个周期==>提醒用户不要停止运动");
                            sportStopTip = true;
                        }
                    }
                }
                SystemClock.sleep(500);
            }
        }
    };



    private class BlueToothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.length() > 0) {
              if (action.equals(LocationService.LOCATION_GET_DATA_MESSAGE)) {	//接收到移动位置数据
                    LoacationBean mLocationBean = (LoacationBean) intent.getSerializableExtra("locationdatamsg");
//                    LogUtils.e("langtde==="+mLocationBean.getLangtde()+"纬度==="+mLocationBean.getLatitude());
                    if (mLocationBean != null) {
                        mLangtde = mLocationBean.getLangtde();
                        mLatitude = mLocationBean.getLatitude();
                        mCurrentSpeed = mLocationBean.getSpeed();
                        // --------------------------------- 同传感器一样，判别速度，未测试
                        userReadUIPeriod = 0;
                        needDeclineSpeed = true;
                        stopSportPeriod = 0;
                        sportStopTip = true;
                        // ---------------------------------
                        mCurrentWay = mLocationBean.getWay();
                        mCurrentWay = mCurrentWay / 1000;
                        if (isStartLoc) {
                            MapViewUtil.getInstance().drawStartLine(mLatitude, mLangtde, mCurrentDirection, mCurrentSpeed);
                        } else {
                            MapViewUtil.getInstance().updateLoc(mLatitude, mLangtde, mCurrentDirection);
                        }
                    }
                }
            }
        }
    }







    /**************************************
     * GPS 权限
     ************************************************/

    private final int OPEN_GPS = 1314;

    private void openGPSSettings() {
        //判断GPS是否正常启动
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            new NormalTipDialog(this)
                    .setType(2)
                    .title(getString(R.string.gps_tips_for_sport))
                    .content(getString(R.string.sport_gps_tips)).setSingleButtonOnClick(new NormalTipDialog.SingleButtonOnClick() {
                @Override
                public void positive() {
                    //返回开启GPS导航设置界面
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, OPEN_GPS);
                }

                @Override
                public void negative() {
                    // 点击取消
                    finish();
                }
            }).show();

            return;
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                getlocationPression();
            } else {
                requestLocation();
            }
        }
    }


    public void getlocationPression() {
        List<String> list = new ArrayList<>();
        //运行时权限，没有注册就重新注册
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            list.add(READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!list.isEmpty()) {	//没有权限就添加
            String[] permissions = list.toArray(new String[list.size()]);//如果list不为空，就调用ActivityCompat.requestPermissions添加权限
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {	//有相关权限则执行程序
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "用户取消开启权限", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    requestLocation();//调用定位
                }
                break;
            default:
        }
    }

    private void requestLocation() {
        mLocationIntent = new Intent(getApplicationContext(), LocationService.class);
        startService(mLocationIntent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_GPS) {
            openGPSSettings();
        }
    }
/*****************************************GPS权限END******************************************************/
}
