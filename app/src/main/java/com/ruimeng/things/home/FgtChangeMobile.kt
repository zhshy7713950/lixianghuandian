package com.ruimeng.things.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ruimeng.things.AtyLogin
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.UserInfoLiveData
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.fgt_change_mobile.*
import wongxd.Config
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.SmsTimeUtils
import wongxd.http
import wongxd.utils.SystemUtils
import wongxd.utils.utilcode.util.SPUtils
import org.greenrobot.eventbus.EventBus
import com.ruimeng.things.FgtMain
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.entity.local.GetCaptchaLocal
import com.entity.local.CheckCaptchaLocal
import com.net.whenSuccess
import com.net.whenError
import com.net.whenUnknownError
import com.ruimeng.things.LoginViewModel
import androidx.fragment.app.viewModels
import java.lang.ref.WeakReference

class FgtChangeMobile : BaseBackFragment() {

    companion object {
        const val VERIFY_TYPE = 1
        const val CHANGE_TYPE = 2
        fun newInstance(type: Int) = FgtChangeMobile().apply {
            arguments = Bundle().apply {
                putInt("type", type)
            }
        }
    }

    private val type: Int by lazy { arguments?.getInt("type") ?: VERIFY_TYPE }
    private var oldPhone: String? = null

    override fun getLayoutRes(): Int = R.layout.fgt_change_mobile

