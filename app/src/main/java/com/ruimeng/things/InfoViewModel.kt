package com.ruimeng.things

import androidx.lifecycle.ViewModel
import wongxd.Config
import wongxd.Http
import wongxd.common.createVM

/**
 * Created by wongxd on 2018/11/13.
 */
class InfoViewModel : ViewModel() {


    companion object {

        fun getDefault(): InfoViewModel = createVM(App.getMainAty())
    }

    val userInfo: UserInfoLiveData = UserInfoLiveData.getInstance()


    init {
        UserInfoLiveData.getFromString()
        Http.token = Config.getDefault().token

    }


}