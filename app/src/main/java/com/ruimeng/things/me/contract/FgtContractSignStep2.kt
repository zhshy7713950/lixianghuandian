package com.ruimeng.things.me.contract

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.ruimeng.things.*
import com.ruimeng.things.me.widget.signature_view.SignatureView
import com.ruimeng.things.shop.PostGlideEngine
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.CaptureStrategy
import kotlinx.android.synthetic.main.fgt_contract_sign_step_2.*
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.NotificationHelper.mContext
import wongxd.common.getCurrentAty
import wongxd.common.loadImg
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.http
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by wongxd on 2018/11/26.
 */
class FgtContractSignStep2 : BaseBackFragment() {

    companion object {
        const val REQUEST_IMAGE = 1002

        fun newInstance(contractId: String): FgtContractSignStep2 {
            val fgt = FgtContractSignStep2()
            val b = Bundle()
            b.putString("contractId", contractId)
            fgt.arguments = b
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_contract_sign_step_2

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "合同签约")

        fl_front_id_card.setOnClickListener { getPic() }

        fl_hw.setOnClickListener { sign(activity) }


        btn_submit.setOnClickListener {

            fun doSubmit() {
                http {
                    url = PathV3.CONFIRM_SIGN

                    params["contract_id"] = contractId
                    params["id_png"] = frontImgUrl
                    params["usersign_png"] = hwImgUrl

                    onSuccessWithMsg { res, msg ->
                        EasyToast.DEFAULT.show(msg)
                        UserInfoLiveData.refresh()
                        setFragmentResult(FgtContractSignStep1.RESULT_CODE_SHOULD_POP, Bundle())
                        pop()
                    }

                    onFail { code, msg ->
                        EasyToast.DEFAULT.show(msg)
                    }
                }
            }

            if (frontImgUrl.isBlank()) {
                EasyToast.DEFAULT.show("请上传身份证人脸照片")
                return@setOnClickListener
            }

            QMUIDialog.MessageDialogBuilder(activity)
                .setTitle("请阅读后点击确认!")
                .setMessage("是否确认签约？ ")

                .addAction("确认") { dialog, index ->
                    dialog.dismiss()
                    doSubmit()
                }

                .addAction("取消") { dialog, index ->
                    dialog.dismiss()
                }
                .show()
        }


    }


    private fun setFrontImg(imgUrl: String) {
        frontImgUrl = imgUrl
        rl_front_id_card.visibility = View.GONE
        iv_front_id_card.visibility = View.VISIBLE
        iv_front_id_card.loadImg(frontImgUrl)
    }

    private fun setHwImg(imgUrl: String) {
        hwImgUrl = imgUrl
        tv_hw.visibility = View.GONE
        iv_hw.visibility = View.VISIBLE
        iv_hw.loadImg(imgUrl)
    }

    private fun getPic() {
        getPermissions(getCurrentAty(), PermissionType.CAMERA,PermissionType.WRITE_EXTERNAL_STORAGE, allGranted = {
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
        })
    }


    private var frontImgUrl = ""
    private var hwImgUrl = ""

    private fun upLoadImg(imgPath: String, isHwImg: Boolean) {

        val dlg = ProgressDialog(activity)
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dlg.max = 100
        dlg.setTitle(if (isHwImg) "上传签名" else "上传身份证")
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
                //                {"errcode":200,"errmsg":"","data":{"string":"http:\/\/cdn.tk.image.xianlubang.com\/201811261643545863.png"}}
                val json = JSONObject(res)
                val data = json.optJSONObject("data")
                val imgUrl = data.optString("string")

                if (!isHwImg) setFrontImg(imgUrl) else setHwImg(imgUrl)
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
                    upLoadImg(it, false)
                }
            }
        }
    }


    private fun sign(aty: Activity?) {

        val fileDirName =
            activity?.application?.externalCacheDir?.absolutePath + File.separator + "img"//应用缓存地址

        val dirFile = File(fileDirName)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        val lasSignFilePath = File(dirFile, "ec_signature.jpg").absolutePath

        val popupWindow = PopupWindow(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = false
        popupWindow.setBackgroundDrawable(ColorDrawable())
        val inflate = View.inflate(activity, R.layout.layout_ec_sign, null)
        popupWindow.contentView = inflate
        popupWindow.showAtLocation(
            aty?.findViewById<View>(android.R.id.content),
            Gravity.NO_GRAVITY,
            0,
            0
        )
        val signatureView = inflate.findViewById<SignatureView>(R.id.signatureView)
        inflate.findViewById<Button>(R.id.confirm).setOnClickListener {
            val lastFile = File(lasSignFilePath)
            if (lastFile.exists()) {
                val delete = lastFile.delete()
                if (delete) {
                    //删除图片
                    Log.e("w-", "删除电子签名成功")
                } else {
                    Log.e("w-", "删除电子签名失败")
                }
            } else {
                lastFile.createNewFile()
            }

            val signatureBitmap = signatureView.signatureBitmap

            if (trySaveBmp2Jpg(signatureBitmap, lastFile)) {
                upLoadImg(lasSignFilePath, true)
            } else {
                EasyToast.DEFAULT.show("保存失败")
            }
            popupWindow.dismiss()
        }

        inflate.findViewById<Button>(R.id.clear)
            .setOnClickListener { signatureView.clear() }

        inflate.findViewById<Button>(R.id.cancel)
            .setOnClickListener { popupWindow.dismiss() }
    }


    private fun trySaveBmp2Jpg(signature: Bitmap, photo: File): Boolean {
        @Throws(IOException::class)
        fun saveBitmapToJPG(bitmap: Bitmap, photo: File) {
            val newBitmap =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newBitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            val stream = FileOutputStream(photo)
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.close()
        }

        var result = false
        try {
            saveBitmapToJPG(signature, photo)
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }
}