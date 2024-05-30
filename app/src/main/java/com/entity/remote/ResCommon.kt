package com.entity.remote

data class ResCommon<T>(
    val errcode: Int,
    val errmsg: String,
    val data: T
)