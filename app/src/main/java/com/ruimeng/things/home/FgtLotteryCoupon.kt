package com.ruimeng.things.home

import android.os.Bundle
import android.webkit.JavascriptInterface
import com.ruimeng.things.R
import com.ruimeng.things.home.webview.CustomWebView
import wongxd.base.BaseBackFragment
import com.utils.ToastHelper
import wongxd.http
import org.json.JSONObject

class FgtLotteryCoupon : BaseBackFragment() {

    private lateinit var webView: CustomWebView
    private var loadUrl: String = ""

    companion object {
        fun newInstance(grant: String, own: String, ownId: String, price: String, code: String): FgtLotteryCoupon {
            val fragment = FgtLotteryCoupon()
            val args = Bundle()
            // 暂时加载本地 H5 方便测试
//            val url = "file:///android_asset/LotteryCoupon.html?grant=$grant&own=$own&ownId=$ownId&price=$price&code=$code"
            // 原先的线上地址:
            val url = "http://xianglilai.scxll.cn/appH5/LotteryCoupon.html?grant=$grant&own=$own&ownId=$ownId&price=$price&code=$code"
            args.putString("url", url)
            args.putString("ownId", ownId)
            args.putString("grant", grant)
            args.putString("code", code)
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_lottery_coupon

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "幸运福利")
        loadUrl = arguments?.getString("url") ?: ""
        initWebView()
        loadWebContent()
    }

    private fun initWebView() {
        webView = rootView.findViewById(R.id.webview_lottery_coupon)
        // Add JavascriptInterface
        webView.addJavascriptInterface(LotteryJSInterface(), "lotteryMessage")
    }

    private fun loadWebContent() {
        if (loadUrl.isNotEmpty()) {
            webView.loadUrl(loadUrl)
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
            webView.cleanup()
        }
        super.onDestroy()
    }

    inner class LotteryJSInterface {
        @JavascriptInterface
        fun postMessage(action: String) {
            activity?.runOnUiThread {
                when (action) {
                    "spinAction" -> {
                        // Call sendaward API
                        val ownId = arguments?.getString("ownId") ?: ""
                        val grant = arguments?.getString("grant") ?: "0"
                        val code = arguments?.getString("code") ?: ""

                        http {
                            url = "/apiv6/luckywheel/sendaward"
                            params["selfCouponId"] = ownId
                            params["sendCouponPrice"] = grant
                            params["lotteryReqNum"] = code

                            IS_SHOW_MSG = false

                            onResponse { res ->
                                try {
                                    val jsonObj = JSONObject(res as String)
                                    val errcode = jsonObj.optInt("errcode")
                                    val errmsg = jsonObj.optString("errmsg", "发奖失败")
                                    if (errcode == 200) {
                                        // Call JS function
                                        webView.evaluateJavascript("javascript:displayWheel()", null)
                                    } else {
                                        ToastHelper.shortToast(context, errmsg)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    ToastHelper.shortToast(context, "响应解析失败")
                                }
                            }
                        }
                    }
                    "backAction" -> {
                        pop()
                    }
                }
            }
        }
    }
}