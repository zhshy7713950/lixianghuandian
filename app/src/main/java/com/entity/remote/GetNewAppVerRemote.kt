package com.entity.remote

/**
 * 获取后管平台当前上架版本号响应
 *
 * @property updateVer 后管平台当前版本号，如 "1.0.42"
 */
data class GetNewAppVerRemote(
    val updateVer: String? = null
)
