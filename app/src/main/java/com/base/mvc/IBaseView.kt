package com.base.mvc

interface IBaseView<in C> {
    fun  bindCtl(ctl: C)
}