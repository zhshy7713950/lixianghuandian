package com.ruimeng.things

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.ruimeng.things.me.activity.AtyWeb2
import kotlinx.android.synthetic.main.aty_splash.*
import wongxd.Config
import wongxd.Http
import wongxd.base.BaseBackActivity
import wongxd.common.loadBigImg
import wongxd.common.startAty
import wongxd.utils.utilcode.util.ScreenUtils

/**
 * Created by wongxd on 2018/11/19.
 */
class AtySplash : BaseBackActivity() {

    companion object{
        const val HAS_AGREE_AGREEMENT = "has_agree_agreement"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.WHITE

        setContentView(R.layout.aty_splash)
        setSwipeBackEnable(false)

        val hasAgree = Config.getDefault().spUtils.getBoolean(HAS_AGREE_AGREEMENT,false)
        if(!hasAgree){
            llAgreement.isVisible = true
            val str = "欢迎使用「锂享换电」！在您使用前，请您认真阅读并了解用户协议和隐私政策，以了解我们的服务内容和我们在收集和使用您相关个人信息时的处理规则。我们将严格按照《用户协议》和《隐私政策》为您提供服务，保护您的个人信息"
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
            tvAgreement.movementMethod = LinkMovementMethod.getInstance()
            tvAgreement.setText(ssb, TextView.BufferType.SPANNABLE)
            tvAgreement.highlightColor = Color.TRANSPARENT
            btnAgree.setOnClickListener {
                Config.getDefault().spUtils.put(HAS_AGREE_AGREEMENT,true)
                doJump()
            }
            tvDisagree.setOnClickListener {
                finish()
            }
        }else{
            llAgreement.isVisible = false
            iv_splash.loadBigImg(R.drawable.splash)
            iv_splash.postDelayed({ doJump() }, 1500)
        }
    }


    private fun doJump() {
        if (Config.getDefault().token.isEmpty()) {
            Http.TOKEN_LOST_FUN.invoke("登陆状态丢失,需要重新登陆")
            return
        } else {
            startAty<AtyMain>(intent?.extras)
        }
        finish()
    }
}