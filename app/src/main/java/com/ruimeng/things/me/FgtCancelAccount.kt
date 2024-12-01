package com.ruimeng.things.me

import android.os.Bundle
import android.view.View
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.R
import com.utils.unsafeLazy
import kotlinx.android.synthetic.main.fgt_cancel_account.*
import wongxd.base.BaseBackFragment

class FgtCancelAccount : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_cancel_account

    private val phone by unsafeLazy {
        InfoViewModel.getDefault()?.userInfo?.value?.phone ?: ""
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "注销账号")

        tvTitle1.text = String.format(
            "1.为保障您的权益，我们在此提醒您，若您的账号（%s）下：",
            phone
        )
        tvPhone.text = phone
    }
}