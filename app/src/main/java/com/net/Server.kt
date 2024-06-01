package com.net

import android.util.Log
import com.entity.remote.ResCommon
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import wongxd.common.net.netDSL.RequestWrapper
import wongxd.http
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object Server {

    const val TAG = "lxhdServer"

    val gson: Gson by lazy {
        Gson()
    }

    suspend inline fun <R, reified T> call(
        path: String,
        requestParams: R,
        method: String = RequestWrapper.METHOD_POST
    ) = suspendCoroutine<NetworkResponse<ResCommon<T>>> { con ->
        http {
            this.url = path
            this.method = method
            this.params.putAll(
                gson.fromJson(
                    gson.toJson(requestParams),
                    object : TypeToken<HashMap<String, Any>>() {}.type
                )
            )

            onSuccess {
                Log.d(TAG,"=========net response start=========\n$it\n==========net response end============")
                val type: Type =
                    TypeToken.getParameterized(ResCommon::class.java, T::class.java).type
                val resData: ResCommon<T> = gson.fromJson(it, type)
                if (200 == resData.errcode) {
                    con.resume(NetworkResponse.Success(resData))
                } else {
                    con.resume(
                        NetworkResponse.BizError(
                            resData.errcode,
                            resData.errmsg
                        )
                    )
                }
            }

            onFail { code, msg ->
                con.resume(NetworkResponse.UnknownError(code, msg))
            }
        }

    }

}