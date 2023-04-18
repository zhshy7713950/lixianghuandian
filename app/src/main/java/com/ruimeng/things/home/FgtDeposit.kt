package com.ruimeng.things.home

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.GetDepositBean
import com.ruimeng.things.home.bean.GetPayByDepositBean
import com.ruimeng.things.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_deposit.*
import org.json.JSONObject
import wongxd.Config
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.*
import wongxd.common.recycleview.yaksa.linear
import wongxd.http

/**api
 * Created by wongxd on 2018/11/13.
 * 账户押金
 */
class FgtDeposit : BaseBackFragment() {

    companion object {

        enum class PayWay(des: String) {
            NULL(""), //不选择任何方式
            WX(""), //微信
            AL(""), //alipay
            ZM(""), //芝麻信用
            BT(""),//白条
            FQ(""),//分期
            XX(""), //线下
            GROUPPAP(""), //集团支付
        }

        fun newInstance(deviceId: String, isHost: String): FgtDeposit {
            return FgtDeposit().apply {
                arguments = Bundle().apply {
                    putString("deviceId", deviceId)
                    putString("isHost", isHost)
                }
            }
        }
    }

    val deviceId: String by lazy { arguments?.getString("deviceId") ?: "" }
    val getIsHost: String by lazy { arguments?.getString("isHost") ?: "" }

    override fun getLayoutRes(): Int = R.layout.fgt_deposit


