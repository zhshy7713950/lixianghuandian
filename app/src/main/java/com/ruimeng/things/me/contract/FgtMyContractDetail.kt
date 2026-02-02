package com.ruimeng.things.me.contract

import android.os.Bundle
import android.os.Environment
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.ruimeng.things.Path
import com.ruimeng.things.PathV3
import com.ruimeng.things.UserInfoLiveData
import com.ruimeng.things.me.contract.bean.MyContractDetailBean
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_deposit_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_device_model_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_device_num_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_rent_long_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_rent_money_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.webview
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.toPOJO
import wongxd.http
import java.io.File


/**
 * Created by wongxd on 2019/12/24.
 */
class FgtMyContractDetail : BaseBackFragment() {

    companion object {

        fun newInstance(contractId: String, deviceId: Int): FgtMyContractDetail {

            return FgtMyContractDetail().apply {
                arguments = Bundle().apply {
                    putString("contractId", contractId)
                    putInt("deviceId", deviceId)
                }
            }
        }
    }

    private val contractId by lazy { arguments?.getString("contractId", "") ?: "" }
    private val deviceId by lazy { arguments?.getInt("deviceId", 0) ?: 0 }

    override fun getLayoutRes(): Int = com.ruimeng.things.R.layout.fgt_my_contract_detail


    val rootDir by lazy {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "${getString(com.ruimeng.things.R.string.app_name)}合约"
    }

    val thisContractDir by lazy {
        rootDir + "编号:$deviceId"
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "合约详情")


        val settings = webview.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        webview.webViewClient = WebViewClient()
        webview.webChromeClient = WebChromeClient()

        getInfo()
    }

    private fun getInfo() {

        http {
            url = PathV3.MY_CONTRACT_DETAIL
            params["contract_id"] = contractId

            onSuccess { res ->
                tv_device_num_my_contract_detail?.let {

                    val bean = res.toPOJO<MyContractDetailBean>().data

                    tv_device_num_my_contract_detail.text = "编号：${bean.device_id}"
                    tv_device_model_my_contract_detail.text = "${bean.model_str}"
                    tv_rent_long_my_contract_detail.text = "租期：${bean.renttime_str}"
                    tv_deposit_my_contract_detail.text = "押金：¥${bean.deposit}"
                    tv_rent_money_my_contract_detail.text = "租金：¥${bean.rent}"

                    loadProtocol(bean.device_id.toString())
                }

            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }

    private fun loadProtocol(deviceId: String) {
        val userId = UserInfoLiveData.getFromString().id
        if (userId.isBlank()) return

        http {
            url = Path.GET_PROTOCOL_V6
            params["userId"] = userId
            params["deviceId"] = deviceId

            onSuccess {
                try {
                    val jsonObject = JSONObject(it)
                    val dataUrl = jsonObject.optString("data")
                    if (dataUrl.isNotBlank()) {
                        webview.loadUrl(dataUrl)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


}