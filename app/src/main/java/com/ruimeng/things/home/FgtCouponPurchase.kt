package com.ruimeng.things.home

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.adapter.CouponAdapter
import com.ruimeng.things.home.bean.AdInfoBean
import com.ruimeng.things.home.bean.GetRentPayBean
import com.ruimeng.things.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_coupon_purchase.btnPayNow
import kotlinx.android.synthetic.main.fgt_coupon_purchase.rbWx
import kotlinx.android.synthetic.main.fgt_coupon_purchase.rvCoupons
import kotlinx.android.synthetic.main.fgt_coupon_purchase.tvCouponPrice
import kotlinx.android.synthetic.main.fgt_coupon_purchase.tvTotalPrice
import kotlinx.android.synthetic.main.fgt_deposit.btn_pay_now_account_deposit
import org.json.JSONObject
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.getSweetDialog
import wongxd.common.toPOJO
import wongxd.http

class FgtCouponPurchase : BaseBackFragment() {
    companion object {
        fun newInstance(operationInnerDataList: ArrayList<AdInfoBean.OperationInnerData>?): FgtCouponPurchase {
            val fgtCouponPurchase = FgtCouponPurchase()
            fgtCouponPurchase.arguments = Bundle().apply {
                putParcelableArrayList("operationInnerData", operationInnerDataList)
            }
            return fgtCouponPurchase
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_coupon_purchase
    private val couponAdapter by lazy {
        CouponAdapter()
    }
    private val operationInnerDataList by lazy {
        arguments?.getParcelableArrayList<AdInfoBean.OperationInnerData>("operationInnerData")
    }
    private val dlgPayProgress: SweetAlertDialog by lazy {
        getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
    }
    private val dlgPaySuccess: SweetAlertDialog by lazy {
        getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") { paySuccess() }
    }
    private val dlgPayFailed: SweetAlertDialog by lazy {
        getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")
    }

    private fun paySuccess() {
        pop()
    }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initViews()
    }

    private fun initViews() {
        initTopbar(topbar, "购买优惠券")
        with(rvCoupons) {
            layoutManager = LinearLayoutManager(context)
            adapter = couponAdapter.also {
                it.setNewData(operationInnerDataList)
                it.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, i ->
                    updatePrice(operationInnerDataList?.get((i)))
                    it.selectPos = i
                    it.notifyDataSetChanged()
                }
            }
        }
        btnPayNow.setOnClickListener {
            if (operationInnerDataList != null && operationInnerDataList!!.size > 0) {
                payNow(operationInnerDataList!![couponAdapter.selectPos])
            }
        }
        if (operationInnerDataList != null && operationInnerDataList!!.size > 0) {
            updatePrice(operationInnerDataList!![0])
        }
    }

    private fun payNow(operationInnerData: AdInfoBean.OperationInnerData) {
        dlgPayProgress.show()
        val payType = if (rbWx.isChecked) {
            "1"
        } else {
            "2"
        }
        http {
            url = Path.AD_PAY
            params["userId"] = InfoViewModel.getDefault().userInfo?.value?.id ?: ""
            params["packageId"] = operationInnerData.id ?: ""
            params["payType"] = payType
            params["price"] = operationInnerData.price ?: ""

            onSuccess {s->
                val result = s.toPOJO<GetRentPayBean>().data
                when (payType) {
                    "1" -> {
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
                                    getServerPayResult(result.orderid, true)
                                }

                                override fun onFail(msg: String?) {
                                    getServerPayResult(result.orderid, false)
                                }
                            })
                    }

                    "2" -> {
                        BaseAlipay.tryPay(result.alipay.paystr) { _, _, isLocalSuccess ->
                            getServerPayResult(result.orderid, isLocalSuccess)
                        }
                    }
                }
            }

            onFail { _, msg ->
                dlgPayProgress.dismiss()
                dlgPayFailed.apply {
                    this.contentText = msg
                    show()
                }
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
                        dlgPaySuccess?.show()
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

    private fun updatePrice(op: AdInfoBean.OperationInnerData?) {
        op?.let {
            tvTotalPrice.text = it.price
            tvCouponPrice.text = "已优惠¥${it.discount}"
        }
    }

}