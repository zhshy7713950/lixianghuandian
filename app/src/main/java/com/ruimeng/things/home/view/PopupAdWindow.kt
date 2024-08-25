package com.ruimeng.things.home.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.ruimeng.things.FgtMain
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtCouponPurchase
import com.ruimeng.things.home.bean.AdInfoBean
import wongxd.AtyWeb

class PopupAdWindow (private val activity: Activity,
                     private val promotions: AdInfoBean.Promotions
) : PopupWindow(activity) {

    init {
        contentView = View.inflate(activity, R.layout.popup_ad_layout, null)
        val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
        val ivContent = contentView.findViewById<ImageView>(R.id.ivContent)
        with(promotions){
            when(mediaType){
                "1" ->{
                    ivContent.visibility = View.VISIBLE
                    Glide.with(activity).load(mediaURL).into(ivContent)
                    ivContent.setOnClickListener {
                        when(operationType){
                            "0" ->{
                            }
                            "1" -> {
                                //优惠券购买
                                when(operationData?.type){
                                    "couponPurchase" -> {
                                        FgtMain.instance?.start(FgtCouponPurchase.newInstance(operationData.data))
                                    }
                                    else -> {

                                    }
                                }
                            }
                            "2" -> {//app内网页
                                AtyWeb.start(operationTitle,operationURL)
                            }
                            "3" -> {//外部浏览器
                                AtyWeb.startBrowser(activity,operationURL)
                            }
                        }
                        dismiss()
                    }
                }
                else ->{

                }
            }
        }
        ivClose.setOnClickListener {
            dismiss()
        }
        setBackgroundDrawable(ColorDrawable(Color.parseColor("#4A000000")))
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        isClippingEnabled = false
    }

    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.CENTER, 0, 0)
        }
    }
}

sealed class PopActionEvent{

}