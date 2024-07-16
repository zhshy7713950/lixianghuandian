package com.utils.quicklogin

import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.netease.nis.quicklogin.QuickLogin
import com.netease.nis.quicklogin.helper.UnifyUiConfig
import com.netease.nis.quicklogin.listener.QuickLoginPreMobileListener
import com.netease.nis.quicklogin.listener.QuickLoginTokenListener
import com.ruimeng.things.AtyLogin
import com.utils.Configs
import org.jetbrains.anko.textColor
import wongxd.Http
import wongxd.common.dp2px
import wongxd.common.getCurrentAty
import wongxd.common.px2Dp
import wongxd.utils.utilcode.util.ScreenUtils
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object QuickLoginHelper {

    fun getQuickLoginInstance() = QuickLogin.getInstance()
    suspend fun prefetchMobileNumber() = suspendCoroutine<PrefetchResult> {
        getQuickLoginInstance().apply {
            init(getCurrentAty(), Configs.ONE_KEY_BUSINESS_ID)
            prefetchMobileNumber(object : QuickLoginPreMobileListener {
                override fun onGetMobileNumberSuccess(YDToken: String?, mobileNumber: String?) {
                    it.resume(PrefetchResult(true, YDToken, mobileNumber))
                    Log.d(AtyLogin.TAG, "预取号成功了")
                }

                override fun onGetMobileNumberError(YDToken: String?, msg: String?) {
                    it.resume(PrefetchResult(false, YDToken, errorMsg = msg))
                    Log.d(AtyLogin.TAG, "预取号失败: $msg")
                }
            })
        }
    }

    fun onePassLogin(quickLoginTokenListener: QuickLoginTokenListener) {
        getQuickLoginInstance().apply {
            setUnifyUiConfig()
            onePass(quickLoginTokenListener)
        }
    }

    private fun setUnifyUiConfig() {
        val context = getCurrentAty()
        getQuickLoginInstance().setUnifyUiConfig(UnifyUiConfig.Builder().apply {
            //logo
            setLogoIconDrawable(context.getDrawable(com.ruimeng.things.R.drawable.ic_one_key_logo))
            setLogoWidth(122)
            setLogoHeight(90)
            setLogoTopYOffset(80)

            //app名称
            val tvAppName = TextView(context).also {
                it.text = context.getString(com.ruimeng.things.R.string.app_name)
                it.textColor = Color.WHITE
                it.textSize = 20f
                it.layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT).also {mpls ->
                    mpls.topMargin = 10.dp2px().toInt()
                    mpls.addRule(RelativeLayout.BELOW,com.netease.nis.quicklogin.R.id.yd_iv_logo)
                }
                it.gravity = Gravity.CENTER_HORIZONTAL
            }
            addCustomView(tvAppName, "tv_app_name", UnifyUiConfig.POSITION_IN_BODY,null)

            //手机号码
            setMaskNumberColor(Color.WHITE)
            setMaskNumberSize(28)
            setMaskNumberTopYOffset(250)

            //运营商
            setSloganSize(12)
            setSloganColor(Color.WHITE)
            setSloganTopYOffset(290)

            //登录按钮
            setLoginBtnText("本机号码一键登录")
            setLoginBtnTextSize(16)
            setLoginBtnTextColor(Color.BLACK)
            setLoginBtnWidth(ScreenUtils.getScreenWidth().px2Dp().toInt() - 80)
            setLoginBtnHeight(50)
            setLoginBtnTopYOffset(346)
            setLoginBtnBackgroundDrawable(context.getDrawable(com.ruimeng.things.R.drawable.bg_btn_common1))

            setHideNavigation(true)
            setStatusBarColor(android.R.color.transparent)

            //背景图
            setBackgroundImage("bg_one_key")

            //验证码登录按钮
            val button = Button(context).also {
                it.setBackgroundResource(com.ruimeng.things.R.drawable.bg_login_switch)
                it.text = "验证码登录"
                it.textColor = Color.parseColor("#CCCCCC")
                it.textSize = 16f
                it.layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,50.dp2px().toInt()).also {mpls ->
                    mpls.leftMargin = 40.dp2px().toInt()
                    mpls.rightMargin = 40.dp2px().toInt()
                    mpls.topMargin = 30.dp2px().toInt()
                    mpls.addRule(RelativeLayout.BELOW,com.netease.nis.quicklogin.R.id.yd_btn_oauth)
                }
            }
            addCustomView(button, "go_otp_login", UnifyUiConfig.POSITION_IN_BODY) { _, _, _ ->
                getQuickLoginInstance().quitActivity()
            }

            //协议
            setPrivacyTextColor(Color.parseColor("#b8c7d7"))
            setPrivacyProtocolColor(Color.parseColor("#13C681"))
            setPrivacyMarginLeft(30)
            setPrivacySize(12)
            setPrivacyBottomYOffset(30)
            setPrivacyState(true)
            setPrivacyTextMarginLeft(20)
            setCheckBoxGravity(Gravity.CENTER)
            setPrivacyCheckBoxWidth(14)
            setPrivacyCheckBoxHeight(14)
            setCheckedImageName("check_on")
            setUnCheckedImageName("check_off")
            setHidePrivacySmh(true)
            setPrivacyTextStart("未注册的手机号验证后将创建新账号，登录即同意")
            setProtocolConnect("并授权")
            setUserProtocolConnect("和")
            setProtocolText("锂享换电用户协议")
            setProtocolLink("${Http.host}/appH5/userProtocol.html")
            setProtocol2Text("锂享换电隐私政策")
            setProtocol2Link("${Http.host}/appH5/privateProtocol.html")
            setPrivacyTextEnd("获得本机号码")

            //协议web
            setProtocolPageNavBackIcon("ic_web_back")
            setProtocolPageNavColor(Color.parseColor("#404E59"))
            setProtocolPageNavTitleColor(Color.WHITE)
            setProtocolPageNavBackIconWidth(12)
            setProtocolPageNavBackIconHeight(22)

            //未勾选弹窗
            setPrivacyDialogText("请阅读并同意接受协议")
        }.build(context))
    }

}

data class PrefetchResult(
    val isSuccess: Boolean,
    val YDToken: String?,
    val mobileNumber: String? = null,
    val errorMsg: String? = null
)