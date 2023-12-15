package com.ruimeng.things.home

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.flyco.roundview.RoundTextView
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.FgtMain
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.ChangeRentBatteryBean
import com.ruimeng.things.home.bean.ChangeRentBatteryPayInfoBean
import com.ruimeng.things.showConfirmDialog
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_change_rent_battery.*
import org.greenrobot.eventbus.EventBus
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2020/1/8.
 */
class FgtChangeRentBattery : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_change_rent_battery


    companion object {
        fun newInstance(oldContractId: String, newDeviceId: String): FgtChangeRentBattery {
            return FgtChangeRentBattery().apply {
                arguments = Bundle().apply {
                    putString("oldContractId", oldContractId)
                    putString("newDeviceId", newDeviceId.trim())
                }
            }
        }
    }


    private val oldContractId: String by lazy { arguments?.getString("oldContractId") ?: "" }
    private val newDeviceId: String by lazy { arguments?.getString("newDeviceId") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "更换电池")

        getInfo()
    }


    private fun getInfo() {

        http {
            url = PathV3.CHANGE_DEVICE

            params["contract_id"] = oldContractId
            params["new_deviceid"] = newDeviceId

            onSuccessWithMsg { res, msg ->

                rtv_submit?.let {
                    val data = res.toPOJO<ChangeRentBatteryBean>().data
                    renderView(data)
                }

            }


            onFail { code, msg ->
                NormalDialog(activity)
                    .apply {
                        style(NormalDialog.STYLE_TWO)
                        title("重要提示 ")
                        content(msg)
                        btnNum(1)
                        btnText("确定")
                        setOnBtnClickL(OnBtnClickL {
                            dismiss()
                        })
                        show()
                    }
            }

        }
    }

    private var isReturnMoney = false

    private fun renderView(data: ChangeRentBatteryBean.Data) {

        tv_device_num_current.text = "编号：${data.device_info.device_id}"
        tv_rent_long_current.text = "租期：${data.device_info.rent_time}"
        tv_device_model_current.text = "型号：${data.device_info.model_str}"
        tv_deposit_current.text = "押金：¥${data.device_info.deposit}"




        tv_device_num_new.text = "编号：${data.new_device_info.device_id}"
        tv_rent_long_new.text = "租期：${data.new_device_info.rent_time}"
        tv_device_model_new.text = "型号：${data.new_device_info.model_str}"
        tv_deposit_new.text = "押金：¥${data.new_device_info.deposit}"


        isReturnMoney = data.diff_pay.total_diff.contains("-")
        val diffColor =
            Color.parseColor(if (isReturnMoney) "#FF0000" else "#00A0E9")

        tv_tips_status.apply {
            setTextColor(diffColor)
            text = if (isReturnMoney) "需要退还给您的金额为" else "您需要补充的费用金额为"
        }


        tv_rent.text = "租金：¥${data.diff_pay.rent}"
        tv_deposit.text = "押金：¥${data.diff_pay.deposit}"

        tv_total.apply {
            setTextColor(diffColor)
            text = "合计：¥${data.diff_pay.total_diff}"
        }


        rtv_submit.apply {
            setTextColor(diffColor)
            text = if (isReturnMoney) "申请退返" else "立即支付"



            setOnClickListener {
                if (TextUtils.isEmpty(inputCode?.text)){
                    Toast.makeText(activity,inputCode?.hint.toString(),Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                showConfirmDialog(activity, msg = "确定更换电池吗?", confirmClick = {

                    if (isReturnMoney) {
                        doGetPayInfo(data.tradno, 0,inputCode?.text.toString())
                    }else{

                        showPayWayLayout(data.tradno,inputCode?.text.toString())}
                })

            }
        }


    }


    private fun doGetPayInfo(tradNo: String, payType: Int,code: String) {

        http {
            url = PathV3.CHANGE_PAY

            params["contract_id"] = oldContractId
            params["new_deviceid"] = newDeviceId
            params["tradno"] = tradNo
            params["pay_type"] = payType.toString()  // 退钱 传 0
            params["code"]=code
            onSuccessWithMsg { res, msg ->
                rtv_submit?.let {

                    val data = res.toPOJO<ChangeRentBatteryPayInfoBean>().data

                    if (isReturnMoney) {
                        showReturnTips()
                    } else
                        doPay(data, payType == 1)
                }

            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }

    }


    private fun showReturnTips() {

        activity ?: return
        AnyLayer.with(activity!!)
            .contentView(R.layout.layout_change_rent_battery_return_money_tips)
            .backgroundColorInt(Color.parseColor("#85000000"))
            .bindData { anyLayer ->

                anyLayer.contentView.findViewById<TextView>(R.id.tv_tips).apply {
                    text = "已申请退返更换设备差价费用,预计24小时内返回您的原账户"
                }


                anyLayer.contentView.findViewById<RoundTextView>(R.id.rtv_submit).apply {
                    setOnClickListener {
                        anyLayer.dismiss()
                        paySuccessed()
                    }
                }
            }
            .show()

    }


    private fun showPayWayLayout(tradNo: String,code: String) {

        activity ?: return
        AnyLayer.with(activity!!)
            .contentView(R.layout.layout_select_pay_way)
            .backgroundColorInt(Color.parseColor("#85000000"))
            .bindData { anyLayer ->


                // 1微信支付2支付宝支付 如果差值小于0 才弹出支付方式选择 【新增】20200213

                anyLayer.contentView.findViewById<LinearLayout>(R.id.ll_wechat_pay).apply {
                    setOnClickListener {
                        anyLayer.dismiss()
                        doGetPayInfo(tradNo, 1,code)
                    }
                }

                anyLayer.contentView.findViewById<LinearLayout>(R.id.ll_alipay).apply {
                    setOnClickListener {
                        anyLayer.dismiss()
                        doGetPayInfo(tradNo, 2,code)
                    }
                }


                anyLayer.contentView.findViewById<RoundTextView>(R.id.rtv_submit).apply {
                    setOnClickListener {

                    }
                }
            }
            .show()

    }

    private fun doPay(result: ChangeRentBatteryPayInfoBean.Data, isPayByWechat: Boolean) {
        dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
        dlgPaySuccessed =
            getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") { paySuccessed() }
        dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")

        dlgPayProgress?.show()


        if (isPayByWechat) {
            val entity = WXEntryActivity.WxPayEntity()
            result.wxpay.let {
                entity.appId = it.appId
                entity.nonceStr = it.nonceStr
                entity.packageValue = it.packageValue
                entity.partnerId = it.partnerId
                entity.prepayId = it.prepayId
                entity.sign = it.sign
                entity.timeStamp = it.timeStamp
            }

            WXEntryActivity.wxPay(
                activity,
                entity,
                object : WXEntryActivity.WxCallback {
                    override fun onsuccess(code: String?, msg: String?) {
//                        getServerPayResult(result.orderid, true)

                        dlgPayProgress?.dismiss()
                        dlgPaySuccessed?.show()
                    }

                    override fun onFail(msg: String?) {
//                        getServerPayResult(result.orderid, false)
                        dlgPayProgress?.dismiss()
                        dlgPayFailed?.show()
                    }
                })
        } else {

            BaseAlipay.tryPay(result.alipay.paystr) { resultInfo, resultStatus, isLocalSuccessed ->
                //                getServerPayResult(result.orderid, isLocalSuccessed)


                dlgPayProgress?.dismiss()
                if (isLocalSuccessed) {
                    dlgPaySuccessed?.show()
                } else {
                    dlgPayFailed?.show()
                }
            }

        }


    }


    private var dlgPayProgress: SweetAlertDialog? = null

    private var dlgPaySuccessed: SweetAlertDialog? = null

    private var dlgPayFailed: SweetAlertDialog? = null


    /**
     * 支付成功
     */
    private fun paySuccessed() {
        EventBus.getDefault().post(BatteryInfoChangeEvent(newDeviceId))
        EventBus.getDefault().post(FgtMain.Companion.SwitchTabEvent(0))
        pop()
    }
}