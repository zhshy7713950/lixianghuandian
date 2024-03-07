package com.ruimeng.things

import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import com.ruimeng.things.bean.LoginBean
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.activity.AtyWeb2
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.aty_login.*
import wongxd.AtyWeb
import wongxd.Config
import wongxd.Http
import wongxd.base.AtyBase
import wongxd.common.*
import wongxd.http
import wongxd.utils.utilcode.util.SPUtils
import java.lang.ref.WeakReference


/**
 * Created by wongxd on 2018/10/31.
 */
class AtyLogin : AtyBase() {
    private  var phone:String = ""
    private var pageType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_login)
        pageType = intent.getIntExtra("pageType",0)
        iv_wechat_login.visibility = if (pageType == 0) View.VISIBLE else View.GONE
        tv_3.visibility = if (pageType == 0) View.VISIBLE else View.GONE

        tv_get_code.setOnClickListener { getLoginCode() }
        tv_reget_code.setOnClickListener{getLoginCode()}

//        btn_login.setOnClickListener { doLogin() }


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
        tv_login_protocol.highlightColor =Color.TRANSPARENT


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
                Log.i("Login", "onTextChanged: "+p0);
                if (p0.toString().length == 6){
                    doLogin()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        checkStatus(true)
        tv_phone1.setOnClickListener {
            doLoginHttp(tv_phone1.text.toString(),"666888")
        }
        tv_phone2.setOnClickListener {
            doLoginHttp(tv_phone2.text.toString(),"666888")
        }
        tv_phone3.setOnClickListener {
            doLoginHttp(tv_phone3.text.toString(),"666888")
        }
        if (BuildConfig.BUILD_TYPE.equals("debug")){
            layout_test.visibility = View.VISIBLE
        }else{
            layout_test.visibility = View.GONE
        }
    }

    private fun checkStatus(p: Boolean ){
        if (p){
            ll_code.visibility = View.GONE
            ll_phone.visibility =View.VISIBLE
            ll_other.visibility = View.VISIBLE
            tv_title1.text =  if (pageType == 0) "手机验证码登录" else "绑定手机号码"
            tv_no_binding.visibility = if (pageType == 0) View.GONE else View.VISIBLE
            tv_title2.text="未注册的手机号验证后将创建新账号"
            tv_no_binding.setOnClickListener {
                SPUtils.getInstance().put("MOBILE_BIND_SKIP",true)
                startAty<AtyMain>()
                finish()
            }
        }else{
            ll_code.visibility = View.VISIBLE
            ll_phone.visibility =View.GONE
            ll_other.visibility = View.GONE
            tv_title1.text ="输入验证码"
            tv_title2.text="验证码已发送至+86 "+phone
        }
    }

    private fun doLogin() {
        if (!isAgree) {
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
        doLoginHttp(phone,code)


    }
    private fun doLoginHttp(phone:String,code: String) {
        http {
            url =  if (pageType == 0 ) Path.MOBLILE_LOGIN else Path.BIND_MOBILE
            params["mobile"] = phone
            params["code"] = code

            onSuccessWithMsg {s,msg->
                if (pageType == 0){
                    val loginBean = s.toPOJO<LoginBean>()
                    UserInfoLiveData.setToString(loginBean.data.userinfo)
                    Config.getDefault().token = loginBean.data.token
                    startAty<AtyMain>()
                }else{
                    val origin = InfoViewModel.getDefault().userInfo.value
                    origin?.mobile_bind="1"
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
        if (pageType == 1){
            EasyToast.DEFAULT.show("请先完成绑定手机的操作")
        }else{
            super.onBackPressedSupport()
        }



    }
    private fun getLoginCode() {
        if (!isAgree) {
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
                checkStatus(false)
                tv_reget_code?.let {
                    SmsTimeUtils.startCountdown(WeakReference(tv_reget_code))
                }
            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }

    private var isAgree = false

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
}