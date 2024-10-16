package com.ruimeng.things.me.activity

import android.content.Intent
import android.os.Bundle
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.DistrCashInfoBean
import com.utils.ToastHelper
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
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
                    aliPayAccountText?.text = "账号："+secretAccount(data.alipay_acct)
                    aliPayBindBtn?.text = "重新绑定"
                    aliPayBindBtn.setBackgroundResource(R.drawable.bg_btn_common2)
                    aliPayBindBtn.setTextColor(resources.getColor(R.color.white))
                } else {
                    aliPayAccountText?.text = "未绑定"
                    aliPayBindBtn?.text = "立即绑定"
                    aliPayBindBtn.setBackgroundResource(R.drawable.bg_btn_common1)
                    aliPayBindBtn.setTextColor(resources.getColor(R.color.black_3))
                }
                if ("1" ==data.is_wx ) {
                    weChatAccountText?.text = "账号："+secretAccount(data.wx_nickname)
                    weChatBindBtn?.text = "重新授权"
                    weChatBindBtn.setBackgroundResource(R.drawable.bg_btn_common2)
                    weChatBindBtn.setTextColor(resources.getColor(R.color.white))
                } else {
                    weChatAccountText?.text = "未授权"
                    weChatBindBtn?.text = "立即授权"
                    weChatBindBtn.setBackgroundResource(R.drawable.bg_btn_common1)
                    weChatBindBtn.setTextColor(resources.getColor(R.color.black_3))
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
    private fun secretAccount(string: String):String{
        if (string.length == 2){
            return "*${string.substring(1,2)}"
        }else if (string.length > 2){
            return "${string.substring(0,string.length-3)}**${string.substring(string.length-2,string.length-1)}"
        }else{
            return string
        }
    }

    private fun requestBandWx(code: String) {
        http {
            url = "apiv5/bandwx"
            params["code"] = code
            params["appType"] = "lxhd"
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