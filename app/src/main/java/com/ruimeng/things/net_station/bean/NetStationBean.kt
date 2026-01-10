package com.ruimeng.things.net_station.bean

import android.os.Parcelable
import com.amap.api.maps.model.Marker
import com.ruimeng.things.home.FgtHome
import com.utils.MODEL_48
import com.utils.MODEL_60
import com.utils.MODEL_72
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

data class NetStationBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var city: String = "", // 上海市
        var city_id: String = "", // 2
        var list: List<X> = listOf()
    ) {
        @Parcelize
        data class X(
            var address: String = "", // 成都市高新区天府五街6号
            var lat: Double = 0.0, // 30.542695
            var lng: Double = 0.0, // 104.059652
            var id: String = "",
            var site_name: String = "", // 服务中心4
            var cabinetType: String = "",//柜子类型
            var tag: String = "",
            var count: String = "",
            var available_battery : String = "",
            var available_arr: Model = Model(),
            var markerId: String = "" ,
            var tel: String = "" ,// 028-85214458
            var isOnline : Int = 0,
            var isGreen :Int = 0,
            var distance :Float = 0.0f,
            var distanceStr :String = "",
            var telData:List<TelData> = listOf(),
            var workTime: String = "",
            var cellNum: String = "",
            var siteImages: List<String> = listOf(),
            // 新增：不同伏数与安数的可换数统计
            var batTypeCount: @RawValue HashMap<String, HashMap<String, Int>> = hashMapOf(),
            // 新增：所属城市ID（由父级 Data.city_id 传入）
            var cityId: String = "",
        ): Parcelable

        @Parcelize
        data class TelData(
            var duration: String = "",
            var phone: String = ""
        ): Parcelable

        @Parcelize
        data class Model(
            var model_72: Int = 0,
            var model_60: Int = 0,
            var model_48: Int = 0,
        ): Parcelable
    }
}

fun NetStationBean.Data.filterSelf(curV: String):NetStationBean.Data {
    if(this.city_id == "420500" && (curV == MODEL_48 || curV == MODEL_60)){
        this.list = this.list.filter {
            it.cabinetType != "1"
        }.toList()
    }
    return this
}

fun NetStationBean.Data.X.getAvaModelNum(curV: String) = when (curV) {
    MODEL_72 -> {
        var modelNum =available_arr.model_72
        if (this.cityId == "510500" && FgtHome.hasOnlyAh50For72) {
            val override = this.batTypeCount[MODEL_72]?.get("50")
            if (override != null) modelNum = override
        }
        modelNum
    }
    MODEL_60 -> available_arr.model_60
    MODEL_48 -> available_arr.model_48
    else -> available_arr.model_72 + available_arr.model_60 + available_arr.model_48
}