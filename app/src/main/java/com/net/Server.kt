package com.net

import com.entity.remote.ResCommon
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import wongxd.common.net.netDSL.RequestWrapper
import wongxd.http
import java.lang.reflect.Type
import kotlin.coroutines.suspendCoroutine


object Server {

    private val gson: Gson by lazy {
        GsonBuilder().enableComplexMapKeySerialization().create()
    }

    suspend fun <R, T : Any> call(
        path: String,
        requestParams: R,
//        params: MutableMap<String, String>,
        method: String = RequestWrapper.METHOD_POST
    ) = suspendCancellableCoroutine<NetworkResponse<T>> { con ->
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
                val type: Type = object : TypeToken<ResCommon<T>>() {}.type
                val resData: ResCommon<T> = gson.fromJson(it, type)
                if (200 == resData.errcode) {
                    con.resumeWith(Result.success(NetworkResponse.Success(resData.data)))
                } else {
                    con.resumeWith(
                        Result.success(
                            NetworkResponse.BizError(
                                resData.errcode,
                                resData.errmsg
                            )
                        )
                    )
                }
            }

            onFail { code, msg ->
                con.resumeWith(Result.success(NetworkResponse.UnknownError(code, msg)))
            }
        }

    }

}