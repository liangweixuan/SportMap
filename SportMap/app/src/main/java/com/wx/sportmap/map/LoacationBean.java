package com.wx.sportmap.map;/**
 * Created by jd on 2017/5/12.
 */

import java.io.Serializable;

/**
 * created by wangpengfei at 2017/5/12
 */
public class LoacationBean implements Serializable {
    double latitude ;//纬度
    double langtde ;//经度
    double way;//距离
    double speed;//速度
    String time; //时间


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLangtde() {
        return langtde;
    }

    public void setLangtde(double langtde) {
        this.langtde = langtde;
    }

    public double getWay() {
        return way;
    }

    public void setWay(double way) {
        this.way = way;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
