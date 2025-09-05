package com.ruimeng.things.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import com.ruimeng.things.R
import com.ruimeng.things.adapter.BannerImageCommonAdapter
import com.ruimeng.things.net_station.FgtNetStationDetailTwo
import com.ruimeng.things.share.FgtShare
import com.utils.WeChatHelper
import com.youth.banner.Banner
import com.youth.banner.indicator.CircleIndicator
import wongxd.base.FgtBase
import wongxd.utils.utilcode.util.ScreenUtils
import com.ruimeng.things.home.bean.BannerInfo
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.bumptech.glide.Glide
import com.entity.remote.AdInfoRemote
import com.ruimeng.things.home.FgtExtendedGift
import com.ruimeng.things.home.FgtCouponPurchase
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.activity.AtyWeb2
import com.entity.remote.OperationInnerData
import com.entity.remote.OperationData
import wongxd.common.toPOJO
import wongxd.http
import wongxd.common.EasyToast
import com.ruimeng.things.App

object BannerHelper {
    fun initCommonBanner(
        banner: Banner<*, *>,
        fgt: FgtBase
    ) {
        banner.apply {
            layoutParams?.height = (ScreenUtils.getScreenWidth() * 0.22f).toInt()
            val dataList = listOf(
                "https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/SpringFestival-banner.png",
                "https://downxll.oss-cn-beijing.aliyuncs.com/frontAd/wxMin.png"
            )
            (this as Banner<String, BannerImageCommonAdapter>)
                .setAdapter(BannerImageCommonAdapter(dataList).apply {
                    this.setOnBannerListener { _, position ->
                        when (position) {
                            0 -> {
                                fgt.start(FgtShare.newInstance())
                            }

                            1 -> {
                                WeChatHelper.launchWXMiniProgram(
                                    context,
                                    resources.getString(R.string.wx_appid)
                                )
                            }
                        }
                    }
                }, true)
                .addBannerLifecycleObserver(fgt)
                .indicator = CircleIndicator(context)

        }
    }

    fun setupBanner(banner: Banner<*, *>, bannerList: List<BannerInfo>, fgt: FgtBase) {
        banner.apply {
            if(bannerList.isEmpty()){
                banner.visibility = android.view.View.GONE
                return
            }
            layoutParams?.height = (ScreenUtils.getScreenWidth() * 0.22f).toInt()
            val dataList = bannerList.map { it.imgSrc }
            (this as Banner<String, BannerImageCommonAdapter>)
                .setAdapter(BannerImageCommonAdapter(dataList).apply {
                    this.setOnBannerListener { data, position ->
                        val bannerInfo = bannerList[position]
                        when (bannerInfo.opType) {
                            1 -> handleInternalLink(bannerInfo, fgt)
                            2 -> openExternalWebPage(bannerInfo.linkUrl, fgt.requireContext())
                        }
                    }
                }, true)
                .addBannerLifecycleObserver(fgt)
                .indicator = CircleIndicator(context)
        }
    }

    private fun handleInternalLink(bannerInfo: BannerInfo, fgt: FgtBase) {
        val linkUrl = bannerInfo.linkUrl
        when {
            linkUrl.startsWith("couponPurchase://") -> {
                // 检查用户是否有购买优惠券的资格
                checkCouponPurchaseQualification(fgt)
            }
            linkUrl.startsWith("wxMin://") -> {
                WeChatHelper.launchWXMiniProgram(
                    fgt.requireContext(),
                    fgt.resources.getString(R.string.wx_appid)
                )
            }
            linkUrl.startsWith("official://") -> {
                fgt.start(FgtShare.newInstance())
            }
            linkUrl.startsWith("extendedGift://") -> {
                fgt.start(FgtExtendedGift.newInstance())
            }
            linkUrl.startsWith("https://") -> {
                AtyWeb2.start(bannerInfo.title,linkUrl)
            }
            else -> {
                // 兜底判断：如果banner解析出来的type在APP内查询不到，则提示升级APP
                EasyToast.DEFAULT.show("功能暂时无法使用，请您升级APP后重试")
            }
        }
    }

    // 检查购买优惠券资格
    private fun checkCouponPurchaseQualification(fgt: FgtBase) {
        http {
            url = "/apiv6/advertisementinfo/getadvertisement"
            params["userId"] = FgtHome.userId
            params["position"] = "1"
            params["lat"] = App.lat.toString()
            params["lng"] = App.lng.toString()

            onSuccess { res ->
                val adInfo = res.toPOJO<AdvertisementData>().data
                val couponPurchaseAd = adInfo.promotions?.find { ad ->
                    ad.operationData?.type == "couponPurchase"
                }
                
                if (couponPurchaseAd != null) {
                    // 找到优惠券购买广告，进入购买页面
                    fgt.start(FgtCouponPurchase.newInstance(couponPurchaseAd.operationData?.data))
                } else {
                    // 没有找到优惠券购买广告
                    EasyToast.DEFAULT.show("暂未查询到优惠券包信息")
                }
            }

            onFail { _, s ->
                EasyToast.DEFAULT.show("暂未查询到优惠券包信息")
            }
        }
    }

    // 广告数据类
    data class AdvertisementData(
        var `data`: AdInfoRemote,
        var errcode: Int = 0,
        var errmsg: String = ""
    )

    private fun openExternalWebPage(url: String, context: Context) {
        AtyWeb2.startBrowser(context,url)
    }
}