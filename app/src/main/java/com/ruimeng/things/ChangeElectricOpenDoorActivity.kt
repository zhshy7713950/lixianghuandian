package com.ruimeng.things

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_change_electric_open_door.*
import wongxd.base.AtyBase
import wongxd.common.toPOJO
import wongxd.http


class ChangeElectricOpenDoorActivity : AtyBase() {

    private var mActivity: AppCompatActivity? = null

    private var getType = ""
    private var getContractId = ""
    private var getNewDeviceId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_electric_open_door)
        mActivity = this
        initTopbar(topbar, "换电扫码")
        if (!TextUtils.isEmpty(intent.getStringExtra("type"))) {
            getType = intent.getStringExtra("type")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("contract_id"))) {
            getContractId = intent.getStringExtra("contract_id")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("new_deviceid"))) {
            getNewDeviceId = intent.getStringExtra("new_deviceid")
        }
        requestCgScan(getContractId, getNewDeviceId)

    }



    @SuppressLint("SetTextI18n")
    private fun requestCgScan(contractId: String, newDeviceId: String) {
        http {
            url = "apiv4/cgscan"
            params["contract_id"] = contractId
            params["new_deviceid"] = newDeviceId
            onSuccessWithMsg { res, msg ->
                ToastHelper.shortToast(mActivity, msg)
                contentLayout?.visibility = View.VISIBLE

                val data = res.toPOJO<CgScanBean>().data
                currentNumberText?.text = "${data.old_info.device_id}"
                currentValueText?.text = "${data.old_info.electricity}%"
                currentModelText?.text = "${data.old_info.model_str}"
                newNumberText?.text = "${data.new_info.device_id}"
                newValueText?.text = "${data.new_info.electricity}%"
                newModelText?.text = "${data.new_info.model_str}"

                tipsText?.text = data.exchange_tips

                confirmBtn?.setOnClickListener {
                    requestConfirmCgDevice(getContractId, data.new_info.device_id)
                }
            }

            onFail { i, msg ->
                ToastHelper.shortToast(mActivity, msg)
                finish()
            }
        }
    }

    private fun requestConfirmCgDevice(contractId: String, newDeviceId: String) {
        http {
            url = "apiv4/confirmcgdevice"
            params["contract_id"] = contractId
            params["new_deviceid"] = newDeviceId
            onSuccessWithMsg { res, msg ->
                ToastHelper.shortToast(mActivity, msg)
                finish()
            }

            onFail { i, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }
        }
    }

    private fun requestCgOpenDoor(code: String) {
        http {
            url = "apiv4/cgopendoor"
            params["code"] = code

            onSuccessWithMsg { res, msg ->
                ToastHelper.shortToast(mActivity, msg)
                finish()
            }

            onFail { i, msg ->
                ToastHelper.shortToast(mActivity, msg)
                finish()
            }
        }
    }

}