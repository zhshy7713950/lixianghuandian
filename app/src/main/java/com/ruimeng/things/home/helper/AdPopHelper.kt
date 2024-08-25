package com.ruimeng.things.home.helper

import android.app.Activity
import android.view.View
import com.ruimeng.things.home.bean.AdInfoBean
import com.ruimeng.things.home.view.PopupAdWindow

object AdPopHelper {

    fun showAdPop(activity: Activity, adInfoData: AdInfoBean.Data, rootView: View){
        adInfoData.promotions?.forEach {
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