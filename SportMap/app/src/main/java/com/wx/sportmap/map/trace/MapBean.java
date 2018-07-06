package com.wx.sportmap.map.trace;

import com.baidu.mapapi.model.LatLng;

/**
 * created by wangpengfei at 2017/9/25
 */
public class MapBean {
    private LatLng latlang ;
    private double speed;

    public LatLng getLatlang() {
        return latlang;
    }

    public void setLatlang(LatLng latlang) {
        this.latlang = latlang;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
