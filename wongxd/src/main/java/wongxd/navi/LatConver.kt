//package wongxd.navi
//
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import com.amap.api.maps.AMapException
//import com.amap.api.maps.AMapUtils
//import com.amap.api.maps.model.LatLng
//import com.amap.api.maps.model.NaviPara
//
////import com.amap.api.maps.model.LatLng
//
//
///**
// * 高德转百度（火星坐标gcj02ll–>百度坐标bd09ll）
// */
//fun gaoDeToBaidu(gd_lon: Double, gd_lat: Double): DoubleArray {
//
//    val bd_lat_lon = DoubleArray(2)
//
//    val PI = 3.14159265358979324 * 3000.0 / 180.0
//
//    val z = Math.sqrt(gd_lon * gd_lon + gd_lat * gd_lat) + 0.00002 * Math.sin(gd_lat * PI)
//
//    val theta = Math.atan2(gd_lat, gd_lon) + 0.000003 * Math.cos(gd_lon * PI)
//
//    bd_lat_lon[0] = z * Math.cos(theta) + 0.0065
//
//    bd_lat_lon[1] = z * Math.sin(theta) + 0.006
//
//    return bd_lat_lon
//
//}
//
//
///**
// * 百度转高德（百度坐标bd09ll–>火星坐标gcj02ll）
// */
//fun bdToGaoDe(bd_lat: Double, bd_lon: Double): DoubleArray {
//
//    val gd_lat_lon = DoubleArray(2)
//
//    val PI = 3.14159265358979324 * 3000.0 / 180.0
//
//    val x = bd_lon - 0.0065
//    val y = bd_lat - 0.006
//
//    val z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * PI)
//
//    val theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * PI)
//
//    gd_lat_lon[0] = z * Math.cos(theta)
//
//    gd_lat_lon[1] = z * Math.sin(theta)
//
//    return gd_lat_lon
//
//}
//
//
///***
// *
// *
// * @Description 调用高德去导航
// *
// * @param mLatLng
// *
// * @param type
// */
//
//fun startNavByAmap(mLatLng: LatLng, type: Int, mContext: Context) {
//
//
//    try {
//
//        // 构造导航参数
//
//        val naviPara = NaviPara()
//
//        // 设置终点位置
//
//        naviPara.setTargetPoint(mLatLng)
//
//        // 设置导航策略，这里是避免拥堵
//
//        naviPara.setNaviStyle(type)
//
//        // 调起高德地图导航
//
//        AMapUtils.openAMapNavi(naviPara, mContext)
//
//
//    } catch (e: AMapException) {
//
//        e.printStackTrace()
//    }
//
//}
//
///**
// * 调用高德导航：
// * URI调用方式（不想下载SDK包使用URI调用也很方便）
// */
//fun useGaoDe(context: Context, poi: String, lat: String, lon: String) {
//
//    val gddtIntent = Intent("android.intent.action.VIEW",
//
//            Uri.parse("androidamap://navi?sourceApplication=“name”&poiname="
//
//                    + poi + "&lat=" + lat + "&lon=" + lon + "&dev=" + 0
//
//                    + "&style=" + 4))
//
//    gddtIntent.addCategory("android.intent.category.DEFAULT")
//
//    gddtIntent.setPackage("com.autonavi.minimap")
//
//    gddtIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//
//    context.startActivity(gddtIntent)
//}
//
//
///**
// * 调用百度导航
// * 1.AndroidAPI调用方式
// *
// *
// */
////fun wongxd.navi.useBaiDu(context: Context, mLatS: Double, mLonS: Double, startName: String,
////             mLatE: Double, mLonE: Double, endName: String
////) {
////
////
////    val ptS = LatLng(mLatS, mLonE)
////
////    val ptE = LatLng(mLatE, mLonE)
////
////
////    val para = NaviParaOption()
////
////            .startPoint(ptS).endPoint(ptE)
////
////            .startName(startName).endName(endName)
////
////    try {
////
////        // 调起百度地图导航
////
////        BaiduMapNavigation.openBaiduMapNavi(para, context)
////
////    } catch (e: BaiduMapAppNotSupportNaviException) {
////
////        e.printStackTrace()
////
////    }
////
////}
//
//
//
///**
// *
// * 调用百度导航
// *
// */
//fun useBaiDu(context: Context,lonlat:Array<String>,type:String){
////   val  intent = Intent
////
////            .getIntent("intent://map/navi?location="
////
////                    + lonlat[1]
////
////                    + ","
////
////                    + lonlat[0]
////
////                    + "&type="
////
////                    + strategyBD
////
////                    +
////
////                    "&coordType=bd09ll&src=thirdapp.navi.公司名字#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end");
////
////    context.startActivity(intent)// 启动调用
//}