    private var IS_CHECKED_PROTOCOL = false


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "账户押金")

        WaitViewController.from(scroll_account_deposit) { renderChilds() }

        Log.i("data===", "===getIsHost===$getIsHost")

        getDeposit()

    }


    private val currentDepositOption: MutableList<GetDepositBean.Data.DepositOption> =
        mutableListOf()

    @SuppressLint("SetTextI18n")
    private fun initAfterData(data: GetDepositBean.Data) {

        WaitViewController.from(scroll_account_deposit) { removeChilds() }

        tv_battery_num_account_deposit.text = "当前电池编号：" + data.device.device_id

        tv_battery_model_account_deposit.text = "当前电池型号：" + data.device.device_model
//        btn_yes_account_deposit.setOnClickListener {
//            tv_money_account_deposit.text =
//                batteryCombinationBean?.deposit_host
//                    ?: batteryPackageBean?.deposit_host
//                            ?: data.deposit_host
//            dealBtnYesAndNo(true)
//        }

//        btn_no_account_deposit.setOnClickListener {
//            tv_money_account_deposit.text =
//                batteryCombinationBean?.deposit
//                    ?: batteryPackageBean?.deposit
//                            ?: data.deposit
//            dealBtnYesAndNo(false)
//        }

        dealBtnYesAndNo(FgtHome.IsWholeBikeRent)

        ll_wechat_account_deposit.setOnClickListener {
            dealPayWay(PayWay.WX)
        }

        ll_alipay_account_deposit.setOnClickListener {
            dealPayWay(PayWay.AL)
        }


        ll_zhima_account_deposit.setOnClickListener {
            dealPayWay(PayWay.ZM)
        }


        ll_credit_account_deposit.setOnClickListener {
            dealPayWay(PayWay.BT)
        }

        groupPayLayout?.setOnClickListener { dealPayWay(PayWay.GROUPPAP) }

        ll_cash_account_deposit.setOnClickListener { dealPayWay(PayWay.XX) }

        btn_pay_now_account_deposit.setOnClickListener {
            if (!IS_CHECKED_PROTOCOL) {
                EasyToast.DEFAULT.show("请先同意协议")
                return@setOnClickListener
            }

            if (PAY_WAY_TAG == PayWay.NULL) {
                EasyToast.DEFAULT.show("请选择支付方式")
                return@setOnClickListener
            }

            showProgressDialog("请求支付信息中")

            getPayByDeposit()
        }


        iv_check.setOnClickListener {
            if (IS_CHECKED_PROTOCOL) {
                iv_check.setImageResource(R.drawable.uncheck)
            } else {
                iv_check.setImageResource(R.drawable.check)
            }

            IS_CHECKED_PROTOCOL = !IS_CHECKED_PROTOCOL
        }

        tv_view_rant_protocol_account_ya.setOnClickListener {
            val dlg = DialogFragmentRentProtocol()
            dlg.show(childFragmentManager, "protocol")
        }






        ll_wechat_account_deposit.visibility = View.GONE
        ll_alipay_account_deposit.visibility = View.GONE
        ll_zhima_account_deposit.visibility = View.GONE
        groupPayLayout?.visibility = View.GONE
        ll_cash_account_deposit.visibility = View.GONE
        ll_credit_account_deposit.visibility = View.GONE

        data.pay_type.forEach { payType ->
            // channel string 支付渠道， wx=微信 alipay=支付宝 cash=现金 credit=信用支付 pre_alipay = 芝麻信用（免押） 102=grouppay=集团支付
            // is_show int  0不显示 1显示
            val isShow = if (payType.is_show == 1) View.VISIBLE else View.GONE
            val channel = payType.channel

            when (channel) {
                "wx" -> ll_wechat_account_deposit.visibility = isShow
                "alipay" -> ll_alipay_account_deposit.visibility = isShow
                "pre_alipay" -> ll_zhima_account_deposit.visibility = isShow
                "cash" -> ll_cash_account_deposit.visibility = isShow
                "credit" -> ll_credit_account_deposit.visibility = isShow
                "grouppay" -> groupPayLayout.visibility = isShow
                else -> {

                }
            }

        }




        fun renderPackage() {
            //套餐
            ll_package_select_deposit.setOnClickListener {
                rv_select_package_deposit.visibility =
                    if (rv_select_package_deposit.visibility == View.VISIBLE) {
                        iv_enter_select_package_deposit.apply {
                            rotation = 0f
                        }
                        View.GONE
                    } else {
                        iv_enter_select_package_deposit.apply {
                            rotation = 90f
                        }
                        View.VISIBLE
                    }
            }


            fun checkPackage(pos: Int, packageBean: GetDepositBean.Data.Package) {

                val lastPos = data.packageList.indexOf(batteryPackageBean)

                if (lastPos == pos) return


                batteryPackageBean = packageBean
                batteryPackageBean?.let {
                    tv_select_package_deposit.text = it.name
                    if (pos == 0) {
                        it.deposit = data.deposit
                        it.deposit_host = data.deposit_host
                    }


                    tv_money_account_deposit.text =
                        if (FgtHome.IsWholeBikeRent) it.deposit_host else it.deposit

                    currentDepositOption.apply {
                        clear()
                        addAll(it.deposit_option)
                    }


                    if (currentDepositOption.isNotEmpty()) {
                        checkCombination(currentDepositOption[0])
                    }

                }
            }



            rv_select_package_deposit.linear {
                data.packageList.forEach { packageBean ->

                    itemDsl {
                        xml(R.layout.item_rv_select_package_deposit)

                        renderX { position, view ->

                            val tv = view.findViewById<TextView>(R.id.tv)

                            tv.text = packageBean.name

                            view.setOnClickListener {
                                checkPackage(position, packageBean)
                                ll_package_select_deposit.performClick()
                            }

                        }
                    }
                }
            }

            if (data.packageList.isNotEmpty())
                checkPackage(0, data.packageList[0])

            //套餐 end
        }





        renderPackage()

        if (currentDepositOption.isEmpty()) {
            //没有选择套餐
            currentDepositOption.apply {
                clear()
                addAll(data.deposit_option)
            }
        }

        if (currentDepositOption.isNotEmpty()) {
            checkCombination(currentDepositOption[0])
        }

        ll_combination_deposit.setOnClickListener {
            showCombinationPicker()
        }


        iv_check.performClick()
        ll_wechat_account_deposit.performClick()
        dealPayWay(PayWay.NULL)
    }


    private fun checkCombination(combinationBean: GetDepositBean.Data.DepositOption) {

        batteryCombinationBean = combinationBean
        batteryCombinationBean?.let {
            tv_combination_deposit?.text = it.name
            tv_money_account_deposit?.text =
                if (FgtHome.IsWholeBikeRent) it.deposit_host else it.deposit

        }
    }

    /**
     * 组合方式选择框
     */
    private fun showCombinationPicker() {

        if (currentDepositOption.isEmpty()) return

        val lastPos = if (batteryCombinationBean == null) {
            0
        } else {
            currentDepositOption.indexOf(batteryCombinationBean!!)
        }

        //条件选择器
        val pvOptions = OptionsPickerBuilder(activity,
            OnOptionsSelectListener { options1, option2, options3, v ->
                //返回的分别是三个级别的选中位置
                val item = currentDepositOption[options1]
                checkCombination(item)
            })
            .setTitleText("选择组合方式")
            .setSelectOptions(if (lastPos == -1) 0 else lastPos)
            .build<GetDepositBean.Data.DepositOption>()
        pvOptions?.setPicker(currentDepositOption)
        pvOptions?.show()

    }


    private fun dealBtnYesAndNo(isYes: Boolean) {
        FgtHome.IsWholeBikeRent = isYes

        fun setBtnStatus(isCheck: Boolean, btn: QMUIRoundButton) {

            val appColor = activity?.resources?.getColor(R.color.app_color)!!
            val btnDrawable: QMUIRoundButtonDrawable = btn.background as QMUIRoundButtonDrawable

            if (isCheck) {
                btnDrawable.setStrokeData(1, ColorStateList.valueOf(appColor))
                btnDrawable.setBgData(ColorStateList.valueOf(appColor))
                btn.setTextColor(Color.WHITE)
            } else {
                btnDrawable.setStrokeData(1, ColorStateList.valueOf(Color.WHITE))
                btnDrawable.setBgData(ColorStateList.valueOf(Color.WHITE))
                btn.setTextColor(appColor)
            }
        }

        setBtnStatus(isYes, btn_yes_account_deposit)
        setBtnStatus(!isYes, btn_no_account_deposit)

    }

    private var PAY_WAY_TAG = PayWay.NULL

    private fun dealPayWay(tag: PayWay) {

        PAY_WAY_TAG = tag

        fun isViewShow(v: View, isShow: Boolean) {
            if (isShow) {
                v.visibility = View.VISIBLE
            } else {
                v.visibility = View.GONE
            }
        }

        isViewShow(iv_check_wechat_account_deposit, false)
        isViewShow(iv_check_alipay_account_deposit, false)
        isViewShow(iv_check_zhima_account_deposit, false)
        isViewShow(iv_check_cash_account_deposit, false)
        isViewShow(iv_check_credit_account_deposit, false)
        isViewShow(groupPayImage, false)

        when (tag) {

            PayWay.NULL -> {
            }

            PayWay.WX -> {
                isViewShow(iv_check_wechat_account_deposit, true)
            }

            PayWay.AL -> {
                isViewShow(iv_check_alipay_account_deposit, true)
            }

            PayWay.ZM -> {
                isViewShow(iv_check_zhima_account_deposit, true)
            }

            PayWay.XX -> {
                isViewShow(iv_check_cash_account_deposit, true)
            }

            PayWay.BT -> {
                isViewShow(iv_check_credit_account_deposit, true)
            }
            PayWay.GROUPPAP -> {
                isViewShow(groupPayImage, true)
            }
            else -> {

            }
        }
    }


    /**
     * 获取押金信息
     */
    private fun getDeposit() {

        http {
            url = "apiv4/getdeposit"
            params["device_id"] = deviceId
            params["is_host"] = getIsHost
            params["cg_mode"] = Config.getDefault().spUtils.getString("cg_mode", "0")
            onSuccess { res ->

                scroll_account_deposit?.let {
                    val result = res.toPOJO<GetDepositBean>().data
                    initAfterData(result)
                }
            }

            onFail { i, s ->
                if (i == 3001) {
                    //如果errcode=3001 直接跳转到 押金支付界面
                    startWithPop(FgtPayRentMoney.newInstance(deviceId))
                }
            }
        }
    }


    private var dlgPayProgress: SweetAlertDialog? = null

    private var dlgPaySuccessed: SweetAlertDialog? = null

    private var dlgPayFailed: SweetAlertDialog? = null

    private var batteryPackageBean: GetDepositBean.Data.Package? = null

    private var batteryCombinationBean: GetDepositBean.Data.DepositOption? = null

    private fun getPayByDeposit() {

        val agentCode = et_agnet_name_deposit.text.toString()

        if (agentCode.isBlank()) {
            dissmissProgressDialog()
            EasyToast.DEFAULT.show("请输入经销商代码")
            return
        }


        http {
            url = "apiv4/getpaybydeposit"
            params["device_id"] = deviceId
            params["code"] = agentCode
            params["cg_mode"] = Config.getDefault().spUtils.getString("cg_mode", "0")
            params["host"] = if (FgtHome.IsWholeBikeRent) "1" else "2"
            //1微信支付2支付宝支付99线下支付 芝麻信用 101
            params["pay_type"] = when (PAY_WAY_TAG) {
                PayWay.WX -> "1"
                PayWay.AL -> "2"
                PayWay.ZM -> "101"
                PayWay.BT -> "3"
                PayWay.GROUPPAP -> "102"
                else -> "99"
            }
            params["package_id"] = batteryPackageBean?.id ?: ""
            params["opt_id"] = batteryCombinationBean?.id?.toString() ?: ""

            onSuccessWithMsg { res, msg ->
                val result = res.toPOJO<GetPayByDepositBean>().data


                dlgPayProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
                dlgPaySuccessed = getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") {
                    //押金支付成功  跳转租金支付界面
                    startWithPop(FgtPayRentMoney.newInstance(deviceId))
                }
                dlgPayFailed = getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")

                dlgPayProgress?.show()


                if (PAY_WAY_TAG == PayWay.WX) {
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

                    WXEntryActivity.wxPay(activity, entity, object : WXEntryActivity.WxCallback {
                        override fun onsuccess(code: String?, msg: String?) {
                            getServerPayResult(result.orderid, true)
                        }

                        override fun onFail(msg: String?) {
                            getServerPayResult(result.orderid, false)
                        }
                    })
                } else if (PAY_WAY_TAG == PayWay.AL || PAY_WAY_TAG == PayWay.ZM) {

                    BaseAlipay.tryPay(result.alipay.paystr) { resultInfo, resultStatus, isLocalSuccessed ->
                        getServerPayResult(result.orderid, isLocalSuccessed)
                    }

                } else if (PAY_WAY_TAG == PayWay.BT) {
                    dlgPayProgress?.dismiss()
                    dlgPaySuccessed?.show()
                } else if (PAY_WAY_TAG == PayWay.GROUPPAP) {
                    dlgPayProgress?.dismiss()
                    dlgPaySuccessed?.show()
                } else {
                    dlgPayProgress?.dismiss()
                    dlgPaySuccessed?.show()
                }


                btn_pay_now_account_deposit.postDelayed({
                    if (dlgPayProgress?.isShowing ?: false) {
                        dlgPayProgress?.dismissWithAnimation()
                    }
                }, 10 * 1000)

            }

            onFinish {
                dissmissProgressDialog()
            }
        }
    }


    private var retryTime = 0

    /**
     * 获取服务器上的支付结果
     */
    private fun getServerPayResult(orderId: String, shouldRetry: Boolean) {

        fun dealShouldRetry() {
            if (retryTime <= 3 && shouldRetry) {
                btn_pay_now_account_deposit?.postDelayed({
                    getServerPayResult(orderId, shouldRetry)
                }, 2000)
            } else {
                dlgPayProgress?.dismiss()
                dlgPayFailed?.show()
            }
        }

        retryTime++


        http {
            IS_SHOW_MSG = false
            url = Path.ORDERSTATUS

            params["orderid"] = orderId

            onSuccess {
                retryTime = 0

                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val order_status = data.optInt("order_status")
                //order_status itn 0待支付1支付失败 99支付成功 100已退款  客户端判断errcode=200,并且order_status等于99即可跳入下一步
                when (order_status) {
                    99 -> {
                        dlgPayProgress?.dismiss()
                        dlgPaySuccessed?.show()
                    }
                    0 -> {
                        dealShouldRetry()
                    }
                    else -> {
                        dlgPayProgress?.dismiss()
                        dlgPayFailed?.show()
                    }
                }
            }

            onFail { i, s ->
                dealShouldRetry()
            }
        }
    }
}