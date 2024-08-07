package com.ruimeng.things

import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.entity.local.OneKeyLoginLocal
import com.net.whenBizError
import com.net.whenError
import com.net.whenSuccess
import com.net.whenUnknownError
import com.netease.nis.quicklogin.listener.QuickLoginTokenListener
import com.ruimeng.things.bean.LoginBean
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.activity.AtyWeb2
import com.utils.Configs
import com.utils.ToastHelper
import com.utils.quicklogin.PrefetchResult
import com.utils.quicklogin.QuickLoginHelper
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
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

    private var phone: String = ""
    private var pageType = 0
    private var prefetchResult: PrefetchResult? = null
    private val vm: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_login)
        pageType = intent.getIntExtra("pageType", 0)
//        iv_wechat_login.visibility = if (pageType == 0) View.VISIBLE else View.GONE
//        tv_3.visibility = if (pageType == 0) View.VISIBLE else View.GONE

        tv_get_code.setOnClickListener { getLoginCode() }

//        btn_login.setOnClickListener { doLogin() }

        et_phone.setText(SPUtils.getInstance().getString(TAG_LAST_LOGIN_PHONE))
        cb_login.isChecked = isAgree
        cb_login.setOnCheckedChangeListener { compoundButton, b ->
            isAgree = b
        }
        val str = "我已认真阅读并同意接受锂享换电的\n《用户协议》" +
                "以及《隐私政策》"
        val ssb = SpannableStringBuilder()
        ssb.append(str)
        val start = str.indexOf("《")
        ssb.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //设置文件颜色
                ds.color = Color.parseColor("#13C681")
                // 去掉下划线
                ds.isUnderlineText = false
            }

            override fun onClick(p0: View) {
                AtyWeb2.start("用户协议", "${Http.host}/appH5/userProtocol.html")
            }
        }, start, start + 6, 0)
        val end = str.lastIndexOf("《")
        ssb.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //设置文件颜色
                ds.color = Color.parseColor("#13C681")
                ds.bgColor = Color.TRANSPARENT
                // 去掉下划线
                ds.isUnderlineText = false
            }

            override fun onClick(p0: View) {
                AtyWeb2.start("隐私政策", "${Http.host}/appH5/privateProtocol.html")
            }
        }, end, end + 6, 0)
        tv_login_protocol.setMovementMethod(LinkMovementMethod.getInstance())
        tv_login_protocol.setText(ssb, TextView.BufferType.SPANNABLE)
        tv_login_protocol.highlightColor = Color.TRANSPARENT


        iv_wechat_login.setOnClickListener {
            if (!isAgree) {
                EasyToast.DEFAULT.show("请阅读并同意接受协议")
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

        et_code.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.i("Login", "onTextChanged: " + p0);
//                if (p0.toString().length == 6) {
//                    doLogin()
//                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        tv_phone1.setOnClickListener {
            doLoginHttp(tv_phone1.text.toString(), "666888")
        }
        tv_phone2.setOnClickListener {
            doLoginHttp(tv_phone2.text.toString(), "666888")
        }
        tv_phone3.setOnClickListener {
            doLoginHttp(tv_phone3.text.toString(), "666888")
        }
        tv_phone4.setOnClickListener {
            doLoginHttp(tv_phone4.text.toString(), "666888")
        }
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            layout_test.visibility = View.VISIBLE
        } else {
            layout_test.visibility = View.GONE
        }
        btnOtpLogin.setOnClickListener {
            if (et_phone.text.toString().isEmpty()) {
                EasyToast.DEFAULT.show("请输入手机号码")
                return@setOnClickListener
            }
            if (et_code.text.toString().isEmpty()) {
                EasyToast.DEFAULT.show("请输入验证码")
                return@setOnClickListener
            }
            doLogin()
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

    private fun checkStatus(p: Boolean) {
    }

    private fun doLogin() {
        if (!isAgree && pageType == 0) {
            EasyToast.DEFAULT.show("请阅读并同意接受协议")
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
        doLoginHttp(phone, code)
    }

    private var hasRetryYDToken = false

    private fun doOneKeyLogin(YDToken: String?, accessToken: String?) {
        vm.oneKeyLogin(OneKeyLoginLocal(YDToken ?: "", accessToken ?: "")).observeForever {
            it.whenSuccess { resCommon ->
                QuickLoginHelper.getQuickLoginInstance().quitActivity()
                UserInfoLiveData.setToString(resCommon.data.userinfo!!)
                Config.getDefault().token = resCommon.data.token
                startAty<AtyMain>()
            }.whenError { code, msg ->
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

    private fun doLoginHttp(phone: String, code: String) {
        http {
            url = if (pageType == 0) Path.MOBLILE_LOGIN else Path.BIND_MOBILE
            params["mobile"] = phone
            params["code"] = code

            onSuccessWithMsg { s, msg ->
                if (pageType == 0) {
                    SPUtils.getInstance().put(TAG_LAST_LOGIN_PHONE, phone)
                    val loginBean = s.toPOJO<LoginBean>()
                    UserInfoLiveData.setToString(loginBean.data.userinfo)
                    Config.getDefault().token = loginBean.data.token
                    startAty<AtyMain>()
                } else {
                    val origin = InfoViewModel.getDefault().userInfo.value
                    origin?.mobile_bind = "1"
                    origin?.mobile = phone
                    InfoViewModel.getDefault().userInfo.postValue(origin)

                    UserInfoLiveData.refresh()
                    EasyToast.DEFAULT.show(msg)
                }
                Config.getDefault().spUtils.put(FgtHome.KEY_LAST_DEVICE_ID, "")
                finish()
            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }

    override fun onBackPressedSupport() {
        if (pageType == 1) {
            EasyToast.DEFAULT.show("请先完成绑定手机的操作")
        } else {
            super.onBackPressedSupport()
        }


    }

    private fun getLoginCode() {
        if (!isAgree && pageType == 0) {
            EasyToast.DEFAULT.show("请阅读并同意接受协议")
            return
        }
        phone = et_phone.text.toString()
        if (phone.isBlank()) {
            EasyToast.DEFAULT.show("请输入手机号")
            return
        }

        http {
            url = Path.GET_CODE
            params["mobile"] = phone
            params["tag"] = if (pageType == 0) "login" else "bindmobile"

            onSuccess {
                EasyToast.DEFAULT.show("验证码已发送")
                SmsTimeUtils.startCountdown(WeakReference(tv_get_code))
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
            params["appType"] = "lxhd"

            onSuccess {
                val loginBean = it.toPOJO<LoginBean>()
                UserInfoLiveData.setToString(loginBean.data.userinfo)
                Config.getDefault().token = loginBean.data.token
                startAty<AtyMain>()
                finish()
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.isLoginActivityStarted = false
    }
}