package com.ruimeng.things.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import com.ruimeng.things.R
import com.ruimeng.things.ScanQrCodeActivity
import com.ruimeng.things.home.FgtHome.Companion.REQUEST_ZXING_CODE
import com.ruimeng.things.home.helper.ScanResultCheck
import com.utils.FlashUtil
import com.utils.ToastHelper
import com.uuzuche.lib_zxing.activity.CaptureFragment
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.aty_scan_qrcode.*
import wongxd.base.AtyBase
import wongxd.base.custom.anylayer.AnyLayer


/**
 * Created by wongxd on 2019/7/17.
 */
class AtyScanQrcode : AtyBase() {

    companion object {


        val TYPE_CHANGE = "change"

        const val TYPE_PAY_RENT = "type_pay_rent"

        val RESULT_PREFIX = "resultPrefix"

        val RESULT_OLD_CONTRACT_ID = "resultOldContractId"
        val IS_HOST="isHost"

        /**
         * @param resultPrefix 扫码结果的前缀  [TYPE_CHANGE]-更换设备
         */
        fun start(aty: Activity, resultPrefix: String = "", oldContractId: String = "", isHost: String = "") {
            Log.i("data===","===contract_id3===${oldContractId}")
            aty.startActivityForResult(Intent(aty, AtyScanQrcode::class.java).apply {
                putExtra(RESULT_PREFIX, resultPrefix)
                putExtra(RESULT_OLD_CONTRACT_ID, oldContractId)
                putExtra(IS_HOST, isHost)
            }, REQUEST_ZXING_CODE)
        }
    }


    private val resultOldContractId by lazy { intent?.getStringExtra(RESULT_OLD_CONTRACT_ID) ?: ""

    }

    private val getIsHost by lazy { intent?.getStringExtra(IS_HOST) ?: ""

    }

    private val resultPrefix by lazy { intent?.getStringExtra(RESULT_PREFIX) ?: "" }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_scan_qrcode)

        initTopbar(topbar, "扫一扫")
        Log.i("data===","===contract_id4===${intent?.getStringExtra(RESULT_OLD_CONTRACT_ID)}")
        /**
         * 执行扫面Fragment的初始化操作
         */
        val captureFragment = CaptureFragment()
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.layout_scan_qrcode_camera)

        //二维码解析回调函数
        captureFragment.analyzeCallback = object : CodeUtils.AnalyzeCallback {
            override fun onAnalyzeSuccess(mBitmap: Bitmap, result: String) {
                checkScanResult(result)
            }

            override fun onAnalyzeFailed() {
                ToastHelper.shortToast(this@AtyScanQrcode,"二维码错误")
            }
        }
        /**
         * 替换我们的扫描控件
         */
        supportFragmentManager.beginTransaction().replace(R.id.fl_my_container, captureFragment).commit()

        tv_input_code.setOnClickListener {
            val intent = Intent(this, AtyInputCode::class.java)
            intent.putExtra("type",3)
            startActivityForResult(intent, 1)

//            AnyLayer.with(this)
//                .contentView(R.layout.fgt_input_device_code)
//                .bindData { anyLayer ->
//                    val et = anyLayer.contentView.findViewById<EditText>(R.id.et_input_device_code)
//                    val btn = anyLayer.contentView.findViewById<Button>(R.id.btn_submit_device_code)
//                    if (resultPrefix == TYPE_CHANGE) {
//                        et.hint = "请输入要更换设备的编号"
//                    }
//                    btn.setOnClickListener {
//                        val result = et.text.toString()
//                        anyLayer.dismiss()
//                        if (result.isNotBlank()) {
//                            this@AtyScanQrcode.apply {
//                                setResult(Activity.RESULT_OK, Intent().apply {
//                                    putExtras(Bundle().apply {
//                                        putInt(
//                                            CodeUtils.RESULT_TYPE,
//                                            if (result.isBlank()) CodeUtils.RESULT_FAILED else CodeUtils.RESULT_SUCCESS
//                                        )
//                                        putString(RESULT_PREFIX, resultPrefix)
//                                        putString(RESULT_OLD_CONTRACT_ID, resultOldContractId)
//                                        putString(IS_HOST, getIsHost)
//                                        putString(CodeUtils.RESULT_STRING, result)
//                                    })
//                                })
//                                finish()
//                            }
//                        }
//                    }
//                }
//                .backgroundBlurScale(10f)
//                .backgroundBlurRadius(10f)
//                .backgroundColorInt(Color.parseColor("#55000000"))
//                .show()
        }

        tv_light.setOnClickListener {
           CodeUtils.isLightEnable(tv_light.text.equals("打开手电筒"))
            tv_light.text = if ( tv_light.text.equals("打开手电筒")) "关闭手电筒" else "打开手电筒"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1001 && data != null){
            checkScanResult(data.getStringExtra("code")?:"")
        }
    }
    private fun checkScanResult(result:String){
        ScanResultCheck().checkResult(3,result,object :
            ScanResultCheck.CheckResultListener{
            override fun checkStatus(pass: Boolean) {
                if (pass) {
                    this@AtyScanQrcode.apply {
                        setResult(Activity.RESULT_OK, Intent().apply {
                            putExtras(Bundle().apply {
                                putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS)
                                putString(RESULT_PREFIX, resultPrefix)
                                putString(RESULT_OLD_CONTRACT_ID, resultOldContractId)
                                putString(IS_HOST, getIsHost)
                                putString(CodeUtils.RESULT_STRING, result)
                            })
                        })
                        finish()
                    }
                }
            }
        })

    }



}