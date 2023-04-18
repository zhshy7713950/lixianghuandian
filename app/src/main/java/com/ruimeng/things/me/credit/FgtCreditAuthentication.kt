package com.ruimeng.things.me.credit

import android.app.ProgressDialog
import android.os.Bundle
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.App
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.zipImg
import kotlinx.android.synthetic.main.fgt_credit_authentication.*
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.base.custom.idcardCamera.camera.CameraActivity
import wongxd.common.EasyToast
import wongxd.common.getSweetDialog
import wongxd.common.loadImg
import wongxd.http
import java.io.File

/**
 * Created by wongxd on 2019/1/2.
 */
class FgtCreditAuthentication : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_credit_authentication


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "信用体系认证")


        iv_upload_idcard_oneside.setOnClickListener {
            CameraActivity.navToCamera(this, CameraActivity.TYPE_ID_CARD_FRONT) { imgPath ->
                isFront = true
                zipImg(App.getMainAty(), imgPath) {
                    upLoadIDCARD(it)
                }
            }
        }


        iv_upload_idcard_otherside.setOnClickListener {
            CameraActivity.navToCamera(this, CameraActivity.TYPE_ID_CARD_BACK) { imgPath ->
                isFront = false
                zipImg(App.getMainAty(), imgPath) {
                    upLoadIDCARD(it)
                }
            }
        }



        btn_next_credit_authentication.setOnClickListener {
            val phone = et_phone_credit_authentication.text.toString()
            val card = et_card_num_credit_authentication.text.toString()
            val address = et_address_credit_authentication.text.toString()


            if (address.isBlank()) {
                EasyToast.DEFAULT.show("请填地址信息")
                return@setOnClickListener
            }

            if (phone.isBlank() || card.isBlank()) {
                EasyToast.DEFAULT.show("请填写银行卡信息")
                return@setOnClickListener
            }

            if (frontImgUrl.isBlank() || backImgUrl.isBlank()) {
                EasyToast.DEFAULT.show("请上传身份证照片")
                return@setOnClickListener
            }


            val dlgProgress = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "认证中")
            dlgProgress.show()

            http {
                url = Path.CREDITAPPLY
                params["bank_no"] = card
                params["bank_mobile"] = phone
                params["img1"] = frontImgUrl
                params["img2"] = backImgUrl
                params["address"] = address

                onFinish {
                    dlgProgress.dismissWithAnimation()
                }

                onSuccessWithMsg { res, msg ->
                    EasyToast.DEFAULT.show(msg)
                    pop()
                }

            }
        }
    }

    private var isFront = true

    private var frontImgUrl = ""
    private var backImgUrl = ""

    private fun setFrontImg(imgUrl: String) {
        frontImgUrl = imgUrl
        iv_upload_idcard_oneside.loadImg(if (frontImgUrl.isBlank()) R.drawable.upload_idcard_oneside else frontImgUrl)
    }

    private fun setBackImg(imgUrl: String) {
        backImgUrl = imgUrl
        iv_upload_idcard_otherside.loadImg(if (backImgUrl.isBlank()) R.drawable.upload_idcard_otherside else backImgUrl)
    }

    fun upLoadIDCARD(imgPath: String) {

        val dlg = ProgressDialog(activity)
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dlg.max = 100
        dlg.setTitle("上传身份证")
        dlg.show()

        http {
            url = Path.UPLOAD_IMG

            imgs["file"] = File(imgPath)

            onUploadFile { progress, total, index ->
                //                wongxd.utils.utilcode.util.LogUtils.i(progress, total, index)
                dlg.progress = progress
            }

            onFinish {
                dlg.dismiss()
            }

            onFail { code, msg ->
                if (isFront) setFrontImg("") else setBackImg("")
            }

            onSuccessWithMsg { res, msg ->
                //                {"errcode":200,"errmsg":"","data":{"string":"http:\/\/cdn.tk.image.xianlubang.com\/201811261643545863.png"}}
                val json = JSONObject(res)
                val data = json.optJSONObject("data")
                val imgUrl = data.optString("string")

                if (isFront) setFrontImg(imgUrl) else setBackImg(imgUrl)
            }
        }
    }


}