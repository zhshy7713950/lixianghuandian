package com.ruimeng.things.home

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.ruimeng.things.Path
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.ConfirmInstallmentBean
import com.ruimeng.things.home.bean.RentInstallmentPaymentBean
import com.ruimeng.things.showTipDialog
import kotlinx.android.synthetic.main.fgt_rent_installment_payment.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import wongxd.AtyWeb
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.MainLooper
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.SystemUtils

/**
 * 租金分期支付
 * Created by wongxd on 2020/1/7.
 */
class FgtRentInstallmentPayment : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_rent_installment_payment


    companion object {
        fun newInstance(contractId: String, orderId: String): FgtRentInstallmentPayment {
            return FgtRentInstallmentPayment().apply {
                arguments = Bundle().apply {
                    putString("contractId", contractId)
                    putString("orderId", orderId)
                }
            }
        }
    }


    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }
    private val orderId: String by lazy { arguments?.getString("orderId") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "请选择分期支付方式")

        getInfo()
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        if (doStatusCheck) {
            sureOrderIsPay()
        }
    }


    private fun getInfo() {

        http {
            url = PathV3.INSTALMENT_INFO
            params["contract_id"] = contractId
            params["order_id"] = orderId

            onSuccess { res ->

                ll_alipay_rent_installment_payment?.let {
                    val data = res.toPOJO<RentInstallmentPaymentBean>().data

                    ll_alipay_rent_installment_payment.apply {
                        visibility =
                            if (data.alipay_show == 1) View.VISIBLE else View.GONE

                        setOnClickListener {
                            confirmInstalment("alipay")
                        }

                    }


                    ll_bank_rent_installment_payment.apply {
                        visibility =
                            if (data.bank_show == 1) View.VISIBLE else View.GONE

                        setOnClickListener {
                            confirmInstalment("bank")
                        }
                    }



                    showLayer(data)

                }

            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }


    private fun showLayer(data: RentInstallmentPaymentBean.Data) {

        if (null == activity) return

        AnyLayer.with(activity!!)
            .contentView(R.layout.layout_rent_installment_payment)
            .backgroundColorInt(Color.parseColor("#85000000"))
            .cancelableOnTouchOutside(false)
            .cancelableOnClickKeyBack(false)
            .bindData { anyLayer ->

                anyLayer.contentView.findViewById<WebView>(R.id.web).apply {
                    val webSettings = getSettings()
                    webSettings.setPluginState(WebSettings.PluginState.ON)
                    webSettings.setLightTouchEnabled(true)
                    webSettings.setJavaScriptCanOpenWindowsAutomatically(true)
                    webSettings.setLoadWithOverviewMode(true)
                    webSettings.setJavaScriptEnabled(true)
                    webSettings.setCacheMode(WebSettings.LOAD_DEFAULT)
                    webSettings.setDomStorageEnabled(true)
                    webSettings.setDatabaseEnabled(true)
                    webSettings.setAppCacheEnabled(true)

                    webSettings.setAllowFileAccess(true)
                    webSettings.setSavePassword(true)
                    webSettings.setSupportZoom(true)
                    webSettings.setBuiltInZoomControls(true)
                    /**
                     * 用WebView显示图片，可使用这个参数 设置网页布局类型：
                     * 1、LayoutAlgorithm.NARROW_COLUMNS ：适应内容大小
                     * 2、LayoutAlgorithm.SINGLE_COLUMN : 适应屏幕，内容将自动缩放
                     */
                    webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS)
                    webSettings.setUseWideViewPort(true)

                    setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY)
                    setHorizontalScrollbarOverlay(true)
                    setHorizontalScrollBarEnabled(true)
                    setVerticalScrollBarEnabled(true)
                    requestFocus()

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            url: String?
                        ): Boolean {

                            view?.loadUrl(url)
                            return true
//                            return super.shouldOverrideUrlLoading(view, url)
                        }

                        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return shouldOverrideUrlLoading(view, request?.url?.toString())
                        }
                    }

                    loadUrl(data.url)
                }

                anyLayer.contentView.findViewById<QMUIRoundButton>(R.id.btn).apply {
                    val btn = this

                    object : CountDownTimer((data.wait_sec * 1000).toLong(), 1000.toLong()) {
                        override fun onTick(millisUntilFinished: Long) {
                            MainLooper.runOnUiThread {
                                btn.text =
                                    "同意(${millisUntilFinished / 1000}s)"
                                btn.setOnClickListener {}
                            }
                        }

                        override fun onFinish() {

                            MainLooper.runOnUiThread {

                                btn.text = "同意"
                                btn.setOnClickListener {
                                    anyLayer.dismiss()
                                }
                            }
                        }
                    }.start()

                }


            }
            .show()


    }


    private fun confirmInstalment(channel: String) {

        http {
            url = PathV3.CONFIRM_INSTALMENT
            params["contract_id"] = contractId
            params["channel"] = channel
            params["order_id"] = orderId


            onSuccessWithMsg { res, msg ->

                ll_alipay_rent_installment_payment?.let {

                    val data = res.toPOJO<ConfirmInstallmentBean>().data
                    if (data.target == "webview") {
                        AtyWeb.start("分期支付", data.url)
                    } else {
                        SystemUtils.openUrlByBrowser(activity, data.url)
                    }

                    doStatusCheck = true

                }
            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }


    private var doStatusCheck = false

    private fun sureOrderIsPay() {

        http {

            url = Path.ORDERSTATUS

            params["orderid"] = orderId

            onSuccessWithMsg { res, msg ->
                ll_alipay_rent_installment_payment?.let {

                    val json = JSONObject(res)
                    val data = json.optJSONObject("data")
                    val order_status = data.optInt("order_status")
                    //order_status itn 0待支付1支付失败 99支付成功 100已退款  客户端判断errcode=200,并且order_status等于99即可跳入下一步
                    if (order_status == 99) {
                        showTipDialog(activity, msg = "支付成功") {
                            pop()
                            notifyPopFgtPayRentMoney()
                        }
                    } else if (order_status == 0) {
                        showTipDialog(activity, msg = "待支付") {
                            showTipDialog(activity, msg = "待支付")
                        }
                    } else {
                        showTipDialog(activity, msg = "支付失败")
                    }


                }
            }


            onFail { cdde, msg ->
                ll_alipay_rent_installment_payment?.let {
                    showTipDialog(activity, msg = msg)
                }
            }
        }

    }


    private fun notifyPopFgtPayRentMoney() {
        EventBus.getDefault().post(FgtPayRentMoney.EventInstallmentPaymentSuccess(true))
    }
}