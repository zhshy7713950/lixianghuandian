package com.ruimeng.things.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.entity.local.AdPayLocal
import com.entity.remote.OperationInnerData
import com.net.whenError
import com.net.whenSuccess
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.R
import com.ruimeng.things.home.adapter.CouponAdapter
import com.ruimeng.things.home.vm.CouponPurchaseViewModel
import com.utils.unsafeLazy
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_coupon_purchase.btnPayNow
import kotlinx.android.synthetic.main.fgt_coupon_purchase.rbWx
import kotlinx.android.synthetic.main.fgt_coupon_purchase.rvCoupons
import kotlinx.android.synthetic.main.fgt_coupon_purchase.tvCouponPrice
import kotlinx.android.synthetic.main.fgt_coupon_purchase.tvTotalPrice
import kotlinx.coroutines.launch
import wongxd.alipay.BaseAlipay
import wongxd.base.BaseBackFragment
import wongxd.common.getSweetDialog

class FgtCouponPurchase : BaseBackFragment() {
    companion object {
        fun newInstance(operationInnerDataList: List<OperationInnerData>?): FgtCouponPurchase {
            val fgtCouponPurchase = FgtCouponPurchase()
            fgtCouponPurchase.arguments = Bundle().apply {
                putParcelableArrayList("operationInnerData", ArrayList(operationInnerDataList))
            }
            return fgtCouponPurchase
        }
    }

    private val vm: CouponPurchaseViewModel by viewModels()
    override fun getLayoutRes(): Int = R.layout.fgt_coupon_purchase
    private val couponAdapter by unsafeLazy {
        CouponAdapter()
    }
    private val operationInnerDataList by unsafeLazy {
        arguments?.getParcelableArrayList<OperationInnerData>("operationInnerData")
    }
    private val dlgPayProgress: SweetAlertDialog by unsafeLazy {
        getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "支付中")
    }
    private val dlgPaySuccess: SweetAlertDialog by unsafeLazy {
        getSweetDialog(SweetAlertDialog.SUCCESS_TYPE, "支付成功") { paySuccess() }
    }
    private val dlgPayFailed: SweetAlertDialog by unsafeLazy {
        getSweetDialog(SweetAlertDialog.ERROR_TYPE, "支付失败")
    }

    private fun paySuccess() {
        pop()
    }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initEvent()
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

    private fun initEvent() {
        lifecycleScope.launchWhenCreated {
            launch {
                vm.serverPayResultLiveData.observeForever {
                    Log.d("LXHDNet", "支付结果===> ${it.isSuccess}")
                    dlgPayProgress.dismiss()
                    if (it.isSuccess) {
                        dlgPaySuccess.show()
                    } else {
                        dlgPayFailed.show()
                    }
                }
            }
        }
    }

    private fun payNow(operationInnerData: OperationInnerData) {
        dlgPayProgress.show()
        val payType = if (rbWx.isChecked) {
            "1"
        } else {
            "2"
        }
        vm.adPay(
            AdPayLocal(
                FgtHome.userId,
                operationInnerData.id ?: "",
                payType,
                operationInnerData.price ?: ""
            )
        ).observeForever { netRes ->
            netRes.whenSuccess {
                when (payType) {
                    "1" -> {
                        val entity = WXEntryActivity.WxPayEntity()
                        it.data.wxpay.let {
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
                                    getServerPayResult(it.data.orderid, true)
                                }

                                override fun onFail(msg: String?) {
                                    getServerPayResult(it.data.orderid, false)
                                }
                            })
                    }

                    "2" -> {
                        BaseAlipay.tryPay(it.data.alipay.paystr) { _, _, isLocalSuccess ->
                            getServerPayResult(it.data.orderid, isLocalSuccess)
                        }
                    }
                }
            }.whenError { _, msg ->
                dlgPayProgress.dismiss()
                dlgPayFailed.apply {
                    this.contentText = msg
                    show()
                }
            }
        }
    }

    private fun getServerPayResult(orderId: String, localSuccess: Boolean) {
        vm.pollServerPayResult(orderId, localSuccess)
    }

    private fun updatePrice(op: OperationInnerData?) {
        op?.let {
            tvTotalPrice.text = it.price
            tvCouponPrice.text = "已优惠¥${it.discount}"
        }
    }

}