package com.ruimeng.things.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.ruimeng.things.R
import com.ruimeng.things.home.helper.ScanResultCheck
import com.utils.FlashUtil
import com.utils.ToastHelper
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.aty_input_code.etCode
import kotlinx.android.synthetic.main.aty_input_code.tvConfirm
import kotlinx.android.synthetic.main.aty_scan_qrcode.topbar
import kotlinx.android.synthetic.main.aty_scan_qrcode.tv_light
import wongxd.base.AtyBase

class AtyInputCode : AtyBase() {
    var getType = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_input_code)
        getType = intent.getIntExtra("type",1)
        initTopbar(topbar,"手动输入编码" )
        etCode.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (etCode.text.trim().length > 0){
                    tvConfirm.setBackgroundResource(R.drawable.bg_btn_common1)
                }else{
                    tvConfirm.setBackgroundResource(R.drawable.bg_btn_common4)
                }
            }

        })
        tvConfirm.setOnClickListener {
            if (etCode.text.trim().length == 0){
                ToastHelper.shortToast(this,"请输入编码")
            }else{
               checkScanResult(etCode.text.toString().trim())
            }
        }
        tv_light.setOnClickListener {
            FlashUtil.changeFlashLight(this,tv_light.text.equals("打开手电筒"))
            tv_light.text = if ( tv_light.text.equals("打开手电筒")) "关闭手电筒" else "打开手电筒"
        }
    }
    private fun checkScanResult(result:String){
        ScanResultCheck().checkResult(getType,result,object :
            ScanResultCheck.CheckResultListener{
            override fun checkStatus(pass: Boolean) {
                if (pass){
                    val intent = Intent()
                    intent.putExtra("code",result)
                    setResult(1001,intent)
                    finish()
                }
            }
        })

    }

}