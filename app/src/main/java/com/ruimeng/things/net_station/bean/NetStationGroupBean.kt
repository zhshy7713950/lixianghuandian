package com.ruimeng.things.net_station.bean

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import com.utils.MODEL_48
import com.utils.MODEL_60

/**
 * 新版换电站网点接口 apiv6/cgstationnetwork/list 的返回结构。
 *
 * 结构：data -> list -> 单个元素为一个"聚合站点"。
 * 一个聚合站点里可能包含 1 个或多个电柜，电柜以其 id 作为 key。
 * 电柜内部字段与旧接口一致，故复用 [NetStationBean.Data.X]。
 */
data class NetStationGroupBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var isBelong: Int = 0,
        var city: String = "", // 宜昌市
        var city_id: String = "", // 420500
        // 每个元素是一个聚合站点（key 为电柜 id）
        var list: List<LinkedHashMap<String, NetStationBean.Data.X>> = listOf()
    )
}

/**
 * 聚合站点：地图上一个气球对应的一个或多个电柜。
 */
class StationGroup(
    val cabinets: List<NetStationBean.Data.X>
) {
    /** 该聚合站点在地图上对应 marker 的 id */
    var markerId: String = ""

    /** 用于展示定位的代表电柜（取第一个） */
    val first: NetStationBean.Data.X get() = cabinets.first()
    val lat: Double get() = first.lat
    val lng: Double get() = first.lng

    /**
     * 是否在线：只要有 1 个电柜在线，整体即视为在线。
     * 决定气球是否为灰色。
     */
    val isOnline: Int get() = if (cabinets.any { it.isOnline == 1 }) 1 else 0

    /**
     * 聚合可换电池数：规则与单电柜一致。
     * 若用户为 48/60/72，则累加各电柜对应型号的可换数；
     * 若未指明，则累加全部（72 + 60 + 48）。
     */
    fun getAvaModelNum(curV: String): Int =
        cabinets.fold(0) { acc, cabinet -> acc + cabinet.getAvaModelNum(curV) }
}

/**
 * 新接口专用解析器。
 *
 * 相比全局 gson，额外兼容了后端在没有电池时把 batTypeCount 返回为空数组 `[]`
 * 而不是空对象 `{}` 的情况，避免解析直接抛异常。
 */
object NetStationGroupParser {

    private val gson: Gson by lazy {
        val batTypeCountType =
            object : TypeToken<HashMap<String, HashMap<String, Int>>>() {}.type
        GsonBuilder()
            .registerTypeAdapter(
                batTypeCountType,
                JsonDeserializer<HashMap<String, HashMap<String, Int>>> { json, _, _ ->
                    val result = HashMap<String, HashMap<String, Int>>()
                    // 只有当它确实是一个对象时才解析，遇到 [] 或 null 直接返回空 map
                    if (json != null && json.isJsonObject) {
                        for ((volKey, volValue) in json.asJsonObject.entrySet()) {
                            val inner = HashMap<String, Int>()
                            if (volValue != null && volValue.isJsonObject) {
                                for ((ahKey, ahValue) in volValue.asJsonObject.entrySet()) {
                                    try {
                                        inner[ahKey] = ahValue.asInt
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                            result[volKey] = inner
                        }
                    }
                    result
                }
            )
            .create()
    }

    fun parse(json: String): NetStationGroupBean =
        gson.fromJson(json, NetStationGroupBean::class.java)
}

/**
 * 宜昌特殊处理：城市 420500 且用户电压为 48/60 时，过滤掉 cabinetType == "1" 的电柜。
 * 逻辑与旧的 [filterSelf] 保持一致，只是作用在聚合站点内部的电柜列表上。
 */
fun filterGroupCabinets(
    cityId: String,
    cabinets: List<NetStationBean.Data.X>,
    curV: String
): List<NetStationBean.Data.X> {
    return if (cityId == "420500" && (curV == MODEL_48 || curV == MODEL_60)) {
        cabinets.filter { it.cabinetType != "1" }
    } else {
        cabinets
    }
}
