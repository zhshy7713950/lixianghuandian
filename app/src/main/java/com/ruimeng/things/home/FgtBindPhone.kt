package com.ruimeng.things.home

import android.os.Bundle
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.UserInfoLiveData
import kotlinx.android.synthetic.main.fgt_bind_phone.*
import wongxd.base.FgtBase
import wongxd.common.EasyToast
import wongxd.common.SmsTimeUtils
import wongxd.http
import java.lang.ref.WeakReference

/**
 * Created by wongxd on 2018/11/13.
 */
class FgtBindPhone : FgtBase() {
    override fun getLayoutRes(): Int = R.layout.fgt_bind_phone

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "绑定手机号码", false)


        fl_get_code_bind_phone.setOnClickListener {
            val phone = et_phone_bind_phone.text.toString()

            if (phone.isNotBlank()) {
                http {
                    url = Path.GET_CODE
                    params["mobile"] = phone
                    params["tag"] = "bindmobile"

                    onSuccessWithMsg { s, msg ->
                        EasyToast.DEFAULT.show(msg)
                        SmsTimeUtils.startCountdown(WeakReference(tv_get_code_bind_phoen))
                    }
                }
            }

        }


        btn_bind_phone.setOnClickListener {
            val phone = et_phone_bind_phone.text.toString()
            val code = et_code_bind_phone.text.toString()

            if (phone.isNotBlank() && code.isNotBlank()) {
                http {
                    url = Path.BIND_MOBILE
                    params["code"] = code
                    params["mobile"] = phone

                    onSuccessWithMsg { s, msg ->
                        val origin = InfoViewModel.getDefault().userInfo.value
                        origin?.mobile_bind="1"
                        origin?.mobile = phone
                        InfoViewModel.getDefault().userInfo.postValue(origin)

                        UserInfoLiveData.refresh()
                        EasyToast.DEFAULT.show(msg)
                        pop()
                    }
                }

            } else {
                EasyToast.DEFAULT.show("手机号和验证码为必填项")
            }

        }


    }


    override fun onBackPressedSupport(): Boolean {

        EasyToast.DEFAULT.show("请先完成绑定手机的操作")

        return true
    }
}