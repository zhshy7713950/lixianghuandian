package com.utils

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLng
import com.ruimeng.things.App

object MapUtils {

    fun initializer(apiKey: String?) {
        if (!apiKey.isNullOrEmpty()) {
            MapsInitializer.setApiKey(apiKey)
            com.amap.api.maps2d.MapsInitializer.setApiKey(apiKey)
        }
    }

    fun calculateDistance(lat: Double, lng: Double): String {
        val distance = AMapUtils.calculateLineDistance(LatLng(lat, lng), LatLng(App.lat, App.lng))
        return if (distance >= 1000)
            "${String.format("%.2f", (distance / 1000))}km"
        else
            "${String.format("%.2f", distance)}m"
    }
}