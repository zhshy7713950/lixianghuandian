package com.ruimeng.things

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.ruimeng.things.bean.UserInfoBean
import org.greenrobot.eventbus.EventBus
import wongxd.Config
import wongxd.common.gson
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2018/11/13.
 */
class UserInfoLiveData private constructor() : MutableLiveData<UserInfoBean.Data.UserInfo>() {

    companion object {
        const val STORE_KEY = "UserInfoLiveData"

        fun setToString(userInfo: UserInfoBean.Data.UserInfo) {
            getInstance().postValue(userInfo)
            Config.getDefault().stringCacheUtils.put(STORE_KEY, gson.toJson(userInfo))
        }

        fun getFromString(): UserInfoBean.Data.UserInfo {
            val json = Config.getDefault().stringCacheUtils.getAsString(STORE_KEY)
            if (json.isNullOrBlank()) {
                return UserInfoBean.Data.UserInfo()
            }
            val data: UserInfoBean.Data.UserInfo = json.toPOJO()
            getInstance().postValue(data)
            return data
        }

        private val sInstance: UserInfoLiveData by lazy { UserInfoLiveData() }

        fun getInstance(): UserInfoLiveData = sInstance

        /**
         * 获取当前语音开关状态
         *
         * @return 当前语音开关状态，true表示开启，false表示关闭
         */
        fun getCurrentVoiceSwitchStatus(): Boolean {
            val currentUserInfo = getInstance().value
            return currentUserInfo?.isVoiceActived == 1
        }


        fun refresh(callback: () -> Unit = {}) {
            http {
                url = Path.USERINFO

                onSuccess {
                    val result = it.toPOJO<UserInfoBean>().data.userinfo
                    setToString(result)
                    // 发送EventBus事件通知UserInfo更新
                    EventBus.getDefault().post(UserInfoUpdateEvent(result))
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

    /**
     * 获取当前语音开关状态
     * @return 当前语音开关状态，true表示开启，false表示关闭
     */
    fun getCurrentVoiceSwitchStatus(): Boolean {
        val currentUserInfo = getInstance().value
        return currentUserInfo?.isVoiceActived == 1
    }

}

// UserInfo更新事件
data class UserInfoUpdateEvent(val userInfo: UserInfoBean.Data.UserInfo)