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

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, if (type == VERIFY_TYPE) "验证老手机号码" else "变更新手机号码")
        initViews()
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
            val phone = etPhone.text.toString()
            if (phone.isBlank()) {
                EasyToast.DEFAULT.show("请输入手机号")
                return@setOnClickListener
            }
            val code = etCode.text.toString()
            if (phone.isBlank()) {
                EasyToast.DEFAULT.show("请输入验证码")
                return@setOnClickListener
            }
            if (type == VERIFY_TYPE) {
                verifyOldMobile(phone, code)
            } else {
                changeMobile(phone, code,it)
            }
        }
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
        val phone = etPhone.text.toString()
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
                SmsTimeUtils.startCountdown(WeakReference(tvGetCode))
            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }
}