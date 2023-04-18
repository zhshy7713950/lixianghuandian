package com.ruimeng.things.net_station;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

//import static com.amap.api.location.AMapLocationClient.updatePrivacyAgree;
//import static com.amap.api.location.AMapLocationClient.updatePrivacyShow;

/**
 * User: LJM * Date&Time: 2016-08-17 & 22:36 * Describe: 获取经纬度工具类
 * * * 需要权限 *
 * <!--用于进行网络定位-->
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"></uses-permission>
 * <!--用于访问GPS定位-->
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"></uses-permission>
 * <!--获取运营商信息，用于支持提供运营商信息相关的接口-->
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
 * <!--用于访问wifi网络信息，wifi信息会用于进行网络定位-->
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
 * <!--这个权限用于获取wifi的获取权限，wifi信息会用来进行网络定位-->
 * <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
 * <!--用于访问网络，网络定位需要上网-->
 * <uses-permission android:name="android.permission.INTERNET"></uses-permission>
 * <!--用于读取手机当前的状态-->
 * <uses-permission android:name="android.permission.READ_PHONE_STATE"></uses-permission>
 * <!--写入扩展存储，向扩展卡写入数据，用于写入缓存定位数据-->
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
 * <p>
 * <p>
 * 需要在application 配置的mate-data 和sevice
 * <p>
 * <service android:name="com.amap.api.location.APSService" ></service>
 * <meta-data android:name="com.amap.api.v2.apikey" android:value="60f458d237f0494627e196293d49db7e"/>
 * <p>
 * 另外，还需要一个key xxx.jks *
 */
public class AMapLocUtils {
    private AMapLocationClient locationClient = null; // 定位
    private AMapLocationClientOption locationOption = null; // 定位设置

    private LonLatListener mLonLatListener;

    public void getLonLat(Context context, LonLatListener lonLatListener) {
        mLonLatListener = lonLatListener;
//        updatePrivacyShow(context, true, true);
//        updatePrivacyAgree(context, true);
        try {
            locationClient = new AMapLocationClient(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);// 设置定位模式为高精度模式 locationClient.setLocationListener(this);// 设置定位监听 locationOption.setOnceLocation(false); // 单次定位 locationOption.setNeedAddress(true);//逆地理编码 mLonLatListener = lonLatListener;//接口 locationClient.setLocationOption(locationOption);// 设置定位参数 locationClient.startLocation(); // 启动定位 } public interface LonLatListener{ void getLonLat(AMapLocation aMapLocation); } }
        locationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        locationOption.setOnceLocation(true);
        locationOption.setLocationCacheEnable(true);
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                mLonLatListener.getLonLat(aMapLocation);
                locationClient.stopLocation();
                locationClient.onDestroy();
                locationClient = null;
                locationOption = null;
            }
        });
        locationClient.startLocation();
    }

    public interface LonLatListener {
        void getLonLat(AMapLocation aMapLocation);
    }

}
