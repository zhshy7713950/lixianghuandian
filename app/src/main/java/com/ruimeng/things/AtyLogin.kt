package com.ruimeng.things

import android.os.Bundle
import android.text.Html
import android.util.Log
import com.ruimeng.things.bean.LoginBean
import com.ruimeng.things.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.aty_login.*
import wongxd.AtyWeb
import wongxd.Config
import wongxd.Http
import wongxd.base.AtyBase
import wongxd.common.*
import wongxd.http
import java.lang.ref.WeakReference

/**
 * Created by wongxd on 2018/10/31.
 */
class AtyLogin : AtyBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_login)


        tv_get_code.setOnClickListener { getLoginCode() }
        btn_login.setOnClickListener { doLogin() }


        cb_login.isChecked = isAgree
        cb_login.setOnCheckedChangeListener { compoundButton, b ->
            isAgree = b
        }

        tv_login_protocol.text = Html.fromHtml("我已阅读并同意<font color='#008577'>《登录注册协议》</font>")
        tv_login_protocol.setOnClickListener {
            Log.e("登录注册协议", "${Http.host}/appweb/regagreement")
            AtyWeb.start("登录注册协议", "${Http.host}/appweb/regagreement")
        }




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