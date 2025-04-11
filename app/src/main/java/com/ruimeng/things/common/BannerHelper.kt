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
import com.ruimeng.things.home.FgtExtendedGift
import com.ruimeng.things.me.activity.AtyWeb2

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
            else -> {
                // 打开内部网页
                AtyWeb2.start(bannerInfo.title,bannerInfo.linkUrl)
            }
        }
    }

    private fun openExternalWebPage(url: String, context: Context) {
        AtyWeb2.startBrowser(context,url)
    }
}