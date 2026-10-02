package com.ruimeng.things.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome.Companion.REQUEST_ZXING_CODE
import com.ruimeng.things.home.helper.ScanResultCheck
import com.utils.ToastHelper
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import wongxd.base.AtyBase

class AtyScanQrcode : AtyBase() {
    companion object {
        const val TYPE_CHANGE = "change"
        const val TYPE_PAY_RENT = "type_pay_rent"
        const val TYPE_TICKET = "type_ticket"
        const val RESULT_PREFIX = "resultPrefix"
        const val RESULT_OLD_CONTRACT_ID = "resultOldContractId"
        const val IS_HOST = "isHost"

        fun start(aty: Activity, resultPrefix: String = "", oldContractId: String = "", isHost: String = "") {
            aty.startActivityForResult(Intent(aty, AtyScanQrcode::class.java).apply {
                putExtra(RESULT_PREFIX, resultPrefix)
                putExtra(RESULT_OLD_CONTRACT_ID, oldContractId)
                putExtra(IS_HOST, isHost)
            }, REQUEST_ZXING_CODE)
        }
    }

    private val resultPrefix by lazy { intent.getStringExtra(RESULT_PREFIX) ?: "" }
    private val resultOldContractId by lazy { intent.getStringExtra(RESULT_OLD_CONTRACT_ID) ?: "" }
    private val isHost by lazy { intent.getStringExtra(IS_HOST) ?: "" }
    private var returning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_scan_page)
        initTopbar(findViewById(R.id.topbar), "扫一扫")
        val fragment = ScrollingCaptureFragment()
        CodeUtils.setFragmentArgs(fragment, R.layout.layout_scan_qrcode_camera)
        fragment.analyzeCallback = object : CodeUtils.AnalyzeCallback {
            override fun onAnalyzeSuccess(mBitmap: Bitmap, result: String) = checkScanResult(result)
            override fun onAnalyzeFailed() { ToastHelper.shortToast(this@AtyScanQrcode, "二维码错误") }
        }
        supportFragmentManager.beginTransaction().replace(R.id.fl_my_container, fragment).commit()
        ScanPageUi.bind(this, ScanPageMode.fromLegacyType("", resultPrefix))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScanPageUi.REQUEST_INPUT -> if (resultCode == 1001) checkScanResult(data?.getStringExtra("code") ?: "")
            ScanPageUi.REQUEST_STATION -> if (resultCode == Activity.RESULT_OK) checkScanResult(data?.getStringExtra("code") ?: "")
            ScanPageUi.REQUEST_ALBUM -> if (resultCode == Activity.RESULT_OK) lifecycleScope.launch {
                val code = withContext(Dispatchers.Default) { ScanPageUi.readAlbumCode(this@AtyScanQrcode, data) }
                if (code.isNullOrBlank()) ToastHelper.shortToast(this@AtyScanQrcode, "识别失败，请稍后重试")
                else checkScanResult(code)
            }
        }
    }

    private fun checkScanResult(result: String) {
        if (result.isBlank() || returning) return
        ScanResultCheck().checkResult(3, result, object : ScanResultCheck.CheckResultListener {
            override fun checkStatus(pass: Boolean) {
                if (!pass || returning) return
                returning = true
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS)
                    putExtra(CodeUtils.RESULT_STRING, result)
                    putExtra(RESULT_PREFIX, resultPrefix)
                    putExtra(RESULT_OLD_CONTRACT_ID, resultOldContractId)
                    putExtra(IS_HOST, isHost)
                })
                finish()
            }
        })
    }
}
