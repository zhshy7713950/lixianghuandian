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
import com.entity.remote.Promotions
import com.ruimeng.things.FgtMain
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtCouponPurchase
import com.ruimeng.things.home.FgtExtendedGift
import com.ruimeng.things.home.FgtLotteryCoupon
import com.ruimeng.things.home.bean.BannerInfo
import com.ruimeng.things.home.bean.LuckyWheelLotteryBean
import com.ruimeng.things.me.activity.AtyWeb2
import com.ruimeng.things.share.FgtShare
import com.utils.WeChatHelper
import wongxd.base.FgtBase

class PopupAdWindow (private val fgtBase: FgtBase,
                     private val promotions: Promotions,
                     private val extraData: Any? = null
) : PopupWindow(fgtBase.requireActivity()) {

    init {
        val context = fgtBase.requireContext()
        contentView = View.inflate(context, R.layout.popup_ad_layout, null)
        val ivClose = contentView.findViewById<ImageView>(R.id.ivClose)
        val ivContent = contentView.findViewById<ImageView>(R.id.ivContent)
        with(promotions){
            when(mediaType){
                "1" ->{
                    ivContent.isVisible = true
                    Glide.with(context).load(mediaURL).into(ivContent)
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
                                handleInternalLink(operationURL,operationTitle,fgtBase)
                            }
                            "3" -> {//外部浏览器
                                AtyWeb2.startBrowser(context,operationURL)
                            }
                            "A" -> {
                                // 跳转抽奖福利页面
                                val grant = extraData?.let { 
                                    if (it is LuckyWheelLotteryBean.Data) it.sendCouponPrice else "0"
                                } ?: "0"
                                val own = extraData?.let { 
                                    if (it is LuckyWheelLotteryBean.Data) it.selfCouponPrice else "0"
                                } ?: "0"
                                val ownId = extraData?.let { 
                                    if (it is LuckyWheelLotteryBean.Data) it.selfCouponId else ""
                                } ?: ""
                                val price = extraData?.let { 
                                    if (it is LuckyWheelLotteryBean.Data) it.paymentPrice else "0"
                                } ?: "0"
                                val code = extraData?.let { 
                                    if (it is LuckyWheelLotteryBean.Data) it.lotteryReqNum else ""
                                } ?: ""
                                FgtMain.instance?.start(FgtLotteryCoupon.newInstance(grant, own, ownId, price, code))
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

    private fun handleInternalLink(linkUrl: String?, title: String?, fgt: FgtBase) {
        if (linkUrl.isNullOrBlank()) return
        when {
            linkUrl.startsWith("extendedGift://") -> {
                fgt.start(FgtExtendedGift.newInstance())
            }
            linkUrl.startsWith("annualReport://") -> {
                val path = "/pages/基础/年度报告/annualReport"
                WeChatHelper.launchWXMiniProgram(
                    fgt.requireContext(),
                    fgt.resources.getString(R.string.wx_appid),
                    path
                )
            }
            else -> {
                // 打开内部网页
                AtyWeb2.start(title,linkUrl)
            }
        }
    }


    fun show(view: View) {
        if (fgtBase.requireActivity().window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.CENTER, 0, 0)
        }
    }
}

sealed class PopActionEvent{

}