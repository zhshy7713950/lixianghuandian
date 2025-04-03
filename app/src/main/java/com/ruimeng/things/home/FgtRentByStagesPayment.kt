package com.ruimeng.things.home

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ruimeng.things.R
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.fgt_rent_by_stages_payment.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp
import wongxd.base.BaseBackFragment
import wongxd.common.toPOJO
import wongxd.http

class FgtRentByStagesPayment : BaseBackFragment() {

    companion object {
        fun newInstance(contractId: String, totalAmount: Double, periodAmount: Double, period: Int): FgtRentByStagesPayment {
            val fragment = FgtRentByStagesPayment()
            val args = Bundle()
            args.putString("contractId", contractId)
            args.putDouble("totalAmount", totalAmount)
            args.putDouble("periodAmount", periodAmount)
            args.putInt("period", period)
            fragment.arguments = args
            return fragment
        }
    }

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }
    private val totalAmount: Double by lazy { arguments?.getDouble("totalAmount") ?: 0.0 }
    private val periodAmount: Double by lazy { arguments?.getDouble("periodAmount") ?: 0.0 }
    private val period: Int by lazy { arguments?.getInt("period") ?: 3 }

    override fun getLayoutRes(): Int = R.layout.fgt_rent_by_stages_payment

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "分期免息")
        initView()
        initWebView()
    }

    private fun initView() {
        // 设置支付金额和分期信息
        tv_total_amount.text = "¥ $totalAmount"
        tv_by_stages.text = "￥${periodAmount} x ${period}期"

        // 设置协议标题样式
        tvAgreementTitle.apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dip(12), dip(12), 0, 0)
        }

        btnConfirm.setOnClickListener {
            confirmInstalment()
        }
    }

    private fun initWebView() {
        webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadUrl("http://xianglilai.scxll.cn//llgou/index.html")
        }
    }

    private fun confirmInstalment() {
        http {
            url = "/apiv6/llgpay/confirminstalment"
            params["contract_id"] = contractId
            params["period"] = period.toString()
            params["channel"] = "bank"

            onSuccess { res ->
                val data = res.toPOJO<InstalmentConfirmBean>().data
                if (data.target.isNullOrEmpty() || data.url.isNullOrEmpty()) {
                    ToastHelper.shortToast(context, "分期免息信息获取失败，请稍后重试")
                } else {
                    // 跳转到WebView页面
                    pop()
                    start(FgtRentByStagesWebView.newInstance(data.url, data.target))
                }
            }

            onFail { _, msg ->
                ToastHelper.shortToast(context, msg)
            }
        }
    }

    data class InstalmentConfirmBean(
        val data: InstalmentConfirmData
    )

    data class InstalmentConfirmData(
        val target: String?,
        val url: String?
    )
} 