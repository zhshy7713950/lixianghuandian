package wongxd.navi

import android.content.Intent
import android.util.Log
import wongxd.Wongxd
import wongxd.base.AppManager
import wongxd.common.checkPackage
import java.net.URISyntaxException


/**
 * Created by wongxd on 2018/11/12.
 */


object NaviUtil {

    private val TAG = "导航工具"



    /**
     * 高德转百度（火星坐标gcj02ll–>百度坐标bd09ll）
     */
    fun gaoDeToBaidu(gd_lat: Double, gd_lon: Double): DoubleArray {

        val bd_lat_lon = DoubleArray(2)

        val PI = 3.14159265358979324 * 3000.0 / 180.0

        val z = Math.sqrt(gd_lon * gd_lon + gd_lat * gd_lat) + 0.00002 * Math.sin(gd_lat * PI)

        val theta = Math.atan2(gd_lat, gd_lon) + 0.000003 * Math.cos(gd_lon * PI)

        bd_lat_lon[0] = z * Math.cos(theta) + 0.0065

        bd_lat_lon[1] = z * Math.sin(theta) + 0.006

        return bd_lat_lon

    }


    /**
     * 百度转高德（百度坐标bd09ll–>火星坐标gcj02ll）
     */
    fun bdToGaoDe(bd_lat: Double, bd_lon: Double): DoubleArray {

        val gd_lat_lon = DoubleArray(2)

        val PI = 3.14159265358979324 * 3000.0 / 180.0

        val x = bd_lon - 0.0065
        val y = bd_lat - 0.006

        val z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI)

        val theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI)

        gd_lat_lon[0] = z * Math.cos(theta)

        gd_lat_lon[1] = z * Math.sin(theta)

        return gd_lat_lon

    }


    /**
     *
     * 确定起终点坐标BY高德
     *
     *
     * @param sname 起点名
     *
     * @param dname 终点名
     *
     */

    fun setUpGaodeAppByLoca(
        softname: String,
        latitude_a: String, longtitude_a: String, sname: String,
        latitude_b: String, longtitude_b: String, dname: String
    ) {

        try {

            val intent =
                Intent.getIntent("androidamap://route?sourceApplication=$softname&slat=$latitude_a&slon=$longtitude_a&sname=$sname&dlat=$latitude_b&dlon=$longtitude_b&dname=$dname&dev=0&m=0&t=1")

            if (isInstallByread("com.autonavi.minimap")) {

                AppManager.getAppManager().currentActivity().startActivity(intent)

                Log.e(TAG, "高德地图客户端已经安装")

            } else {

                Log.e(TAG, "没有安装高德地图客户端")

            }

        } catch (e: URISyntaxException) {

            e.printStackTrace()

        }

    }


    /**
     *
     * 确认起终点名称BY高德
     *
     * @param sname 起点名
     *
     * @param dname 终点名
     *
     */

    fun setUpGaodeAppByName(
        softname: String,
        sname: String,
        dname: String
    ) {

        try {

            val intent =
                Intent.getIntent("androidamap://route?sourceApplication=$softname" + "&sname=" + sname + "&dname=" + dname + "&dev=0&m=0&t=1")

            if (isInstallByread("com.autonavi.minimap")) {

                AppManager.getAppManager().currentActivity().startActivity(intent)

                Log.e(TAG, "高德地图客户端已经安装")

            } else {

                Log.e(TAG, "没有安装高德地图客户端")

            }

        } catch (e: URISyntaxException) {

            e.printStackTrace()

        }

    }


    /**
     *
     * 我的位置BY高德
     *
     * @param sname 起点名
     *
     * @param dname 终点名
     */

    fun setUpGaodeAppByMine(
        softname: String,
        sname: String,
        latitude_b: String, longtitude_b: String, dname: String
    ) {

        try {

            val intent =
                Intent.getIntent("androidamap://route?sourceApplication=$softname&sname=$sname&dlat=$latitude_b&dlon=$longtitude_b&dname=$dname&dev=0&m=0&t=1")

            if (isInstallByread("com.autonavi.minimap")) {

                AppManager.getAppManager().currentActivity().startActivity(intent)

                Log.e(TAG, "高德地图客户端已经安装")

            } else {

                Log.e(TAG, "没有安装高德地图客户端")

            }

        } catch (e: URISyntaxException) {

            e.printStackTrace()

        }

    }


    /**
     *
     * 注意下面的起终点坐标都是百度坐标，如果使用高德坐标系有很大的误差
     *
     */

    fun setUpBaiduAPPByLoca(
        latitude_qidian: String, longtitude_qidian: String, startName: String,
        latitude_zhongdian: String, longtitude_zhongdian: String, endName: String,
        yourCompanyName: String, yourAppName: String
    ) {

        try {

            val intent =
                Intent.getIntent("intent://map/direction?origin=latlng:$latitude_qidian,$longtitude_qidian|name:$startName&destination=latlng:$latitude_zhongdian,$longtitude_zhongdian|name:$endName&mode=driving&src=$yourCompanyName|$yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end")

            if (isInstallByread("com.baidu.BaiduMap")) {

                AppManager.getAppManager().currentActivity().startActivity(intent)

                Log.e(TAG, "百度地图客户端已经安装")

            } else {

                Log.e(TAG, "没有安装百度地图客户端")

            }

        } catch (e: URISyntaxException) {

            e.printStackTrace()

        }

    }


    /**
     *
     * 通过起终点名字使用百度地图
     *
     * @param origin 起点
     * @param destination 终点
     *
     */

    fun setUpBaiduAPPByEndName(origin: String, destination: String, yourCompanyName: String, yourAppName: String) {

        try {

            val intent =
                Intent.getIntent("intent://map/direction?origin=$origin&destination=$destination&mode=driving&src=$yourCompanyName|$yourAppName#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end")

            if (isInstallByread("com.baidu.BaiduMap")) {

                AppManager.getAppManager().currentActivity().startActivity(intent)

                Log.e(TAG, "百度地图客户端已经安装")

            } else {

                Log.e(TAG, "没有安装百度地图客户端")

            }

        } catch (e: URISyntaxException) {

            e.printStackTrace()

        }

    }


    /**
     *
     * 判断是否安装目标应用
     *
     * @param packageName 目标应用安装后的包名
     *
     * @return 是否已安装目标应用
     */

    private fun isInstallByread(packageName: String): Boolean {

//    return File("/data/data/$packageName").exists()

        return checkPackage(Wongxd.instance, packageName)
    }

}