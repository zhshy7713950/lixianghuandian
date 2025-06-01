package com.ruimeng.things.me.contract

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.webkit.WebViewClient
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.Path
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.home.ContractCheckEvent
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.FgtUploadAuthInfo
import com.ruimeng.things.me.contract.bean.ContractSignStepOneBean
import com.ruimeng.things.me.contract.bean.ProtocolBean
import kotlinx.android.synthetic.main.fgt_contract_sign_step_1.*
import org.greenrobot.eventbus.EventBus
import wongxd.base.BaseBackFragment
import wongxd.common.*
import wongxd.http

/**
 * Created by wongxd on 2019/12/30.
 * https://github.com/wongxd
 * wxd1@live.com
 */
class FgtContractSignStep1 : BaseBackFragment() {

    companion object {

        val RESULT_CODE_SHOULD_POP = 1002

        fun newInstance(
            contractId: String,
            qStr: String,
            contractType: Int,
            pageType: Int = 0,
            deviceId: String = "",
            deviceModel: String = ""
        ): FgtContractSignStep1 {
            val fgt = FgtContractSignStep1()
            val b = Bundle()
            b.putString("contractId", contractId)
            b.putString("qStr", qStr)
            b.putString("deviceId", deviceId)
            b.putString("deviceModel", deviceModel)
            b.putInt("contractType", contractType)
            b.putInt("pageType", pageType)
            fgt.arguments = b
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_contract_sign_step_1

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }

    private val qStr by lazy { arguments?.getString("qStr") ?: "" }
    private val deviceModel by lazy { arguments?.getString("deviceModel") ?: "" }
    private val deviceId by lazy { arguments?.getString("deviceId") ?: "" }
    private val contractType by lazy { arguments?.getInt("contractType") ?: 1 }

    // 0 从合约列表进入， 1 查看合约（支付押金、单次购买、续期升级）  2 查看合约需要签名（支付租金）
    private val getPageType by lazy { arguments?.getInt("pageType", 0) }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, if (getPageType == 1) "租赁协议" else "合同签约")
        progressDlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "加载中", false)
        progressDlg?.show()
        getContractInfo()
    }


    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CODE_SHOULD_POP && resultCode == RESULT_CODE_SHOULD_POP) {
            EventBus.getDefault().post(FgtMyContract.EventDoContractSearch(qStr, contractType))
            pop()
        }
    }

    private fun getContractInfo() {
        http {
            url = Path.GET_PROTOCOL
            params["userId"] = FgtHome.userId
            params["deviceId"] = FgtHome.CURRENT_DEVICEID

            onSuccess { res ->
                val bean = res.toPOJO<ProtocolBean>()
                if(!bean?.data.isNullOrEmpty()){
                    wvContract?.apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl(bean?.data)
                    }
                }
            }
        }
        http {
            url = if (getPageType == 1) PathV3.PAYMENT_SIGN_CONTRACT else PathV3.SIGN_CONTRACT
            if (getPageType != 1) {
                params["contract_id"] = contractId
            }
            params["appType"] = "lxhd"
            onFinish {
                progressDlg?.dismissWithAnimation()
            }

            onSuccess { res ->

                wvContract?.let {

                    val bean = res.toPOJO<ContractSignStepOneBean>()
                    val data = bean.data

                    if (getPageType != 0) {
                        layout_battery1.visibility = View.VISIBLE
                        layout_battery2.visibility = View.GONE
                        if (getPageType == 1) {
                            tv_battery_num_pay_rent_money.text = deviceId
                            tv_battery_model_pay_rent_money.text = deviceModel
                        } else {
                            tv_battery_num_pay_rent_money.text = "${data.device_id}"
                            tv_battery_model_pay_rent_money.text = "${data.model_str}"
                        }
                    } else {
                        layout_battery1.visibility = View.GONE
                        layout_battery2.visibility = View.VISIBLE
                        tv_device_num_my_contract_detail.text = "电池编号：${data.device_id}"
                        tv_device_model_my_contract_detail.text = "${data.model_str}"
                        tv_rent_long_my_contract_detail.text = "${data.renttime_str}"
                        tv_deposit_my_contract_detail.text = "${data.deposit}元"
                        tv_rent_money_my_contract_detail.text = "${data.rent}元"
                    }

                    if (getPageType != 1) {
                        object : CountDownTimer((data.wait_sec * 1000).toLong(), 1000.toLong()) {
                            override fun onTick(millisUntilFinished: Long) {
                                MainLooper.runOnUiThread {
                                    btn_sign_contract?.text =
                                        "我已阅读并同意合同内容(${millisUntilFinished / 1000}s)"
                                    btn_sign_contract?.setOnClickListener {}
                                }
                            }

                            override fun onFinish() {
                                MainLooper.runOnUiThread {
                                    btn_sign_contract?.text = "我已阅读并同意合同内容"
                                    btn_sign_contract?.setOnClickListener {
                                        startForResult(
                                            FgtContractSignStep2.newInstance(data.contract_id),
                                            RESULT_CODE_SHOULD_POP
                                        )
                                    }
                                }
                            }
                        }.start()
                    } else {
                        btn_sign_contract.visibility = View.GONE
//                        btn_sign_contract?.text = "我已确认"
//                        btn_sign_contract.setOnClickListener {
//                            EventBus.getDefault().post(ContractCheckEvent(true))
//                            pop()
//                        }
                    }
                }

            }

            onFail { code, msg ->
                wvContract?.let {
                    if (code == 901) {
                        //跳转到上传身份证信息
                        startWithPop(FgtUploadAuthInfo.newInstance(contractId))
                    } else
                        EasyToast.DEFAULT.show(msg)
                }
            }
        }
    }

    private var progressDlg: SweetAlertDialog? = null

}