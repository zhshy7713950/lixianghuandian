package com.ruimeng.things.home

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_follow_wechat_account.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.utils.SystemUtils
import wongxd.utils.utilcode.util.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by wongxd on 2020/1/8.
 */
class FgtFollowWechatAccount : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_follow_wechat_account

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "关注公众号")

        tv_save_qr?.setOnClickListener {
            doSavePng(listOf("www"))
        }

        tv_open_wechat.setOnClickListener { doOpenWechat() }

        tv_open_wechat_way_two.setOnClickListener { tv_open_wechat.performClick() }


        tv_copy_wechat_account_name.setOnClickListener {
            SystemUtils.copyText(activity, "享锂来")
            EasyToast.DEFAULT.show("复制成功")
        }
    }


    private fun doOpenWechat() {
        val lan = activity?.packageManager?.getLaunchIntentForPackage("com.tencent.mm")
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.component = lan?.component
        startActivity(intent)
    }

    private fun doSavePng(pngs: List<String>) {

        val rootDir by lazy {
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "${getString(R.string.app_name)}"
        }

        val thisContractDir by lazy {
            rootDir + File.separator + "wecaht"
        }


        /**
         * 保存位图到本地
         * @param bitmap
         * @param path 本地路径
         * @return void
         */
        fun saveImage(bitmap: Bitmap, path: String) {
            val file = File(path)
            var fileOutputStream: FileOutputStream? = null
            //文件夹不存在，则创建它
            if (file.exists()) {
                file.delete()
            }
            try {
                fileOutputStream = FileOutputStream(path)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }


//            // 其次把文件插入到系统图库
//            try {
//                MediaStore.Images.Media.insertImage(
//                    context?.contentResolver,
//                    file.absolutePath,
//                    file.name,
//                    null
//                )
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            }
            // 最后通知图库更新
            context?.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + file.absoluteFile)
                )
            )


        }


        fun getImageFromResourec(index: Int) {

            val url: URL
            var connection: HttpURLConnection? = null
            var bitmap: Bitmap? = null
            try {
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.wechat_account_qr)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            bitmap?.let { saveImage(it, "$thisContractDir/$index.png") }
        }

        /**
         * 获取网络图片
         * @param imageurl 图片网络地址
         * @return Bitmap 返回位图
         */
        fun getImageInputStream(imageurl: String, index: Int) {

            val url: URL
            var connection: HttpURLConnection? = null
            var bitmap: Bitmap? = null
            try {
                url = URL(imageurl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 6000 //超时设置
                connection.doInput = true
                connection.useCaches = false //设置不使用缓存
                val inputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }


            bitmap?.let { saveImage(it, "$thisContractDir/$index.png") }
        }

        if (pngs.isEmpty()) return

        FileUtils.createOrExistsDir(thisContractDir)
        val mSaveDialog = ProgressDialog.show(context, "保存图片", "图片正在保存中，请稍等...", true)

        doAsync {
            pngs.forEachWithIndex { i, png ->
                //                getImageInputStream(png, i)
                getImageFromResourec(System.currentTimeMillis().toInt())
            }
            uiThread {
                mSaveDialog.dismiss()
                NormalDialog(activity)
                    .apply {
                        style(NormalDialog.STYLE_TWO)
                        title("保存成功")
                        content("保存到了【${thisContractDir}】目录中。")
                        btnNum(1)
                        btnText("确定")
                        setOnBtnClickL(OnBtnClickL {
                            dismiss()
                        })
                        show()
                    }

            }
        }
    }


}