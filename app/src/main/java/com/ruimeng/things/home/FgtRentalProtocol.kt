package com.ruimeng.things.home

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.UserInfoLiveData
import kotlinx.android.synthetic.main.fgt_rental_protocol.*
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.http

/**
 * 租赁协议
 */
class FgtRentalProtocol : BaseBackFragment() {

    companion object {
        fun newInstance(deviceId: String): FgtRentalProtocol {
            return FgtRentalProtocol().apply {
                arguments = Bundle().apply {
                    putString("deviceId", deviceId)
                }
            }
        }
    }

    private val deviceId: String by lazy { arguments?.getString("deviceId") ?: "" }

    override fun getLayoutRes(): Int = R.layout.fgt_rental_protocol

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "租赁协议")

        initWebView()
        getProtocolUrl()
    }

    private fun initWebView() {
        val settings = webview.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        webview.webViewClient = WebViewClient()
        webview.webChromeClient = WebChromeClient()
    }

    private fun getProtocolUrl() {
        val userId = UserInfoLiveData.getFromString().id
        if (userId.isBlank()) {
            return
        }

        http {
            url = Path.GET_PROTOCOL_V6
            params["userId"] = userId
            params["deviceId"] = deviceId

            onSuccess {
                try {
                    val jsonObject = JSONObject(it)
                    val dataUrl = jsonObject.optString("data")
                    if (dataUrl.isNotBlank()) {
                        webview.loadUrl(dataUrl)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
