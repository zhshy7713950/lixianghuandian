package com.ruimeng.things

import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.local.GetMapKeyLocal
import com.net.call.BizService
import com.net.whenSuccess
import com.utils.MapUtils
import kotlinx.coroutines.launch
import wongxd.utils.utilcode.util.SPUtils

class MainViewModel : BaseViewModel() {
    fun getMapKey() {
        val localMapKey = SPUtils.getInstance().getString("map_key")
        MapUtils.initializer(localMapKey)
        viewModelScope.launch {
            BizService.getAMapKey(GetMapKeyLocal()).whenSuccess {
                it.data?.let {apiKey ->
                    if(apiKey.isNotEmpty()){
                        SPUtils.getInstance().put("map_key", apiKey)
                        MapUtils.initializer(apiKey)
                    }
                }
            }
        }
    }

}