package com.ruimeng.things.home

/**
 * Created by wongxd on 2018/11/12.
 */

data class BatteryOpenEvent(val isOpen:Boolean)

data class BatteryInfoChangeEvent(val device_id:String)