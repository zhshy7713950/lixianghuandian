package com.ruimeng.things.me.activity

import android.os.Bundle
import android.text.TextUtils
import com.ruimeng.things.R
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_bind_ali_pay.*
import wongxd.base.AtyBase
import wongxd.http


class BindAliPayActivity : AtyBase() {

    private var getAccountName = ""
    private var getRealName = ""

    companion object {
        lateinit var mActivity: BindAliPayActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_ali_pay)
        initView()
        setListener()
        initData()
    }

    private fun initView() {
        mActivity = this
    }

    private fun setListener() {
        confirmBtn?.setOnClickListener {
            if (TextUtils.isEmpty(inputNameText?.text)) {
                ToastHelper.shortToast(mActivity, inputNameText?.hint)
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(inputAccountText?.text)) {
                ToastHelper.shortToast(mActivity, inputAccountText?.hint)
                return@setOnClickListener
            }
            requestBandAliPay(inputAccountText?.text.toString(), inputNameText?.text.toString())
        }
    }

    private fun initData() {
        initTopbar(topbar, "绑定支付宝")
        if (!TextUtils.isEmpty(intent.getStringExtra("accountName"))) {
            getAccountName = intent.getStringExtra("accountName")?:""
            inputAccountText?.setText(getAccountName)
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("realName"))) {
            getRealName = intent.getStringExtra("realName")?:""
            inputNameText?.setText(getRealName)
        }

    }

    private fun requestBandAliPay(acctName: String, realName: String) {
        http {
            url = "apiv5/bandalipay"
            params["acct_name"] = acctName
            params["real_name"] = realName
            onSuccessWithMsg { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
                finish()
            }

            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }
        }
    }

}