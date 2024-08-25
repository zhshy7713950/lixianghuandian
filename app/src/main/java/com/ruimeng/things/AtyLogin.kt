package com.ruimeng.things

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.netease.nis.quicklogin.listener.QuickLoginTokenListener
import com.ruimeng.things.bean.LoginBean
import com.ruimeng.things.wxapi.WXEntryActivity
import com.utils.ToastHelper
import com.utils.quicklogin.PrefetchResult
import com.utils.quicklogin.QuickLoginHelper
import kotlinx.android.synthetic.main.aty_login.*
import kotlinx.coroutines.launch
import wongxd.AtyWeb
import wongxd.Config
import wongxd.Http
import wongxd.base.AtyBase
import wongxd.common.*
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.http
import wongxd.utils.utilcode.util.SPUtils
import java.lang.ref.WeakReference

/**
 * Created by wongxd on 2018/10/31.
 */
class AtyLogin : AtyBase() {

    companion object {
        const val TAG = "AtyLogin"
        const val TAG_LAST_LOGIN_PHONE = "last_login_phone"
    }

    private var prefetchResult: PrefetchResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_login)


        tv_get_code.setOnClickListener { getLoginCode() }
        btn_login.setOnClickListener { doLogin() }


        cb_login.isChecked = isAgree
        cb_login.setOnCheckedChangeListener { compoundButton, b ->
            isAgree = b
        }

        val str = "我已认真阅读并同意接受享锂来的\n《用户协议》" +
                "以及《隐私政策》"
        val ssb = SpannableStringBuilder()
        ssb.append(str)
        val start = str.indexOf("《")
        ssb.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //设置文件颜色
                ds.color = Color.parseColor("#55bb87")
                // 去掉下划线
                ds.isUnderlineText = false
            }

            override fun onClick(p0: View) {
                AtyWeb.start("用户协议", "${Http.host}/appH5/userProtocol.html")
            }
        }, start, start + 6, 0)
        val end = str.lastIndexOf("《")
        ssb.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //设置文件颜色
                ds.color = Color.parseColor("#55bb87")
                ds.bgColor = Color.TRANSPARENT
                // 去掉下划线
                ds.isUnderlineText = false
            }

            override fun onClick(p0: View) {
                AtyWeb.start("隐私政策", "${Http.host}/appH5/privateProtocol.html")
            }
        }, end, end + 6, 0)
        tv_login_protocol.setMovementMethod(LinkMovementMethod.getInstance())
        tv_login_protocol.setText(ssb, TextView.BufferType.SPANNABLE)
        tv_login_protocol.highlightColor = Color.TRANSPARENT


        et_phone.setText(SPUtils.getInstance().getString(TAG_LAST_LOGIN_PHONE))

        iv_wechat_login.setOnClickListener {
            if (!isAgree) {
                EasyToast.DEFAULT.show("请同意《登陆注册协议》")
                return@setOnClickListener
            }

            WXEntryActivity.wxLogin(this, object : WXEntryActivity.WxCallback {
                override fun onsuccess(code: String?, msg: String?) {

                    bothNotNull(code, msg) { a, b ->
                        postWechatCode(a)
                    }
                }

                override fun onFail(msg: String?) {

                }
            })


        }

        btnOneKey.setOnClickListener {
            if (prefetchResult != null && prefetchResult!!.isSuccess) {
                doOnePassLogin()
            } else {
                ToastHelper.longToast(getCurrentAty(), "网络检测中，请稍候重试，或使用验证码登录")
            }
        }

        requestPermission()
    }

    private fun requestPermission() {
        getPermissions(getCurrentAty(), PermissionType.READ_PHONE_STATE,
            result = { _, _ ->
                initQuickLogin()
            },
            allGranted = {
                initQuickLogin()
            }
        )
    }

    private fun initQuickLogin() {
        lifecycleScope.launchWhenCreated {
            launch {
                this@AtyLogin.prefetchResult = QuickLoginHelper.prefetchMobileNumber(this@AtyLogin)
                prefetchResult?.let {
                    if (it.isSuccess) {
                        doOnePassLogin()
                    }
                }
            }
        }
    }

    private fun doOnePassLogin() {
        QuickLoginHelper.onePassLogin(object : QuickLoginTokenListener {
            override fun onGetTokenSuccess(YDToken: String?, accessCode: String?) {
                doOneKeyLogin(YDToken, accessCode)
                Log.d(TAG, "YDToken = $YDToken accessCode = $accessCode")
            }

            override fun onGetTokenError(YDToken: String?, code: Int, msg: String?) {
                ToastHelper.longToast(
                    getCurrentAty(),
                    "登录失败，请稍后重试，或使用验证码登录"
                )
            }
        })
    }

    private var hasRetryYDToken = false
    private fun doOneKeyLogin(YDToken: String?, accessToken: String?) {
        http {
            url = Path.ONE_KEY_LOGIN
            params["wangyiToken"] = YDToken ?: ""
            params["accessToken"] = accessToken ?: ""

            onSuccess {
                val loginBean = it.toPOJO<LoginBean>()
                QuickLoginHelper.getQuickLoginInstance().quitActivity()
                UserInfoLiveData.setToString(loginBean.data.userinfo!!)
                Config.getDefault().token = loginBean.data.token
                startAty<AtyMain>()
            }

            onFail { code, msg ->
                if (code == 450 && !hasRetryYDToken) {
                    lifecycleScope.launch {
                        hasRetryYDToken = true
                        this@AtyLogin.prefetchResult = QuickLoginHelper.prefetchMobileNumber(this@AtyLogin)
                        prefetchResult?.let { result ->
                            if (result.isSuccess) {
                                doOneKeyLogin(result.YDToken,accessToken)
                            }
                        }
                    }
                } else {
                    QuickLoginHelper.getQuickLoginInstance().quitActivity()
                    EasyToast.DEFAULT.show(msg)
                }
            }
        }
    }

    private fun doLogin() {
        if (!isAgree) {
            EasyToast.DEFAULT.show("请同意《登陆注册协议》")
            return
        }

        val phone = et_phone.text.toString()
        if (phone.isBlank()) {
            EasyToast.DEFAULT.show("请输入手机号")
            return
        }

        val code = et_code.text.toString()
        if (phone.isBlank()) {
            EasyToast.DEFAULT.show("请输入验证码")
            return
        }

        http {
            url = Path.MOBLILE_LOGIN
            params["mobile"] = phone
            params["code"] = code

            onSuccess {
                SPUtils.getInstance().put(TAG_LAST_LOGIN_PHONE, phone)
                val loginBean = it.toPOJO<LoginBean>()
                UserInfoLiveData.setToString(loginBean.data.userinfo)
                Config.getDefault().token = loginBean.data.token
                startAty<AtyMain>()
                finish()
            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }

    }


    private fun getLoginCode() {
        val phone = et_phone.text.toString()
        if (phone.isBlank()) {
            EasyToast.DEFAULT.show("请输入手机号")
            return
        }

        http {
            url = Path.GET_CODE
            params["mobile"] = phone
            params["tag"] = "login"

            onSuccess {
                EasyToast.DEFAULT.show("验证码已发送")
                tv_get_code?.let {
                    SmsTimeUtils.startCountdown(WeakReference(tv_get_code))
                }
            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }

    private var isAgree = true

    private fun postWechatCode(code: String) {
        http {
            url = Path.WX_LOGIN
            params["code"] = code

            onSuccess {
                val loginBean = it.toPOJO<LoginBean>()
                UserInfoLiveData.setToString(loginBean.data.userinfo)
                Config.getDefault().token = loginBean.data.token
                startAty<AtyMain>()
                finish()
            }


        }
    }
}