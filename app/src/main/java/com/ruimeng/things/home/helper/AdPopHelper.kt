package com.ruimeng.things.home.helper

import android.app.Activity
import android.view.View
import com.entity.remote.AdInfoRemote
import com.ruimeng.things.home.view.PopupAdWindow

object AdPopHelper {

    fun showAdPop(activity: Activity,adInfoRemote: AdInfoRemote,rootView: View){
        adInfoRemote.promotions?.forEach {
            when(it.promotionType){
                "0" ->{

                }
                "1" ->{
                    PopupAdWindow(activity,it).show(rootView)
                }
            }
        }
    }
}