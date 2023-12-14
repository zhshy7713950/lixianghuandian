package com.ruimeng.things.me

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.ruimeng.things.*
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.FgtPayRentMoney
import com.ruimeng.things.home.bean.ScanResultEvent
import com.ruimeng.things.shop.PostGlideEngine
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.CaptureStrategy
import kotlinx.android.synthetic.main.fgt_true_name_v3.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.NotificationHelper.mContext
import wongxd.common.loadImg
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.http
import java.io.File

/**
 * Created by wongxd on 2020/1/9.
 * https://github.com/wongxd
 * wxd1@live.com
 */
class FgtTrueName : BaseBackFragment() {

    companion object {
        const val REQUEST_IMAGE = 1002


        fun newInstance(deviceId: String = ""): FgtTrueName {
            return FgtTrueName().apply {
                arguments = Bundle().apply {
                    putString("deviceId", deviceId)
                }
            }
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_true_name_v3

    private val deviceId: String by lazy { arguments?.getString("deviceId") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "实名认证")

        iv_front_true_name.setOnClickListener {
            isFrontFlag = 1
            getPic()
        }

        iv_back_true_name.setOnClickListener {
            isFrontFlag = 2
            getPic()
        }


        iv_hand_card_true_name.setOnClickListener {
            isFrontFlag = 3
            getPic()
        }



        rtv_submit_true_name.setOnClickListener {

            if (frontImgUrl.isBlank() || backImgUrl.isBlank()) {
                EasyToast.DEFAULT.show("请上传身份证正面及背面照片")
                return@setOnClickListener
            }


            if (handCardUrl.isBlank()) {
                EasyToast.DEFAULT.show("请上传手持身份证照片")
                return@setOnClickListener
            }

            http {
                url = PathV3.REAL_NAME

                params["id_png_a"] = frontImgUrl
                params["id_png_b"] = backImgUrl
                params["hold_id_png"] = handCardUrl

                onSuccessWithMsg { res, msg ->
                    EasyToast.DEFAULT.show(msg)
                    UserInfoLiveData.refresh()
                    showTipDialog(activity, msg = msg, click = {

                        pop()

                        if (deviceId.isNotBlank()) {
//                            FgtHome.dealScanResult(deviceId)
                            EventBus.getDefault().post(ScanResultEvent(deviceId, FgtPayRentMoney.PAGE_TYPE_CREATE))
                        }

                    })

                }

                onFail { code, msg ->
                    showTipDialog(activity, msg = msg)
                }
            }
        }


    }


    private fun setFrontImg(imgUrl: String) {
        frontImgUrl = imgUrl
        iv_front_true_name.loadImg(frontImgUrl)
    }

    private fun setBackImg(imgUrl: String) {
        backImgUrl = imgUrl
        iv_back_true_name.loadImg(backImgUrl)
    }


    private fun setHandCardImg(imgUrl: String) {
        handCardUrl = imgUrl
        iv_hand_card_true_name.loadImg(handCardUrl)
    }

    private fun getPic() {
        getPermissions(
            listOf(
                PermissionType.CAMERA,
                PermissionType.WRITE_EXTERNAL_STORAGE,
                PermissionType.READ_EXTERNAL_STORAGE
            )
        ){
            Matisse.from(this)
                .choose(MimeType.allOf())
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

    }

    private var isFrontFlag = 1

    private var frontImgUrl = ""
    private var backImgUrl = ""
    private var handCardUrl = ""

    private fun uploadPng(imgPath: String) {

        val dlg = ProgressDialog(activity)
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dlg.max = 100
        dlg.setTitle("上传身份证照片")
        dlg.show()

        http {
            url = Path.UPLOAD_IMG

            imgs["file"] = File(imgPath)

            onUploadFile { progress, total, index ->
                dlg.progress = progress
            }

            onFinish {
                dlg.dismiss()
            }

            onSuccessWithMsg { res, msg ->
                //{"errcode":200,"errmsg":"","data":{"string":"http:\/\/cdn.tk.image.xianlubang.com\/201811261643545863.png"}}
                val json = JSONObject(res)
                val data = json.optJSONObject("data")
                val imgUrl = data.optString("string")

                when (isFrontFlag) {
                    1 -> setFrontImg(imgUrl)
                    2 -> setBackImg(imgUrl)
                    else -> setHandCardImg(imgUrl)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {//从相册选择完图片

            val arraylist = ArrayList<String>()
            Matisse.obtainResult(data).forEach { uri ->
                //                Logger.e(uri.toString())
                arraylist.add(
                    PostGlideEngine.getAbsoluteImagePath(mContext, uri).replace(
                        "/my_images/",
                        "/storage/emulated/0/"
                    )
                )
            }
            if (arraylist.isNotEmpty()) {
                zipImg(App.getMainAty(), arraylist[0]) {
                    uploadPng(it)
                }
            }
        }
    }
}