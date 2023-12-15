package com.ruimeng.things.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.flyco.roundview.RoundTextView
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.MathUtil
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.ChangeDepositCombinationBean
import com.ruimeng.things.home.bean.ChangeDepositCombinationPayInfoBean
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_change_deposit_combination.*
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.recycleview.yaksa.linear
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2020/2/18.
 */
class FgtChangeDepositCombination : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_change_deposit_combination

    companion object {
        fun newInstance(contractId: String): FgtChangeDepositCombination {
            return FgtChangeDepositCombination().apply {
                arguments = Bundle().apply {
                    putString("contractId", contractId)
                }
            }

        }
    }


    private val contractId by lazy { arguments?.getString("contractId") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "更换租赁组合方式")

        getInfo()
    }


    private fun getInfo() {

        http {
            url = "apiv4/cgdepositinfo"
            params["contract_id"] = contractId

            onSuccess { res ->
                tv_current_battery_change_combination?.let {
                    val data = res.toPOJO<ChangeDepositCombinationBean>().data

                    renderViewAfterData(data)
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

    private var currentDepositOption: ChangeDepositCombinationBean.Data.DepositOption? = null

    private var isReturnMoney = false

    private var deviceId = ""

    private fun renderViewAfterData(data: ChangeDepositCombinationBean.Data) {

        deviceId = data.device_id.toString()

        tv_current_battery_change_combination.text = "当前设备：${data.device_id}"

        tv_current_combination_change_combination.text = data.cur_deposit_name

        tv_tips_change_combination.text = data.tips

        @SuppressLint("SetTextI18n")
        fun refreshStatusView() {
            currentDepositOption ?: return
            tv_tips_status_change_combination ?: return

            isReturnMoney =
                currentDepositOption!!.deposit_host.toDouble() <= data.cur_deposit.toDouble()

            if (isReturnMoney) {
                //退款
                tv_tips_status_change_combination.apply {
                    setTextColor(Color.parseColor("#FF0000"))
                    text = "需要退还给您金额为"
                }

                tv_total_change_combination.apply {
                    setTextColor(Color.parseColor("#FF0000"))
                    text =
                        "合计：￥${MathUtil.reservedDecimal(-currentDepositOption!!.deposit_host.toDouble() + data.cur_deposit.toDouble())}"
                }

                rtv_submit_change_combination.apply {
                    setOnClickListener {
                        showReturnTips()
                    }
                }


            } else {
                //付款
                tv_tips_status_change_combination.apply {
                    setTextColor(Color.parseColor("#00A0E9"))
                    text = "您需要补充的费用金额为"
                }
                tv_total_change_combination.apply {
                    setTextColor(Color.parseColor("#00A0E9"))
                    text =
                        "合计：￥${MathUtil.reservedDecimal(currentDepositOption!!.deposit_host.toDouble() - data.cur_deposit.toDouble())}"
                }

                rtv_submit_change_combination.apply {
                    setOnClickListener {
                        showPayWayLayout()
                    }
                }

            }


        }


        fun renderCombination() {
            //组合

            ll_changed_to_combination_change_combination.setOnClickListener {
                rv_combination_change_combination.visibility =
                    if (rv_combination_change_combination.visibility == View.VISIBLE) {
                        iv_enter_combination_change_combination.apply {
                            rotation = 0f
                        }
                        View.GONE
                    } else {
                        iv_enter_combination_change_combination.apply {
                            rotation = 90f
                        }
                        View.VISIBLE
                    }
            }


            fun checkCombination(
                combinationBean: ChangeDepositCombinationBean.Data.DepositOption
            ) {

                currentDepositOption = combinationBean
                currentDepositOption?.let {
                    tv_changed_to_combination_change_combination.text = it.name
                    tv_deposit_change_combination.text = "押金：￥${it.deposit}"
                    refreshStatusView()
                }
            }



            rv_combination_change_combination.linear {
                data.deposit_option.forEach { combinationBean ->

                    itemDsl {
                        xml(R.layout.item_rv_select_package_deposit)

                        renderX { position, view ->

                            val tv = view.findViewById<TextView>(R.id.tv)

                            tv.text = combinationBean.name

                            view.setOnClickListener {
                                checkCombination(combinationBean)
                                ll_changed_to_combination_change_combination.performClick()
                            }

                        }
                    }
                }
            }


            //组合 end
        }

        renderCombination()
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
                        doGetPayInfos(0)
                    }
                }
            }
            .show()

    }

    private fun showReturnTipss(content:String) {

        activity ?: return
        AnyLayer.with(activity!!)
            .contentView(R.layout.layout_change_rent_battery_return_money_tips)
            .backgroundColorInt(Color.parseColor("#85000000"))
            .bindData { anyLayer ->

                anyLayer.contentView.findViewById<TextView>(R.id.tv_tips).apply {
                    text = content
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


    private fun showPayWayLayout() {

        activity ?: return
        AnyLayer.with(activity!!)
            .contentView(R.layout.layout_select_pay_way)
            .backgroundColorInt(Color.parseColor("#85000000"))
            .bindData { anyLayer ->


                // 1微信支付2支付宝支付 如果差值小于0 才弹出支付方式选择 【新增】20200213

                anyLayer.contentView.findViewById<LinearLayout>(R.id.ll_wechat_pay).apply {
                    setOnClickListener {
                        anyLayer.dismiss()
                        doGetPayInfo(1)
                    }
                }

                anyLayer.contentView.findViewById<LinearLayout>(R.id.ll_alipay).apply {
                    setOnClickListener {
                        anyLayer.dismiss()
                        doGetPayInfo(2)
                    }
                }


                anyLayer.contentView.findViewById<RoundTextView>(R.id.rtv_submit).apply {
                    setOnClickListener {

                    }
                }
            }
            .show()

    }

    private fun doGetPayInfo(payType: Int) {
        http {
            url ="apiv4/cgdeposit"

            params["contract_id"] = contractId
            params["opt_id"] = currentDepositOption!!.id.toString()
            // 1微信支付2支付宝支付 如果差值小于0 才弹出支付方式选择
            params["pay_type"] = payType.toString()

            onSuccessWithMsg { res, msg ->
                rtv_submit_change_combination?.let {

                    val data = res.toPOJO<ChangeDepositCombinationPayInfoBean>().data

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

    private fun doGetPayInfos(payType: Int) {
        http {
            url ="apiv4/cgdeposit"

            params["contract_id"] = contractId
            params["opt_id"] = currentDepositOption!!.id.toString()
            // 1微信支付2支付宝支付 如果差值小于0 才弹出支付方式选择
            params["pay_type"] = payType.toString()

            onSuccessWithMsg { res, msg ->
                rtv_submit_change_combination?.let {

                    val data = res.toPOJO<ChangeDepositCombinationPayInfoBean>().data

                    showReturnTipss(msg)
                }

            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }

    }



    private fun doPay(result: ChangeDepositCombinationPayInfoBean.Data, isPayByWechat: Boolean) {
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

                        dlgPayProgress?.dismiss()
                        dlgPaySuccessed?.show()
                    }

                    override fun onFail(msg: String?) {
                        dlgPayProgress?.dismiss()
                        dlgPayFailed?.show()
                    }
                })
        } else {

            BaseAlipay.tryPay(result.alipay.paystr) { resultInfo, resultStatus, isLocalSuccessed ->

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
        startWithPop(FgtDeposit.newInstance(deviceId,"0"))
    }
}