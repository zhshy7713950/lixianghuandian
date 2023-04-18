package com.ruimeng.things

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.ruimeng.things.bean.UserInfoBean
import wongxd.Config
import wongxd.common.gson
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2018/11/13.
 */
class UserInfoLiveData private constructor() : MutableLiveData<UserInfoBean.Data.UserInfo>() {

    companion object {
        val STORE_KEY = "UserInfoLiveData"

        fun setToString(userInfo: UserInfoBean.Data.UserInfo) {
            getInstance().postValue(userInfo)
            Config.getDefault().stringCacheUtils.put(UserInfoLiveData.STORE_KEY, gson.toJson(userInfo))
        }

        fun getFromString(): UserInfoBean.Data.UserInfo {
            val json = Config.getDefault().stringCacheUtils.getAsString(UserInfoLiveData.STORE_KEY)
            if (json.isNullOrBlank()) {
                return UserInfoBean.Data.UserInfo()
            }
            val data: UserInfoBean.Data.UserInfo = json.toPOJO()
            getInstance().postValue(data)
            return data
        }

        private val sInstance: UserInfoLiveData by lazy { UserInfoLiveData() }

        fun getInstance(): UserInfoLiveData = sInstance


        fun refresh(callback: () -> Unit = {}) {
            http {
                url = Path.USERINFO

                onSuccess {
                    val result = it.toPOJO<UserInfoBean>().data.userinfo
                    setToString(result)
                    callback.invoke()
                }
            }
        }
    }


    fun simpleObserver(owner: LifecycleOwner, then: (UserInfoBean.Data.UserInfo) -> Unit) {
        observe(owner, Observer<UserInfoBean.Data.UserInfo> {
            it?.let(then)
        })
    }


}