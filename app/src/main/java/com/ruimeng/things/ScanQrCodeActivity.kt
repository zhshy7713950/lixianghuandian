package com.ruimeng.things

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.ruimeng.things.home.AtyInputCode
import com.ruimeng.things.home.AtyScanQrcode
import com.uuzuche.lib_zxing.activity.CaptureFragment
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.activity_scan_qr_code.*
import kotlinx.android.synthetic.main.aty_scan_qrcode.tv_light
import wongxd.base.AtyBase
import wongxd.base.custom.anylayer.AnyLayer


class ScanQrCodeActivity : AtyBase() {

    private var mActivity: AppCompatActivity? = null

    private var getType = ""
    private var getContractId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr_code)
        mActivity = this
        initTopbar(topbar, "换电扫码")
        if (!TextUtils.isEmpty(intent.getStringExtra("type"))) {
            getType = intent.getStringExtra("type")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("type"))) {
            getType = intent.getStringExtra("type")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("contract_id"))) {
            getContractId = intent.getStringExtra("contract_id")
        }
        /**
         * 执行扫面Fragment的初始化操作
         */
        val captureFragment = CaptureFragment()
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.layout_scan_qrcode_camera)
        //二维码解析回调函数
        captureFragment.analyzeCallback = object : CodeUtils.AnalyzeCallback {
            override fun onAnalyzeSuccess(mBitmap: Bitmap, result: String) {
                val resultIntent = Intent()
                val bundle = Bundle()
                bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS)
                bundle.putString(CodeUtils.RESULT_STRING, result)
                bundle.putString("type", getType)
                bundle.putString("contract_id", getContractId)
                resultIntent.putExtras(bundle)
                setResult(Activity.RESULT_OK, resultIntent)
               finish()
            }

            override fun onAnalyzeFailed() {
                val resultIntent = Intent()
                val bundle = Bundle()
                bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED)
                bundle.putString(CodeUtils.RESULT_STRING, "")
                bundle.putString("type", "")
                bundle.putString("contract_id", "")
                resultIntent.putExtras(bundle)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
        /**
         * 替换我们的扫描控件
         */
        supportFragmentManager.beginTransaction().replace(R.id.fl_my_container, captureFragment)
            .commit()


        tv_input_code.setOnClickListener {
            val intent = Intent(this, AtyInputCode::class.java)

            startActivityForResult(intent, 1)
        }
        tv_light.setOnClickListener {
            CodeUtils.isLightEnable(tv_light.text.equals("打开手电筒"))
            tv_light.text = if ( tv_light.text.equals("打开手电筒")) "关闭手电筒" else "打开手电筒"
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1001 && data != null){
            val resultIntent = Intent()
            val bundle = Bundle()
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS)
            bundle.putString(CodeUtils.RESULT_STRING, data.getStringExtra("code"))
            bundle.putString("type", getType)
            bundle.putString("contract_id", getContractId)
            resultIntent.putExtras(bundle)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }





}