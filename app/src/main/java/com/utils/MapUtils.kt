package com.utils

import com.amap.api.maps.MapsInitializer

object MapUtils {

    fun initializer(apiKey: String?) {
        if(!apiKey.isNullOrEmpty()){
            MapsInitializer.setApiKey(apiKey)
            com.amap.api.maps2d.MapsInitializer.setApiKey(apiKey)
        }
    }
}