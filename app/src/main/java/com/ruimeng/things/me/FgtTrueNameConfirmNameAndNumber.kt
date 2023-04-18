package com.ruimeng.things.me


import android.os.Bundle
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.UserInfoLiveData
import kotlinx.android.synthetic.main.fgt_true_name_confirm_name_and_number.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.http


/**
 *
 * 手动输入姓名 和  身份证号
 * Created by wongxd on 2018/11/26.
 */
class FgtTrueNameConfirmNameAndNumber : BaseBackFragment() {


    override fun getLayoutRes(): Int = R.layout.fgt_true_name_confirm_name_and_number


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "实名认证")


        btn_submit_truename.setOnClickListener {

            val name = et_name_truename.text.toString()
            val idcard = et_idcard_truename.text.toString()

            if (name.isBlank() || idcard.isBlank()) {
                EasyToast.DEFAULT.show("请填写名字和身份证号码")
                return@setOnClickListener
            }

            http {
                url = Path.REALNAME

                params["name"] = name
                params["idnumber"] = idcard

                onSuccessWithMsg { res, msg ->
                    EasyToast.DEFAULT.show(msg)
                    UserInfoLiveData.refresh { pop() }
                }
            }
        }

    }


}