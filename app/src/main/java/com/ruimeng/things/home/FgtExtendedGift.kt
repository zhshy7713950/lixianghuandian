package com.ruimeng.things.home

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_extended_gift.*
import wongxd.base.BaseBackFragment

class FgtExtendedGift : BaseBackFragment() {

    companion object {
        fun newInstance(): FgtExtendedGift {
            return FgtExtendedGift()
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_extended_gift

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "电池免息租·好礼免费领")

        // 加载图片
        Glide.with(this).load("https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/extendedGift-top.png").into(ivTop)
        Glide.with(this).load("https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/extendedGift-middle.png").into(ivMiddle)
        Glide.with(this).load("https://downxll.oss-cn-beijing.aliyuncs.com/wxmin/images/extendedGift-bottom.png").into(ivBottom)

        ivMiddle.setOnClickListener {
            pop()
        }
    }
} 