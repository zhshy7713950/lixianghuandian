package com.ruimeng.things.home.helper

import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.home.FgtHome
import org.json.JSONObject
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.ToastUtils

class ScanResultCheck {

    /**
     * 扫描结束检查方法
     * type: 1.扫码开门：apiv6/payment/checkpayment
    2.手动换电：apiv4/cgscan
    3.支付押金、租金、实名（”点击添加“按钮、”点击购买套餐“按钮、”右上角扫码“按钮）：apiv4/rentstep1
     4.租电
     5.退还
     */
    fun checkResult(type:Int, result:String, listener: CheckResultListener){
        when(type){
            1 -> {
                http{
                    url = "/apiv6/payment/checkpayment"
                    params["user_id"] = "${InfoViewModel.getDefault().userInfo.value?.id}"
                    params["device_id"] = FgtHome.CURRENT_DEVICEID
                    params["code"] = result
                    onSuccess {
                        listener.checkStatus(true)
                    }
                    onFail { i, s ->
                        listener.checkStatus(false)
                    }
                }
            }
            2 -> {
                http {
                    url = "apiv4/cgscan"
                    params["contract_id"] = FgtHome.contractId
                    params["new_deviceid"] = result
                    onSuccess {
                        listener.checkStatus(true)
                    }
                    onFail { i, s ->
                        listener.checkStatus(false)
                    }
                }
                listener.checkStatus(true)
            }
            3->listener.checkStatus(true)
            4->listener.checkStatus(true)
            5->listener.checkStatus(true)
            6->listener.checkStatus(true)

        }

    }
    public interface  CheckResultListener{
        fun checkStatus(pass:Boolean);
    }
}