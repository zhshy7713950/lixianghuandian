package com.ruimeng.things.common

import com.ruimeng.things.R
import com.ruimeng.things.adapter.BannerImageCommonAdapter
import com.ruimeng.things.share.FgtShare
import com.utils.WeChatHelper
import com.youth.banner.Banner
import com.youth.banner.indicator.CircleIndicator
import wongxd.base.FgtBase
import wongxd.utils.utilcode.util.ScreenUtils

object BannerHelper {
    fun initCommonBanner(
        banner: Banner<*, *>,
        fgt: FgtBase
    ) {
        (banner as Banner<String, BannerImageCommonAdapter>).apply {
            layoutParams?.height = (ScreenUtils.getScreenWidth() * 0.22f).toInt()
            val dataList = listOf(
                "https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/SpringFestival-banner.png"
            )
            setAdapter(BannerImageCommonAdapter(dataList).apply {
                this.setOnBannerListener { _, position ->
                    when (position) {
                        0 -> {
                            fgt.start(FgtShare.newInstance())
                        }
                    }
                }
            }, true)
                .addBannerLifecycleObserver(fgt)

            this.indicator = CircleIndicator(context)

        }
    }
}