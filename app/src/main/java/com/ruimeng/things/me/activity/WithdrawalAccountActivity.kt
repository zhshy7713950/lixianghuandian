package com.ruimeng.things.me.activity

import android.content.Intent
import android.os.Bundle
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.DistrCashInfoBean
import com.ruimeng.things.wxapi.WXEntryActivity
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_withdrawal_account.*
import wongxd.base.AtyBase
import wongxd.common.bothNotNull
import wongxd.common.toPOJO
import wongxd.http


class WithdrawalAccountActivity : AtyBase() {

    companion object {
        lateinit var mActivity: WithdrawalAccountActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdrawal_account)
        firstVisit = true
        initView()
        setListener()
        initData()
    }

    private fun initView() {
        mActivity = this
    }

    private fun setListener() {

    }

    private fun initData() {
        initTopbar(topbar, "提现账户")
        requestDistrCashInfo()
    }

    private var firstVisit = true
    override fun onResume() {
        super.onResume()
        if (firstVisit) {
            firstVisit = false
        }
        if (!firstVisit) {
            requestDistrCashInfo()
        }
    }




    private fun requestDistrCashInfo() {
        http {
            url = "apiv5/distrcashinfo"
            onSuccessWithMsg { res, _ ->
                val data = res.toPOJO<DistrCashInfoBean>().data
                if ("1" == data.is_alipay) {
                    aliPayAccountText?.text = data.alipay_acct
                    aliPayBindBtn?.text = "修改"
                } else {
                    aliPayAccountText?.text = "还未绑定支付宝账户"
                    aliPayBindBtn?.text = "去绑定"
                }
                if ("1" ==data.is_wx ) {
                    weChatAccountText?.text = data.wx_nickname
                    weChatBindBtn?.text = "授权绑定"
                } else {
                    weChatAccountText?.text = "还未授权绑定微信账户"
                    weChatBindBtn?.text = "授权绑定"
                }
                aliPayBindBtn?.setOnClickListener {
                    val intent=Intent(mActivity, BindAliPayActivity::class.java)
                    if ("1" == data.is_alipay) {
                        intent.putExtra("accountName",data.alipay_acct)
                        intent.putExtra("realName","")
                    }
                    startActivity(intent)
                }
                weChatBindBtn?.setOnClickListener {
                    WXEntryActivity.wxLogin(mActivity, object : WXEntryActivity.WxCallback {
                        override fun onsuccess(code: String?, msg: String?) {

                            bothNotNull(code, msg) { getCode, _ ->
                                requestBandWx(getCode)
                            }
                        }

                        override fun onFail(msg: String?) {

                        }
                    })

                }
            }

            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }
        }
    }

    private fun requestBandWx(code: String) {
        http {
            url = "apiv5/bandwx"
            params["code"] = code
            onSuccessWithMsg { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
                requestDistrCashInfo()
            }

            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }
        }
    }

}