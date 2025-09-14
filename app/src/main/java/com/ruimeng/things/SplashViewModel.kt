package com.ruimeng.things

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.base.viewmodel.BaseViewModel
import com.entity.remote.GetThirdAdStatusRemote
import com.net.NetworkResponse
import com.net.call.BizService
import com.net.whenSuccess
import com.ruimeng.things.ads.AdManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 启动页ViewModel
 * 
 * 功能说明：
 * - 处理启动页的业务逻辑
 * - 请求广告开关状态接口
 */
class SplashViewModel : BaseViewModel() {
    
    companion object {
        private const val TAG = "SplashViewModel"
    }
}
