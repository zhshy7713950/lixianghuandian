package com.ruimeng.things.me

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.ruimeng.things.AtyLogin
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.R
import com.ruimeng.things.UserInfoLiveData
import com.ruimeng.things.me.vm.CancelAccountViewModel
import com.utils.unsafeLazy
import kotlinx.android.synthetic.main.fgt_cancel_account.*
import wongxd.Config
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.utils.SystemUtils
import wongxd.utils.utilcode.util.SPUtils

class FgtCancelAccount : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_cancel_account
    private val vm: CancelAccountViewModel by viewModels()

    private val phone by unsafeLazy {
        InfoViewModel.getDefault()?.userInfo?.value?.phone ?: ""
    }
    private val userId by unsafeLazy {
        InfoViewModel.getDefault()?.userInfo?.value?.id ?: ""
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initEvent()
        initTopbar(topbar, "注销账号")

        tvTitle1.text = String.format(
            "1.为保障您的权益，我们在此提醒您，若您的账号（%s）下：",
            phone
        )
        tvPhone.text = phone
        btnSendCode.setOnClickListener {
            vm.getCode(phone)
        }
        btnCancelAccount.setOnClickListener {
            if(etCode.text.isNullOrBlank()){
                EasyToast.DEFAULT.show("请输入验证码")
                return@setOnClickListener
            }
            vm.unregister(userId, phone, etCode.text.toString())
        }
    }

    private fun initEvent() {
        vm.getCodeLiveData.observeForever {
            EasyToast.DEFAULT.show(it)
        }
        vm.unregisterLiveData.observeForever {
            EasyToast.DEFAULT.show(it)
            Config.getDefault().token = ""
            Config.getDefault().stringCacheUtils.remove(UserInfoLiveData.STORE_KEY)
            SPUtils.getInstance().put("MOBILE_BIND_SKIP", false)
            SystemUtils.cleanTask2Activity(activity, AtyLogin::class.java)
        }
    }
}