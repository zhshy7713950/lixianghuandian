package com.ruimeng.things.me

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.ruimeng.things.App
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.me.credit.FgtCreditContract
import com.ruimeng.things.shop.PostGlideEngine
import com.ruimeng.things.zipImg
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.CaptureStrategy
import kotlinx.android.synthetic.main.fgt_upload_auth_info.*
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.NotificationHelper
import wongxd.common.loadImg
import wongxd.http
import java.io.File

/**
 * Created by Wongxd on 2019/6/10.
 * https://github.com/wongxd
 * wxd1@live.com
 */
class FgtUploadAuthInfo : BaseBackFragment() {

    companion object {

        private val REQUEST_IMAGE = 1002

        fun newInstance(contractId: String): FgtUploadAuthInfo {
            val fgt = FgtUploadAuthInfo()
            val b = Bundle()
            b.putString("contractId", contractId)
            fgt.arguments = b
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_upload_auth_info

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "上传认证信息")

        riv_card_front.setOnClickListener {
            isFront = true
            getPic()
        }

        riv_card_back.setOnClickListener {
            isFront = false
            getPic()
        }


        btn_next_step.setOnClickListener {
            if (frontImgUrl.isBlank() || backImgUrl.isBlank()) {
                EasyToast.DEFAULT.show("请上传相关图片")
                return@setOnClickListener
            }


            http {
                url = Path.HANDIMG
                params["contract_id"] = contractId
                params["img1"] = frontImgUrl
                params["img2"] = backImgUrl


                onSuccess { res ->
                    riv_card_front?.let {
                        startWithPop(FgtCreditContract.newInstance(contractId))
                    }
                }

            }
        }
    }


    private var isFront = true

    private var frontImgUrl = ""
    private var backImgUrl = ""

    private fun uploadImg(imgPath: String) {

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

            onSuccessWithMsg { res, msg ->
                //                {"errcode":200,"errmsg":"","data":{"string":"http:\/\/cdn.tk.image.xianlubang.com\/201811261643545863.png"}}
                val json = JSONObject(res)
                val data = json.optJSONObject("data")
                val imgUrl = data.optString("string")

                if (isFront) setFrontImg(imgUrl) else setBackImg(imgUrl)
            }
        }
    }

    private fun setFrontImg(imgUrl: String) {
        frontImgUrl = imgUrl
        riv_card_front.loadImg(frontImgUrl)
    }

    private fun setBackImg(imgUrl: String) {
        backImgUrl = imgUrl
        riv_card_back.loadImg(backImgUrl)
    }

    private fun getPic() {
        Matisse.from(this)
            .choose(MimeType.ofAll())
            .capture(true)
            .captureStrategy(
                CaptureStrategy(
                    true,
                    activity?.packageName + ".fileprovider"
                )
            )
            .countable(true)
            .maxSelectable(1)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .thumbnailScale(0.85f)
            .imageEngine(PostGlideEngine())
            .forResult(REQUEST_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {//从相册选择完图片

            val arrayList = ArrayList<String>()
            Matisse.obtainResult(data).forEach { uri ->
                //                Logger.e(uri.toString())
                arrayList.add(
                    PostGlideEngine.getAbsoluteImagePath(NotificationHelper.mContext, uri).replace(
                        "/my_images/",
                        "/storage/emulated/0/"
                    )
                )
            }
            if (arrayList.isNotEmpty()) {
                zipImg(App.getMainAty(), arrayList[0]) {
                    uploadImg(it)
                }
            }
        }
    }


}