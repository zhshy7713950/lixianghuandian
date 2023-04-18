package com.ruimeng.things.me

import android.os.Bundle
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_safe_center.*
import wongxd.base.BaseBackFragment

/**
 * Created by wongxd on 2018/11/28.
 */
class FgtSafeCenter : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_safe_center

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "安全中心")

        ll_pwd_safe_center.setOnClickListener { start(FgtPayPwd()) }

        ll_bind_card_safe_center.setOnClickListener { start(FgtBindCard()) }
    }
}