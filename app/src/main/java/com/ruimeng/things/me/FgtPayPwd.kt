package com.ruimeng.things.me

import android.os.Bundle
import com.ruimeng.things.Path
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_pay_pwd.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.SmsTimeUtils
import wongxd.http
import wongxd.utils.SystemUtils
import java.lang.ref.WeakReference

/**
 * Created by wongxd on 2018/11/28.
 */
class FgtPayPwd : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_pay_pwd


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "支付密码")


        tv_get_code_pay_pwd.setOnClickListener {
            val phone = et_phone_pay_pwd.text.toString()
            if (SystemUtils.isHadEmptyText(phone)) {
                EasyToast.DEFAULT.show("请输入手机号")
                return@setOnClickListener
            }

            http {

                url = Path.GET_CODE

                params["mobile"] = phone
                params["tag"] = "paypwd"


                onSuccessWithMsg { res, msg ->
                    SmsTimeUtils.startCountdown(WeakReference(tv_get_code_pay_pwd))
                }
            }

        }

        btn_submit_pay_pwd.setOnClickListener {

            val phone = et_phone_pay_pwd.text.toString()

            val code = et_code_pay_pwd.text.toString()

            val pwd = et_pwd_pay_pwd.text.toString()
            val pwd2 = et_pwd_again_phone_pay_pwd.text.toString()

            if (SystemUtils.isHadEmptyText(phone, code, pwd, pwd2)) {
                EasyToast.DEFAULT.show("请完善本页数据")
                return@setOnClickListener
            }

            if (pwd != pwd2) {
                EasyToast.DEFAULT.show("两次密码不一致")
                return@setOnClickListener
            }


            http {
                url = Path.SETPAYPWD

                params["code"] = code
                params["paypwd"] = pwd
                params["mobile"] = phone

                onSuccessWithMsg { res, msg ->
                    EasyToast.DEFAULT.show(msg)
                    pop()
                }
            }

        }
    }
}