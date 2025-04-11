package com.ruimeng.things.home.helper

import android.app.Activity
import android.view.View
import com.entity.remote.AdInfoRemote
import com.ruimeng.things.home.view.PopupAdWindow
import wongxd.base.FgtBase

object AdPopHelper {

    fun showAdPop(fgtBase: FgtBase,adInfoRemote: AdInfoRemote,rootView: View){
        adInfoRemote.promotions?.forEach {
            when(it.promotionType){
                "0" ->{

                }
                "1" ->{
                    PopupAdWindow(fgtBase,it).show(rootView)
                }
            }
        }
    }
}