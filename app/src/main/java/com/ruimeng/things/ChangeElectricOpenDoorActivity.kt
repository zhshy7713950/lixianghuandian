package com.ruimeng.things

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ruimeng.things.home.bean.PaymentOption
import com.utils.TextUtil
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_change_electric_open_door.*
import wongxd.Config
import wongxd.base.AtyBase
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.SystemUtils


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

    var needActiveNew = false
    var optionAct: PaymentOption ? = null
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


                var textColors = arrayOf("#929FAB","#FFFFFF")
                tv_base_package_name.text = TextUtil.getSpannableString(arrayOf("基础套餐    ",intent.getStringExtra("name")),textColors)
                val option = data.userOptions.filter { it.active_status == "1" }.first()
                if (option != null){
                    tv_change_package_type.text = TextUtil.getSpannableString(arrayOf("换电类型    ",option.name),textColors)
                    tv_package_left_times.text = TextUtil.getSpannableString(arrayOf("剩余次数    ",option.change_times),textColors)
                    tv_change_package_time.text = TextUtil.getSpannableString(arrayOf("有效期     ",TextUtil.formatTime(option.start_time,option.end_time)),textColors)
                    if (option.change_times == "1"){
                        needActiveNew = true
                    }
                }else{
                    if (data.singleChangeInfo != null){
                        tv_change_package_type.text = TextUtil.getSpannableString(arrayOf("换电类型    ","单次换电"),textColors)
                        tv_package_left_times.text = TextUtil.getSpannableString(arrayOf("剩余次数    ","1"),textColors)
                        tv_change_package_time.text = TextUtil.getSpannableString(arrayOf("有效期     ",TextUtil.formatTime(data.singleChangeInfo.start_time,data.singleChangeInfo.end_time)),textColors)
                        needActiveNew = true
                    }else{
                        ToastHelper.shortToast(this@ChangeElectricOpenDoorActivity,"没有找到生效的套餐，请购买")
                    }
                }
                if (needActiveNew == true){
                    needActiveNew = data.userOptions.count { it.active_status == "2" } > 0
                    optionAct = data.userOptions.filter { it.active_status == "2" }.first()
                }
                confirmBtn?.setOnClickListener {
                    requestConfirmCgDevice(getContractId, data.new_info.device_id)
                }

                if (data.singleChangeInfo != null){
                    tv_change_package_type.text = TextUtil.getSpannableString(arrayOf("换电类型    ","单次换电"),textColors)
                    tv_package_left_times.text = TextUtil.getSpannableString(arrayOf("剩余次数    ","1"),textColors)
                    tv_change_package_time.text = TextUtil.getSpannableString(arrayOf("有效期     ",TextUtil.formatTime(data.singleChangeInfo.start_time,data.singleChangeInfo.end_time)),textColors)
                }else{

                    if (option != null){
                        tv_change_package_type.text = TextUtil.getSpannableString(arrayOf("换电类型    ",option.name),textColors)
                        tv_package_left_times.text = TextUtil.getSpannableString(arrayOf("剩余次数    ",option.change_times),textColors)
                        tv_change_package_time.text = TextUtil.getSpannableString(arrayOf("有效期     ",TextUtil.formatTime(option.start_time,option.end_time)),textColors)
                    }else if (data.userOptions.count { it.active_status == "2" } > 0){


                    }else{

                    }
                  }
            }

            onFail { i, msg ->
                ToastHelper.shortToast(mActivity, msg)
                finish()
            }
        }
    }
    private fun activeOption(option: PaymentOption){
        NormalDialog(this).apply {
            style(NormalDialog.STYLE_TWO)
            title("当前您的换电套餐剩余次数已为0，是否立即启动待生效套餐：${option.name}")
            titleTextColor(Color.parseColor("#131414"))
            btnText("确认启用", "暂不启用")
            btnTextColor(Color.parseColor("#29EBB6"), Color.parseColor("#FF6464"))
            setOnBtnClickL(OnBtnClickL {
                http {
                    url = "/apiv6/payment/activeoption"
                    params["user_option_id"] = "${option.option_id}"
                    onSuccess {
                        ToastHelper.shortToast(context,"启用成功")
                        dismiss()
                        pop()
                    }
                    onFail { i, s ->
                        ToastHelper.shortToast(context,s)
                        dismiss()
                    }
                }

            }, OnBtnClickL {
                dismiss()
            })
            show()
        }
    }

    private fun requestConfirmCgDevice(contractId: String, newDeviceId: String) {
        http {
            url = "apiv4/confirmcgdevice"
            params["contract_id"] = contractId
            params["new_deviceid"] = newDeviceId
            onSuccessWithMsg { res, msg ->
                if (needActiveNew && optionAct != null){
                    activeOption(optionAct!!)
                }else{
                    ToastHelper.shortToast(mActivity, msg)
                    finish()
                }

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