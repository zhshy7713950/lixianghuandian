package com.ruimeng.things.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.ruimeng.things.R
import com.utils.FlashUtil
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.aty_input_code.etCode
import kotlinx.android.synthetic.main.aty_input_code.tvConfirm
import kotlinx.android.synthetic.main.aty_scan_qrcode.topbar
import kotlinx.android.synthetic.main.aty_scan_qrcode.tv_light
import wongxd.base.AtyBase

class AtyInputCode : AtyBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aty_input_code)
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
                val intent = Intent()
                intent.putExtra("code",etCode.text.toString().trim())
                setResult(1001,intent)
                finish()
            }
        }
        tv_light.setOnClickListener {
            FlashUtil.changeFlashLight(this,tv_light.text.equals("打开手电筒"))
            tv_light.text = if ( tv_light.text.equals("打开手电筒")) "关闭手电筒" else "打开手电筒"
        }
    }


}