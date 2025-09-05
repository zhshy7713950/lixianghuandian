package com.ruimeng.things.me

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.entity.local.CheckCaptchaLocal
import com.entity.local.GetCaptchaLocal
import com.net.whenError
import com.net.whenSuccess
import com.net.whenUnknownError
import com.ruimeng.things.AtyLogin
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.R
import com.ruimeng.things.UserInfoLiveData
import com.ruimeng.things.LoginViewModel
import com.ruimeng.things.me.vm.CancelAccountViewModel
import com.utils.unsafeLazy
import kotlinx.android.synthetic.main.fgt_cancel_account.*
import wongxd.Config
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.utils.SystemUtils
import wongxd.utils.utilcode.util.SPUtils
import wongxd.common.SmsTimeUtils
import java.lang.ref.WeakReference

class FgtCancelAccount : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_cancel_account
    private val vm: CancelAccountViewModel by viewModels()
    private val vmLogin: LoginViewModel by viewModels()

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
        initOrLoadCaptcha()
        fl_img_captcha.setOnClickListener { refreshCaptcha() }
        tv_change_captcha.setOnClickListener { refreshCaptcha() }
        tvGetCode.setOnClickListener {
            val imgCode = et_img_code.text?.toString()?.trim() ?: ""
            if (imgCode.isBlank() || imgCode.length != 5) {
                EasyToast.DEFAULT.show("请输入图形验证码(5位)")
                return@setOnClickListener
            }
            // 发送验证码：/apiv6/user/checkcaptcha tag=unregister
            vmLogin.checkCaptcha(CheckCaptchaLocal(phone, imgCode, "unregister")).observe(this, Observer { resp ->
                resp.whenSuccess {
                    EasyToast.DEFAULT.show("验证码已发送")
                    SmsTimeUtils.startCountdown(WeakReference(tvGetCode))
                }.whenError { _, msg ->
                    EasyToast.DEFAULT.show(msg)
                }
            })
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
        vm.unregisterLiveData.observeForever {
            EasyToast.DEFAULT.show(it)
            Config.getDefault().token = ""
            Config.getDefault().stringCacheUtils.remove(UserInfoLiveData.STORE_KEY)
            SPUtils.getInstance().put("MOBILE_BIND_SKIP", false)
            SystemUtils.cleanTask2Activity(activity, AtyLogin::class.java)
        }
    }

    private fun initOrLoadCaptcha() {
        if (phone.isBlank() || phone.length != 11) {
            showCaptchaPlaceholder()
        } else {
            loadCaptcha(phone)
        }
    }

    private fun showCaptchaPlaceholder() {
        tv_img_captcha_placeholder.visibility = View.VISIBLE
        iv_img_captcha.visibility = View.GONE
    }

    private fun showCaptchaImage(url: String) {
        tv_img_captcha_placeholder.visibility = View.GONE
        iv_img_captcha.visibility = View.VISIBLE
        Glide.with(this)
            .load(url)
            .into(iv_img_captcha)
    }

    private fun refreshCaptcha() {
        if (phone.isBlank() || phone.length != 11) {
            // 注销页面的手机号直接读取，通常不提示手机号
            showCaptchaPlaceholder()
        } else {
            loadCaptcha(phone)
        }
    }

    private fun loadCaptcha(mobile: String) {
        vmLogin.getCaptcha(GetCaptchaLocal(mobile)).observe(this, Observer { response ->
            response.whenSuccess { resCommon ->
                val rawUrl = resCommon.data
                val finalUrl = if (rawUrl.startsWith("http")) rawUrl else "https$rawUrl"
                if (finalUrl.isBlank()) {
                    showCaptchaPlaceholder()
                } else {
                    val timestamp = System.currentTimeMillis()
                    val separator = if (finalUrl.contains("?")) "&" else "?"
                    val urlWithTimestamp = "$finalUrl${separator}timestamp=$timestamp"
                    showCaptchaImage(urlWithTimestamp)
                }
            }.whenError { _, _ ->
                showCaptchaPlaceholder()
            }
        })
    }
}