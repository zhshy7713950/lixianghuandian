package com.ruimeng.things.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import com.ruimeng.things.R
import com.ruimeng.things.ScanQrCodeActivity
import com.ruimeng.things.home.FgtHome.Companion.REQUEST_ZXING_CODE
import com.ruimeng.things.home.helper.ScanResultCheck
import com.utils.*
import com.uuzuche.lib_zxing.activity.CaptureFragment
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.uuzuche.lib_zxing.activity.ZXingLibrary
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import kotlinx.android.synthetic.main.activity_scan_qr_code.*
import kotlinx.android.synthetic.main.aty_scan_qrcode.*
import kotlinx.android.synthetic.main.aty_scan_qrcode.topbar
import kotlinx.android.synthetic.main.aty_scan_qrcode.tv_input_code
import kotlinx.android.synthetic.main.aty_scan_qrcode.tv_light
import kotlinx.android.synthetic.main.aty_scan_qrcode.tv_select_image
import wongxd.base.AtyBase
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.getCurrentAty
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.base.custom.anylayer.AnyLayer.target

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.ruimeng.things.home.checkImgs.PostGlideEngine
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


/**
 * Created by wongxd on 2019/7/17.
 */
class AtyScanQrcode : AtyBase() {

    companion object {


        val TYPE_CHANGE = "change"

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
        tv_select_image.setOnClickListener {
            getPermissions(getCurrentAty(), PermissionType.WRITE_EXTERNAL_STORAGE, allGranted = {
                Matisse.from(this)
                    .choose(MimeType.allOf())//图片类型
                    .countable(true)//是否显示选择图片的数字
                    .maxSelectable(1)//最大图片选择数
                    //gridExpectedSize(120)getResources().getDimensionPixelSize(R.dimen.grid_expected_size)   显示图片大小
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向
                    .thumbnailScale(0.85f)//清晰度
                    .theme(R.style.Matisse_Zhihu)
                    .imageEngine(PostGlideEngine())
                    .forResult(1002);//请求码

            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1001 && data != null){
            checkScanResult(data.getStringExtra("code"))
        }else if (requestCode == 1002  && resultCode == RESULT_OK){
            val list = Matisse.obtainResult(data)
            Glide.with(this).asBitmap().load(list[0]).into(object: SimpleTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    val baos = ByteArrayOutputStream()
//// 质量压缩方法,这里100表示不压缩,把压缩后的数据存放到baos中
//                    resource.compress(Bitmap.CompressFormat.JPEG, 20, baos)
//                    val bais = ByteArrayInputStream(baos.toByteArray())
//                    val bmScaled = BitmapFactory.decodeStream(bais, null, null)
                    var bitmap2 = ImageUtils.compressBitmap(resource,300)
                    Log.i("TAG", "***************sd")
                    ImageUtils.saveImage(this@AtyScanQrcode,bitmap2)
                    var code = QrCodeUtil.deCodeQR(bitmap2)
                    if (code == null){
                        ToastHelper.shortToast(this@AtyScanQrcode,"无法识别二维码")
                    }else{
                        checkScanResult(code)
                    }
                    Log.i("TAG", "onResourceReady: "+code)
                }

            })
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