package com.ruimeng.things.me

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.flyco.roundview.RoundTextView
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.App
import com.ruimeng.things.FgtMain
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.ScanQrCodeActivity
import com.ruimeng.things.bean.isSH
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.bean.ChangeRentBatteryBean
import com.ruimeng.things.home.bean.ChangeRentBatteryPayInfoBean
import com.ruimeng.things.home.bean.PaymentDetailBean
import com.ruimeng.things.me.view.QuitAllowancePopup
import com.ruimeng.things.me.view.RebackAlertPopup
import com.ruimeng.things.showConfirmDialog
import com.ruimeng.things.voice.VoicePlayerManager
import com.utils.TextUtil
import com.utils.ToastHelper
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_battery_hole
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_battery_num
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_battery_status
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_deposit_return
import kotlinx.android.synthetic.main.fgt_me_deposite.tv_remark
import org.greenrobot.eventbus.EventBus
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.getCurrentAty
import wongxd.common.getSweetDialog
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2020/1/8.
 */
class FgtMeDeposit : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_me_deposite


    companion object {
        fun newInstance(): FgtMeDeposit {
            return FgtMeDeposit().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "账号押金")

        getInfo()
    }

    var virtaul = false
    var deviceId = ""
    private var originPrice: Double = 0.0
    private var couponPrice: Double = 0.0
    private var paymentDetailBean: PaymentDetailBean.Data? = null
    
    private fun getInfo() {
        deviceId =
            if (FgtHome.CURRENT_DEVICEID == "0") FgtHome.NO_PAY_DEVICEID else FgtHome.CURRENT_DEVICEID
        if (deviceId.startsWith("8") && deviceId.length == 8) {
            virtaul = true
        }
        
        // 默认显示：按钮“立即退租”，隐藏备注
        tv_deposit_return.text = "立即退租"
        tv_remark.visibility = View.GONE

        tv_battery_num.text = "电池编号：" + deviceId

        http {
            url = "/apiv6/payment/getuserpaymentinfo"
            params["user_id"] = FgtHome.userId
            params["device_id"] = deviceId
            IS_SHOW_MSG = false
            onSuccess { res ->
                try {
                    val data = res.toPOJO<PaymentDetailBean>().data
                    paymentDetailBean = data
                    originPrice = data.nextMonthPayment?.originPrice ?: 0.0
                    couponPrice = data.nextMonthPayment?.couponPrice ?: 0.0

                    updateUIByPaymentDetail(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        tv_deposit_return.setOnClickListener {
            checkReturnSubsidy()
        }
    }

    private fun updateUIByPaymentDetail(data: PaymentDetailBean.Data) {
        val textColors = arrayOf("#929FAB", "#FFFFFF")
        var typeStr = ""
        var depositStr = ""

        if (data.buyFreeDeposit != null) {
            typeStr = if (data.pay_type == "1") "微信支付" else "支付宝支付"
            depositStr = "免押权益"

            if (virtaul) {
                tv_deposit_return.text = "关闭免押权益"
                tv_remark.visibility = View.VISIBLE
                tv_remark.text = "权益关闭后，免押服务将自动失效"
            }
        } else {
            if (data.pay_type == "1" || data.pay_type == "2") {
                typeStr = if (data.pay_type == "1") "微信支付" else "支付宝支付"
                depositStr = data.deposit.toString()

                if (virtaul) {
                    tv_deposit_return.text = "申请退还押金"
                    tv_remark.visibility = View.VISIBLE
                    tv_remark.text = "退还押金申请通过后，1-2个工作日到账"
                }
            } else if (data.pay_type == "99" || data.pay_type == "101" || data.pay_type == "102") {
                when (data.pay_type) {
                    "99" -> {
                        typeStr = "线下免押"
                        depositStr = "线下免押"
                    }
                    "101" -> {
                        typeStr = "芝麻信用(免押)"
                        depositStr = "芝麻免押"
                    }
                    "102" -> {
                        typeStr = "集团支付"
                        depositStr = "集团免押"
                    }
                }

                if (virtaul) {
                    tv_deposit_return.text = "申请解绑免押"
                    tv_remark.visibility = View.VISIBLE
                    tv_remark.text = "解绑免押申请通过后，将自动解除免押绑定"
                }
            }
        }

        tv_battery_status.text = TextUtil.getSpannableString(arrayOf("支付渠道：", typeStr), textColors)
        tv_battery_hole.text = TextUtil.getSpannableString(arrayOf("电池押金：", depositStr), textColors)
    }

    private fun doOldFlowReturnDeposit() {
        if (virtaul) {
            val payType = paymentDetailBean?.pay_type ?: ""
            val hasBuyFreeDeposit = paymentDetailBean?.buyFreeDeposit != null
            
            var dialogTitle = ""
            var dialogDesc = "请确认是否继续操作？"
            
            if (hasBuyFreeDeposit) {
                dialogTitle = "免押权益关闭后，剩余套餐将清零，请确认操作！"
            } else {
                if (payType == "1" || payType == "2") {
                    dialogTitle = "押金退还完成后，剩余套餐将清零，请确认操作！"
                } else if (payType == "99" || payType == "101" || payType == "102") {
                    dialogTitle = "免押解绑完成后，剩余套餐将清零，请确认操作！"
                } else {
                    // Fallback just in case
                    dialogTitle = "免押解绑/押金退还结束后，剩余套餐将清零，请确认操作！"
                }
            }
            
            NormalDialog(activity)
                .apply {
                    style(NormalDialog.STYLE_TWO)
                    btnNum(2)
                    title(dialogTitle)
                    content(dialogDesc)
                    btnText("确认", "取消")
                    setOnBtnClickL(OnBtnClickL {
                        dismiss()
                        http {
                            url = "/apiv6/payment/getuserpaymentinfo"
                            params["user_id"] = FgtHome.userId
                            params["device_id"] = deviceId
                            IS_SHOW_MSG = false
                            onSuccess { res ->
                                var paymentDetailBean = res.toPOJO<PaymentDetailBean>().data
                                tryReturnDeposit(paymentDetailBean.contract_id)
                            }
                        }
                    }, OnBtnClickL {
                        dismiss()
                    })

                }.show()
        } else {
            VoicePlayerManager.getInstance().playVoice(requireContext(), "tip-1")
            ToastHelper.shortToast(context, "请扫描电柜二维码")
            getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                val intent = Intent(activity, ScanQrCodeActivity::class.java)
                intent.putExtra("type", "退还")
                startActivityForResult(intent, 1)
            })
        }
    }

    private fun checkReturnSubsidy() {
        http {
            url = "/apiv6/user/returnsubsidy"
            params["user_id"] = FgtHome.userId
            params["device_id"] = deviceId
            IS_SHOW_MSG = false
            
            onSuccess { res ->
                try {
                    val obj = org.json.JSONObject(res)
                    val data = obj.optJSONObject("data")
                    val sendCouponPrice = data?.optDouble("sendCouponPrice", 0.0) ?: 0.0
                    if (sendCouponPrice > 0) {
                        val grant = if (sendCouponPrice % 1 == 0.0) {
                            sendCouponPrice.toInt().toString()
                        } else {
                            String.format("%.2f", sendCouponPrice)
                        }
                        
                        val maxPrice = Math.max(couponPrice, sendCouponPrice)
                        var finalPrice = originPrice - maxPrice
                        if (finalPrice < 0.0) finalPrice = 0.0
                        
                        val priceStr = if (finalPrice % 1 == 0.0) {
                            finalPrice.toInt().toString()
                        } else {
                            String.format("%.2f", finalPrice)
                        }

                        QuitAllowancePopup(
                            requireActivity(),
                            grant,
                            priceStr
                        ) {
                            pop()
                        }.show(topbar)
                    } else {
                        doOldFlowReturnDeposit()
                    }
                } catch (e: Exception) {
                    doOldFlowReturnDeposit()
                }
            }
            onFail { _, _ ->
                doOldFlowReturnDeposit()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == 1) {
                if (null != data) {
                    val bundle = data.extras ?: return
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {

                        val result = bundle.getString(CodeUtils.RESULT_STRING)
                        val getType = bundle.getString("type")
                        if ("退还" == getType) {
                            if (result != null) {
                                val hasRecomActivity =
                                    InfoViewModel.getDefault().userInfo.value?.isSH() ?: false
                                RebackAlertPopup(getCurrentAty(), hasRecomActivity,
                                    View.OnClickListener { returnBattery(result) }).show(rootView)

                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 柜内租用电池
     */
    private fun returnBattery(code: String) {
        http {
            url = "apiv6/cabinet/retrunBattery"
            params["user_id"] = FgtHome.userId
            params["code"] = code
            params["device_id"] = deviceId
            onSuccessWithMsg { res, msg ->
                // 播放成功语音
                VoicePlayerManager.getInstance().playVoice(requireContext(), "success-8")
                ToastHelper.shortToast(activity, "请将电池放入电柜，后台自动审核")
                EventBus.getDefault().post(FgtMain.SwitchPageEvent(0))
                pop()
            }

            onFail { i, msg ->
                // 播放失败语音
                VoicePlayerManager.getInstance().playVoice(requireContext(), "fail-1")
                ToastHelper.shortToast(activity, msg)
            }
        }
    }

    private fun tryReturnDeposit(contractId: String) {
        http {
            url = PathV3.RETURN_DEPOIST
            params["contract_id"] = contractId

            onSuccessWithMsg { res, msg ->
                if (null != activity) {
                    deviceId = ""
                    NormalDialog(activity)
                        .apply {
                            style(NormalDialog.STYLE_TWO)
                            btnNum(1)
                            title("提示")
                            content("操作成功")
                            btnText("确认")
                            setOnBtnClickL(OnBtnClickL {
                                dismiss()
                                EventBus.getDefault().post(FgtMain.SwitchPageEvent(0))
                                pop()
                            })
                        }.show()
                } else {
                    EasyToast.DEFAULT.show("操作成功")
                }
            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show("操作失败，请稍后重试")
            }
        }
    }

}