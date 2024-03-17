package com.ruimeng.things

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import com.ruimeng.things.home.AtyInputCode
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.helper.ScanResultCheck
import com.tbruyelle.rxpermissions2.RxPermissions
import com.utils.ToastHelper
import com.uuzuche.lib_zxing.activity.CaptureFragment
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.activity_scan_qr_code.*
import kotlinx.android.synthetic.main.aty_scan_qrcode.tv_light
import wongxd.base.AtyBase
import wongxd.common.getCurrentAty
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions


class ScanQrCodeActivity : AtyBase() {

    private var mActivity: AppCompatActivity? = null

    private var getType = ""
    private var getContractId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr_code)
        mActivity = this

        if (!TextUtils.isEmpty(intent.getStringExtra("type"))) {
            getType = intent.getStringExtra("type")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("type"))) {
            getType = intent.getStringExtra("type")
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("contract_id"))) {
            getContractId = intent.getStringExtra("contract_id")
        }
        initTopbar(topbar, "扫一扫")
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
               ToastHelper.shortToast(this@ScanQrCodeActivity,"无法识别二维码")
            }
        }
        /**
         * 替换我们的扫描控件
         */
        supportFragmentManager.beginTransaction().replace(R.id.fl_my_container, captureFragment)
            .commit()


        tv_input_code.setOnClickListener {
            val intent = Intent(this, AtyInputCode::class.java)
            intent.putExtra("type",getTypeCode())
            startActivityForResult(intent, 1)
        }
        tv_light.setOnClickListener {
            CodeUtils.isLightEnable(tv_light.text.equals("打开手电筒"))
            tv_light.text = if ( tv_light.text.equals("打开手电筒")) "关闭手电筒" else "打开手电筒"
        }
        tv_select_image.setOnClickListener {
            getPermissions(getCurrentAty(), PermissionType.READ_EXTERNAL_STORAGE, allGranted = {
                // 创建意图对象并指定操作为选取图片
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                // 设置结果处理器
                startActivityForResult(intent, 1002)
            })
        }
    }
    fun getTypeCode(): Int {
     return   when(getType){
            "换电开门"->1
            "换电"->2
            "租电"->4
            "退还"->5
         "冻结"->6

         else -> 0
     }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1001 && data != null){
            checkScanResult(data.getStringExtra("code"))
        }else if (requestCode == 1 && resultCode == 1002 && data != null){
            checkScanResult(data.getStringExtra("code"))
        }
    }

    private fun checkScanResult(result:String){
        ScanResultCheck().checkResult(getTypeCode(),result,object :ScanResultCheck.CheckResultListener{
            override fun checkStatus(pass: Boolean) {
                if (pass){
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
            }
        })

    }





}