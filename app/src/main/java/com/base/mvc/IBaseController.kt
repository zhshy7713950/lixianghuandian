package com.base.mvc

interface IBaseController<in V> {
    fun  bindView(view: V)
}