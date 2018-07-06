package com.wx.sportmap.map.trace;/**
 * Created by jd on 2017/9/20.
 */

import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import static com.wx.sportmap.map.trace.BitmapUtil.bmEnd;
import static com.wx.sportmap.map.trace.BitmapUtil.bmStart;


/**
 * created by wangpengfei at 2017/9/20
 */
public class MapViewUtil {
    private static MapViewUtil instance;

    private float mCurrentZoom = 18.0f;
    public MapView mapView = null;
    public BaiduMap baiduMap = null;

    public Overlay polylineOverlay = null;//路线
    private List<MapBean> points = null;//定位点,对应速度
    private List<LatLng> pointlist = null;//定位点
    private List<Integer> colorList = null;//颜色点
    public static MapViewUtil getInstance(){
        if (instance == null) {
            synchronized (MapViewUtil.class){
                instance = new MapViewUtil();
            }
        }
        return instance;
    }
    public void init(MapView view){
        BitmapUtil.init();
        mapView = view;
        baiduMap = mapView.getMap();
        // 隐藏百度的LOGO
        View child = mapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
        // 不显示地图上比例尺
        mapView.showScaleControl(false);
        // 不显示地图缩放控件（按钮控制栏）
        mapView.showZoomControls(false);
        baiduMap.setMyLocationEnabled(true);

        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING,true,null));
        baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {//缩放比例变化监听
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                mCurrentZoom = mapStatus.zoom;
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {

            }
        });
        points = new ArrayList<>();
        pointlist = new ArrayList<>();
        colorList = new ArrayList<>();
    }
    public void onResume() {
        if (null != mapView) {
            mapView.onResume();
        }
    }
    public void onPause() {
        if (null != mapView) {
            mapView.onPause();
        }
    }
    public void clear(){
        if (points != null){
            points.clear();
        }
        if (null != polylineOverlay) {
            polylineOverlay.remove();
        }
        if (null != baiduMap) {
            baiduMap.clear();
        }
        if (null != pointlist) {
            pointlist.clear();
        }
        if (null != colorList) {
            colorList.clear();
        }
    }

    public void onDestory(){
        if (null != polylineOverlay) {
            polylineOverlay.remove();
            polylineOverlay = null;
        }
        if (null != baiduMap) {
            baiduMap.clear();
            baiduMap = null;
        }
        if (null != mapView) {
            mapView.onDestroy();
            mapView = null;
        }
        if (points != null){
            points.clear();
            points = null;
        }
        if (null != pointlist) {
            pointlist.clear();
            pointlist = null;
        }
        if (null != colorList) {
            colorList.clear();
            colorList = null;
        }
        BitmapUtil.clear();
    }
    private LatLng convertMyLocation2Map(double lat, double lang) {
        return new LatLng(lat, lang);
    }
    // 添加终点图标
    public void drawEndPoint(LatLng endPoint) {
        if(baiduMap == null){
            return;
        }
        OverlayOptions endOptions = new MarkerOptions().position(endPoint)
                .icon(bmEnd).zIndex(9).draggable(true);
        baiduMap.addOverlay(endOptions);
    }
    // 更新当前坐标方向
    public void updateMapLocation(LatLng currentPoint, float direction) {
        if(currentPoint == null || baiduMap == null){
            return;
        }
        MyLocationData locData = new MyLocationData.Builder().accuracy(0).
                direction(direction).
                latitude(currentPoint.latitude).
                longitude(currentPoint.longitude).build();
        baiduMap.setMyLocationData(locData);

    }
    //滑动到终点
    public void animateMapStatus(List<MapBean> points) {
        if (null == points || points.isEmpty() || baiduMap == null ||  points.size() > 1 ){
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MapBean bean : points) {
            builder.include(bean.getLatlang());
        }
        MapStatusUpdate msUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build());
        baiduMap.animateMapStatus(msUpdate);
    }
    //滑动到终点
    public void animateMapStatus(LatLng point) {
        if( baiduMap == null){
            return;
        }
        MapStatus.Builder builder = new MapStatus.Builder();
        MapStatus mapStatus = builder.target(point).zoom(mCurrentZoom).build();
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }
    /**
     * 更新方向
     * @param latitude
     * @param langtde
     * @param currentDirection
     */
    public void updateMapDirecte(double latitude, double langtde, int currentDirection) {
        LatLng lng = convertMyLocation2Map(latitude,langtde);
        updateMapLocation(lng,currentDirection);
    }

    /**
     * 定位当前点
     * @param latitude
     * @param langtde
     * @param currentDirection
     */
    public void updateLoc(double latitude, double langtde, int currentDirection) {
        LatLng lng = convertMyLocation2Map(latitude,langtde);
        updateMapLocation(lng,currentDirection);
        animateMapStatus(lng);
    }
    /**
     * 实时更新
     * @param latitude
     * @param longitude
     * @param direction
     */
    private MapBean mapBean = null;
    public void drawStartLine(double latitude,double longitude,float direction,double speed){
        if (!CommonUtil.isZeroPoint(latitude, longitude)) {
            baiduMap.clear();
            if (points == null) {
                points = new ArrayList<>();
            }
            mapBean = new MapBean();
            LatLng currentLatLng = convertMyLocation2Map(latitude,longitude);
            mapBean.setLatlang(currentLatLng);
            mapBean.setSpeed(speed);
            points.add(mapBean);
            LatLng startPoint = points.get(0).getLatlang();
            OverlayOptions startOptions = new MarkerOptions().position(startPoint).icon(bmStart)
                    .zIndex(9).draggable(true);

            if (points.size() == 1) {
                baiduMap.addOverlay(startOptions);
                updateMapLocation(startPoint,direction);
                animateMapStatus(startPoint);
            }else {
                // 添加路线（轨迹）
                pointlist.clear();
                colorList.clear();
                for (MapBean point:points){
                    pointlist.add(point.getLatlang());
                    addColor(point.getSpeed());
                }
                OverlayOptions polylineOptions = new PolylineOptions().width(10)
                        .colorsValues(colorList).points(pointlist);
                baiduMap.addOverlay(startOptions);
                if (null != polylineOverlay) {
                    polylineOverlay.remove();
                    polylineOverlay = null;
                }
                polylineOverlay = baiduMap.addOverlay(polylineOptions);
                MapBean lastMapBean = points.get(points.size() - 1);
                updateMapLocation(lastMapBean.getLatlang(),direction);
                animateMapStatus(lastMapBean.getLatlang());
            }
        }
    }

    private void addColor(double speed) {
        if (speed >= 0 && speed < 5) { //0-5
            colorList.add(0xff6DC877);
        }else if (speed >= 5 && speed < 10) { //5-10
            colorList.add(0xffBADE38);
        }else if (speed >= 10 && speed < 15) { //10-15
            colorList.add(0xffFFEA00);
        }else {
            colorList.add(0xffFF9100);
        }

    }

    /**
     * 停止时
     */
    public void stopTrace(){
        if (points != null && points.size()>0) {
            baiduMap.clear();
            pointlist.clear();
            colorList.clear();

          /*  LatLng startPoint = points.get(0).getLatlang();
            LatLng endPoint = points.get(points.size() - 1).getLatlang();

            // 添加起点图标
            OverlayOptions startOptions = new MarkerOptions()
                    .position(startPoint).icon(bmStart)
                    .zIndex(9).draggable(true);

            // 添加路线（轨迹）
            pointlist.clear();
            colorList.clear();
            for (MapBean point:points){
                pointlist.add(point.getLatlang());
                addColor(point.getSpeed());
            }
            OverlayOptions polylineOptions = new PolylineOptions().width(10)
                    .colorsValues(colorList).points(pointlist);
            // 添加终点图标
            drawEndPoint(endPoint);
            baiduMap.addOverlay(startOptions);
            polylineOverlay = baiduMap.addOverlay(polylineOptions);
            animateMapStatus(points);*/
        }
    }
}
