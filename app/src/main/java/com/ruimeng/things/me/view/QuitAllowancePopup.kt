package com.ruimeng.things.me.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.PopupWindow
import com.ruimeng.things.R
import com.ruimeng.things.home.webview.CustomWebView
import org.json.JSONObject

class QuitAllowancePopup(
    private val activity: Activity,
    private val grant: String,
    private val price: String,
    private val onClosed: () -> Unit
) : PopupWindow(activity) {

    private val webView: CustomWebView

    init {
        contentView = View.inflate(activity, R.layout.popup_quit_allowance, null)
        webView = contentView.findViewById(R.id.webview)
        
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.addJavascriptInterface(AllowanceJSInterface(), "allowanceMessage")

        val url = "https://xianglilai.scxll.cn/appH5/QuitAllowance.html?grant=$grant&price=$price"
        webView.loadUrl(url)
    }

    fun show(anchor: View) {
        if (activity.window.decorView.windowToken != null) {
            val location = IntArray(2)
            anchor.getLocationOnScreen(location)
            val y = location[1] + anchor.height
            val displayMetrics = activity.resources.displayMetrics
            this.height = displayMetrics.heightPixels - y
            showAtLocation(anchor, Gravity.NO_GRAVITY, 0, y)
        }
    }

    override fun dismiss() {
        super.dismiss()
        try {
            webView.destroy()
            webView.cleanup()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class AllowanceJSInterface {
        @JavascriptInterface
        fun postMessage(action: String) {
            activity.runOnUiThread {
                if (action == "backAction") {
                    dismiss()
                    onClosed()
                }
            }
        }
    }
}