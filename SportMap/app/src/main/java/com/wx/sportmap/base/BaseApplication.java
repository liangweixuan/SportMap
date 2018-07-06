package com.wx.sportmap.base;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by corn on 2018/7/6.
 */

public class BaseApplication extends Application{
    public static BaseApplication instance;
    public static BaseApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SDKInitializer.initialize(getApplicationContext());
    }

}
