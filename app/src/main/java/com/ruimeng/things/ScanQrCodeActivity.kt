package com.ruimeng.things

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.ruimeng.things.home.ScanPageMode
import com.ruimeng.things.home.ScanPageUi
import com.ruimeng.things.home.ScrollingCaptureFragment
import com.ruimeng.things.home.helper.ScanResultCheck
import com.utils.ToastHelper
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wongxd.base.AtyBase

class ScanQrCodeActivity : AtyBase() {
    private val scanType by lazy { intent.getStringExtra("type") ?: "" }
    private val contractId by lazy { intent.getStringExtra("contract_id") ?: "" }
    private var returning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_scan_page)
        initTopbar(findViewById(R.id.topbar), "扫一扫")
        val fragment = ScrollingCaptureFragment()
        CodeUtils.setFragmentArgs(fragment, R.layout.layout_scan_qrcode_camera)
        fragment.analyzeCallback = object : CodeUtils.AnalyzeCallback {
            override fun onAnalyzeSuccess(mBitmap: Bitmap, result: String) = checkScanResult(result)
            override fun onAnalyzeFailed() { ToastHelper.shortToast(this@ScanQrCodeActivity, "无法识别二维码") }
        }
        supportFragmentManager.beginTransaction().replace(R.id.fl_my_container, fragment).commit()
        ScanPageUi.bind(this, ScanPageMode.fromLegacyType(scanType, ""))
    }

    private fun getTypeCode(): Int = when (scanType) {
        "换电开门" -> 1
        "换电" -> 2
        "租电" -> 4
        "退还" -> 5
        "冻结" -> 6
        "自助开仓" -> 7
        "推荐官" -> 8
        else -> 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScanPageUi.REQUEST_INPUT -> if (resultCode == 1001) checkScanResult(data?.getStringExtra("code") ?: "")
            ScanPageUi.REQUEST_STATION -> if (resultCode == Activity.RESULT_OK) checkScanResult(data?.getStringExtra("code") ?: "")
            ScanPageUi.REQUEST_ALBUM -> if (resultCode == Activity.RESULT_OK) lifecycleScope.launch {
                val code = withContext(Dispatchers.Default) { ScanPageUi.readAlbumCode(this@ScanQrCodeActivity, data) }
                if (code.isNullOrBlank()) ToastHelper.shortToast(this@ScanQrCodeActivity, "识别失败，请稍后重试")
                else checkScanResult(code)
            }
        }
    }

    private fun checkScanResult(result: String) {
        if (result.isBlank() || returning) return
        ScanResultCheck().checkResult(getTypeCode(), result, object : ScanResultCheck.CheckResultListener {
            override fun checkStatus(pass: Boolean) {
                if (!pass || returning) return
                returning = true
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS)
                    putExtra(CodeUtils.RESULT_STRING, result)
                    putExtra("type", scanType)
                    putExtra("contract_id", contractId)
                })
                finish()
            }
        })
    }
}
