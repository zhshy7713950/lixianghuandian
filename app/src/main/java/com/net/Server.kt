package com.net

import wongxd.common.net.netDSL.RequestWrapper
import wongxd.http
import kotlin.coroutines.suspendCoroutine

object Server {

    suspend inline fun <RESULT> request(
        path: String,
        params: MutableMap<String,String>,
        method: String = RequestWrapper.METHOD_POST
    ) = suspendCoroutine<NetworkResponse<Any>> {
        http {
            this.url = path
            this.method = method
            this.params.putAll(params)
        }

    }

}