    private val vm: LoginViewModel by viewModels()

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, if (type == VERIFY_TYPE) "验证老手机号码" else "变更新手机号码")
        initViews()
        initOrLoadCaptcha()
    }

    private fun initViews() {
        InfoViewModel.getDefault().userInfo.simpleObserver(this) { userinfo ->
            if (type == VERIFY_TYPE) {
                etPhone.setText(userinfo.phone)
            } else {
                tvTitle.text = "老手机号码：${userinfo.phone}"
                oldPhone = userinfo.phone
            }
        }
        if (type == VERIFY_TYPE) {
            tvTitle.text = "请您先完成\"老手机号码\"验证"
            etPhone.hint = "请输入老手机号码"
            btnOtpLogin.text = "提交验证"
        } else {
            etPhone.hint = "请输入新手机号码"
            btnOtpLogin.text = "确认更换"
        }
        tvGetCode.setOnClickListener {
            getLoginCode()
        }
        btnOtpLogin.setOnClickListener {
            // ① 校验手机号码
            val phone = etPhone.text.toString().trim()
            if (phone.isBlank() || phone.length != 11) {
                if (type == VERIFY_TYPE) {
                    EasyToast.DEFAULT.show("请输入老手机号码(11位)")
                } else {
                    EasyToast.DEFAULT.show("请输入新手机号码(11位)")
                }
                return@setOnClickListener
            }
            
            // ② 校验图形验证码
            val imgCode = et_img_code.text?.toString()?.trim() ?: ""
            if (imgCode.isBlank() || imgCode.length != 5) {
                EasyToast.DEFAULT.show("请输入图形验证码(5位)")
                return@setOnClickListener
            }
            
            // ③ 校验短信验证码
            val code = etCode.text.toString().trim()
            if (code.isBlank() || code.length != 6) {
                EasyToast.DEFAULT.show("请输入短信验证码(6位)")
                return@setOnClickListener
            }
            
            // ④ 调用接口
            if (type == VERIFY_TYPE) {
                verifyOldMobile(phone, code)
            } else {
                changeMobile(phone, code, it)
            }
        }
    }

    private fun initOrLoadCaptcha() {
        val mobile = etPhone.text?.toString()?.trim() ?: ""
        if (mobile.isBlank()) {
            showCaptchaPlaceholder()
        } else if (mobile.length == 11) {
            loadCaptcha(mobile)
        } else {
            showCaptchaPlaceholder()
        }
        fl_img_captcha.setOnClickListener { refreshCaptcha() }
        tv_change_captcha.setOnClickListener { refreshCaptcha() }

        etPhone.addTextChangedListener(object: android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank() && s.length == 11) {
                    loadCaptcha(s.toString())
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
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
        val mobile = etPhone.text?.toString()?.trim() ?: ""
        if (mobile.isBlank() || mobile.length != 11) {
            if (type == VERIFY_TYPE) {
                EasyToast.DEFAULT.show("请输入老手机号码(11位)")
            } else {
                EasyToast.DEFAULT.show("请输入新手机号码(11位)")
            }
            return
        }
        loadCaptcha(mobile)
    }

    private fun loadCaptcha(mobile: String) {
        vm.getCaptcha(GetCaptchaLocal(mobile)).observe(this, Observer { response ->
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
            }.whenUnknownError { _ ->
                showCaptchaPlaceholder()
            }
        })
    }

    private fun changeMobile(phone: String, code: String,v: View) {
        NormalDialog(requireContext()).apply {
            style(NormalDialog.STYLE_TWO)
            title("确认将手机号码\"${oldPhone ?: "-"}\"更换为\"${phone}\"？")
            titleTextColor(Color.parseColor("#131414"))
            btnText("确定", "取消")
            btnTextColor(Color.parseColor("#29EBB6"), Color.parseColor("#FF6464"))
            setOnBtnClickL(OnBtnClickL {
                http {
                    url = Path.CHANGE_MOBILE
                    params["oldMobile"] = oldPhone ?: ""
                    params["newMobile"] = phone
                    params["code"] = code
                    onSuccess {
                        ToastHelper.shortToast(context, "更换成功，请重新登录")
                        dismiss()
                        // 发送切换到首页事件
                        EventBus.getDefault().post(FgtMain.SwitchPageEvent(0))
                        v.postDelayed({
                            Config.getDefault().token = ""
                            Config.getDefault().stringCacheUtils.remove(UserInfoLiveData.STORE_KEY)
                            SPUtils.getInstance().put("MOBILE_BIND_SKIP", false)
                            SystemUtils.cleanTask2Activity(activity, AtyLogin::class.java)
                        }, 1500)
                    }
                    onFail { _, msg ->
                        EasyToast.DEFAULT.show(msg)
                        dismiss()
                    }
                }

            }, OnBtnClickL {
                dismiss()
            })
            show()
        }
    }

    private fun verifyOldMobile(phone: String, code: String) {
        http {
            url = Path.VERIFY_MOBILE
            params["mobile"] = phone
            params["code"] = code

            onSuccess {
                pop()
                start(newInstance(CHANGE_TYPE))
            }

            onFail { _, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }


    private fun getLoginCode() {
        // ① 校验手机号码
        val phone = etPhone.text.toString().trim()
        if (phone.isBlank() || phone.length != 11) {
            if (type == VERIFY_TYPE) {
                EasyToast.DEFAULT.show("请输入老手机号码(11位)")
            } else {
                EasyToast.DEFAULT.show("请输入新手机号码(11位)")
            }
            return
        }
        
        // ② 校验图形验证码
        val imgCode = et_img_code.text?.toString()?.trim() ?: ""
        if (imgCode.isBlank() || imgCode.length != 5) {
            EasyToast.DEFAULT.show("请输入图形验证码(5位)")
            return
        }

        // ③ 业务逻辑校验
        if (type == CHANGE_TYPE) {
            val old = oldPhone ?: ""
            if (phone == old) {
                EasyToast.DEFAULT.show("变更的新/老手机号不能相同")
                return
            }
        }

        // ④ 调用接口
        val tagParam = if (type == VERIFY_TYPE) "change" else null
        vm.checkCaptcha(CheckCaptchaLocal(phone, imgCode, tagParam)).observe(this, Observer { resp ->
            resp.whenSuccess {
                EasyToast.DEFAULT.show("验证码已发送")
                SmsTimeUtils.startCountdown(WeakReference(tvGetCode))
            }.whenError { _, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        })
    }
}