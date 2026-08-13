package com.ruimeng.things

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.ruimeng.things.bean.NoReadBean
import com.ruimeng.things.common.getJsonOrNull
import wongxd.Config
import wongxd.common.gson
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2018/11/13.
 */
class NoReadLiveData private constructor() : MutableLiveData<NoReadBean.Data>() {

    companion object {
        private val STORE_KEY = "NoReadLiveData"


        private var cacheVersion = -1

        fun setToString(noRead: NoReadBean.Data) {
            cacheVersion = noRead.ver
            getInstance().postValue(noRead)
            Config.getDefault().stringCacheUtils.put(
                STORE_KEY,
                gson.toJson(noRead)
            )
        }

        fun getFromString(): NoReadBean.Data {
            val data = Config.getDefault().stringCacheUtils.getJsonOrNull(
                STORE_KEY,
                NoReadBean.Data::class.java
            ) ?: return NoReadBean.Data()
            getInstance().postValue(data)
            return data
        }

        private val sInstance: NoReadLiveData by lazy { NoReadLiveData() }

        fun getInstance(): NoReadLiveData = sInstance


        fun refresh(callback: () -> Unit = {}) {

            http {
                url = PathV3.NOREAD
                params["ver"] =
                    (if (cacheVersion == -1) getFromString().ver else cacheVersion).toString()

                onSuccess {
                    try {
                        val result = it.toPOJO<NoReadBean>().data
                        setToString(result)
                        callback.invoke()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    fun simpleObserver(owner: LifecycleOwner, then: (NoReadBean.Data) -> Unit) {
        observe(owner, Observer<NoReadBean.Data> {
            it?.let(then)
        })
    }


}
