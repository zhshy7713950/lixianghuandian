package com.ruimeng.things.me.contract

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.ruimeng.things.FgtViewBigImg
import com.ruimeng.things.PathV3
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.contract.bean.MyContractDetailBean
import com.ruimeng.things.me.contract.download_pdf.AndroidDownloadManager
import com.ruimeng.things.me.contract.download_pdf.AndroidDownloadManagerListener
import com.utils.TextUtil
import kotlinx.android.synthetic.main.fgt_my_contract_detail.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.loadImg
import wongxd.common.recycleview.yaksa.linear
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.OpenFileThing
import wongxd.utils.utilcode.util.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by wongxd on 2019/12/24.
 */
class FgtMyContractDetail : BaseBackFragment() {

    companion object {

        fun newInstance(contractId: String, deviceId: String): FgtMyContractDetail {

            return FgtMyContractDetail().apply {
                arguments = Bundle().apply {
                    putString("contractId", contractId)
                    putString("deviceId", deviceId)
                }
            }
        }
    }

    private val contractId by lazy { arguments?.getString("contractId", "") ?: "" }
    private val deviceId by lazy { arguments?.getString("deviceId", "") ?: "" }

    override fun getLayoutRes(): Int = com.ruimeng.things.R.layout.fgt_my_contract_detail


    val rootDir by lazy {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "${getString(com.ruimeng.things.R.string.app_name)}合约"
    }

    val thisContractDir by lazy {
        rootDir + "编号:$deviceId"
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "合约详情")

        FileUtils.createOrExistsDir(rootDir)


        getInfo()
    }

    private fun doSavePdf() {
        if (pdfUrl.isBlank()) {
            EasyToast.DEFAULT.show("没有对应pdf文件")
            return
        }

        AndroidDownloadManager(activity, pdfUrl)
            .setListener(object : AndroidDownloadManagerListener {
                override fun onPrepare() {
                    Log.d("w-download-pdf", "onPrepare")
                }

                override fun onSuccess(path: String) {
                    Log.d("w-download-pdf", "onSuccess >>>>$path")
                    NormalDialog(activity)
                        .apply {
                            style(NormalDialog.STYLE_TWO)
                            title("合约下载成功")
                            content("合约下载到了【${path}】目录中。")
                            btnNum(1)
                            btnText("查看")
                            setOnBtnClickL(OnBtnClickL {
                                OpenFileThing.openAssignFolder(
                                    activity,
                                    Uri.fromFile(File(path)),
                                    OpenFileThing.FileType.pdf
                                )
                                dismiss()
                            })
                            show()
                        }
                }

                override fun onFailed(throwable: Throwable) {
                    Log.e("w-download-pdf", "onFailed", throwable)
                }
            })
            .download()
    }


    private fun getInfo() {

        http {
            url = PathV3.MY_CONTRACT_DETAIL
            params["contract_id"] = contractId
            params["appType"] = "lxhd"

            onSuccess { res ->
                tv_device_num_my_contract_detail?.let {

                    val bean = res.toPOJO<MyContractDetailBean>().data

                    if (bean.down_sign == 1) {
                        topbar.addRightTextButton("下载", com.ruimeng.things.R.id.right_text)
                            .apply {
                                setTextColor(Color.WHITE)
                                setOnClickListener {
                                    QMUIDialog.MenuDialogBuilder(activity)
                                        .addItem("pdf下载") { dialog, which ->
                                            doSavePdf()
                                            dialog.dismiss()
                                        }
                                        .addItem("png下载") { dialog, which ->
                                            doSaveContractPngs()
                                            dialog.dismiss()
                                        }
                                        .show()

                                }
                            }
                    }

                    tv_device_num_my_contract_detail.text = "电池编号：${bean.device_id}"
                    tv_device_model_my_contract_detail.text = "${bean.model_str}"
                    tv_rent_long_my_contract_detail.text = "${bean.renttime_str}"
                    tv_deposit_my_contract_detail.text =
                        if (FgtHome.payType == "101") "已免押" else "${bean.deposit}元"
                    tv_rent_money_my_contract_detail.text = "${bean.rent}元"
                    if (bean.paymentName == "") {
                        tv_base_package.text =
                            TextUtil.getSpannableString(arrayOf("租电套餐：", "暂无"))
                        tv_base_package_time.visibility = View.GONE
                        tv_change_package.text =
                            TextUtil.getSpannableString(arrayOf("换电套餐：", "暂无"))
                        tv_change_package_time.visibility = View.GONE
                    } else {
                        tv_base_package.text =
                            TextUtil.getSpannableString(arrayOf("租电套餐：", bean.paymentName))
                        tv_base_package_time.text =
                            TextUtil.formatTime(bean.begin_time, bean.exp_time)
                        tv_change_package.text =
                            TextUtil.getSpannableString(arrayOf("换电套餐：", "次数无限制"))
                        tv_change_package_time.text = tv_base_package_time.text
                    }

//                    val options = bean.userOptions?.filter { it.option_type == "2" }
//                    if (options != null && !options.isEmpty() && bean.paymentName != ""){
//
//                    }else{
//
//                    }

                    pdfUrl = bean.pdf
                    pngs.clear()
                    pngs.addAll(bean.sign_pngs.map { it.png })

                    rv_my_contract_detail.linear {

                        bean.sign_pngs.forEach { png ->

                            itemDsl {
                                xml(com.ruimeng.things.R.layout.item_rv_my_contract_detail)
                                renderX { position, view ->
                                    view.findViewById<ImageView>(com.ruimeng.things.R.id.iv)
                                        .loadImg(png.png)
                                    view.setOnClickListener {
                                        start(FgtViewBigImg.newInstance(png.png))
                                    }
                                }
                            }
                        }
                    }
                }

            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }


    private var pdfUrl = ""
    private val pngs = mutableListOf<String>()

    private fun doSaveContractPngs() {

        if (pngs.isEmpty()) return

        FileUtils.createOrExistsDir(thisContractDir)
        val mSaveDialog = ProgressDialog.show(context, "保存图片", "图片正在保存中，请稍等...", true)

        doAsync {
            pngs.forEachWithIndex { i, png ->
                getImageInputStream(png, i)

            }
            uiThread {
                mSaveDialog.dismiss()
                NormalDialog(activity)
                    .apply {
                        style(NormalDialog.STYLE_TWO)
                        title("合约下载成功")
                        content("合约下载到了【${thisContractDir}】目录中。")
                        btnNum(1)
                        btnText("查看")
                        setOnBtnClickL(OnBtnClickL {
                            openAlbum()
                        })
                        show()
                    }

            }
        }
    }


    private fun openAlbum() {
        val intent = Intent()
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT < 19) {
            intent.action = Intent.ACTION_GET_CONTENT
        } else {
            intent.action = Intent.ACTION_OPEN_DOCUMENT
        }
        activity?.startActivity(intent)
    }

    /**
     * 获取网络图片
     * @param imageurl 图片网络地址
     * @return Bitmap 返回位图
     */
    private fun getImageInputStream(imageurl: String, index: Int) {
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


        bitmap?.let { saveImage(it, "$thisContractDir/编号:${deviceId}-$index.png") }
    }


    /**
     * 保存位图到本地
     * @param bitmap
     * @param path 本地路径
     * @return void
     */
    private fun saveImage(bitmap: Bitmap, path: String) {
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


        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(
//                context?.contentResolver,
//                file.absolutePath,
//                file.name,
//                null
//            )
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        }
        // 最后通知图库更新
        context?.sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + file.absoluteFile)
            )
        )


    }

}