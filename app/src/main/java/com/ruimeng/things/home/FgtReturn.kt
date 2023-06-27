package com.ruimeng.things.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.recyclerview.widget.LinearLayoutManager

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundFrameLayout
import com.ruimeng.things.CustomDialog
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.DeviceDetailBean
import com.ruimeng.things.home.bean.ReturnLogBean
import com.ruimeng.things.home.checkImgs.*
import com.ruimeng.things.me.contract.FgtContractSignStep1
import com.utils.CommonDialogCallBackHelper
import com.utils.CommonPromptDialogHelper
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.CaptureStrategy
import kotlinx.android.synthetic.main.fgt_return.*
import me.yokeyword.fragmentation.SupportFragment
import org.json.JSONObject
import wongxd.Wongxd
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.getTime
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.utilcode.util.ScreenUtils
import wongxd.utils.utilcode.util.SizeUtils
import java.io.File
import java.lang.ref.WeakReference

/**
 * Created by wongxd on 2018/11/23.
 */
class FgtReturn : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_return

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "退还")

        tab_return.addTab(QMUITabSegment.Tab("退还"))
            .addTab(QMUITabSegment.Tab("退还记录"))
            .setDefaultSelectedColor(resources.getColor(R.color.app_color))

        tab_return.setDefaultNormalColor(Color.parseColor("#BDBDBD"))

        tab_return.mode = QMUITabSegment.MODE_FIXED
        tab_return.setHasIndicator(true)
        tab_return.notifyDataChanged()
        tab_return.selectTab(0)

        tab_return.addOnTabSelectedListener(object : QMUITabSegment.OnTabSelectedListener {
            override fun onDoubleTap(index: Int) {

            }

            override fun onTabReselected(index: Int) {
            }

            override fun onTabUnselected(index: Int) {
            }

            override fun onTabSelected(index: Int) {
                scrollView_return.visibility = if (index == 0) View.VISIBLE else View.GONE
                srl_reapair_log.visibility = if (index == 1) View.VISIBLE else View.GONE
            }
        })


        initPicView()
        getPermissions(activity, PermissionType.WRITE_EXTERNAL_STORAGE, PermissionType.CAMERA, allGranted = {
            initPicData()
            initRv()
        })


        fl_battery_broke_return.setOnClickListener { setCheckedTag(1) }
        fl_battery_fine_return.setOnClickListener { setCheckedTag(0) }

        btn_yes_return.setOnClickListener { setHostIndex(1) }
        btn_no_return.setOnClickListener { setHostIndex(0) }


        btn_submit_return.setOnClickListener {
            val msg = et_return.text.toString()


            val sb = StringBuilder()
            imgUploadedMap.forEach { item ->
                sb.append(item.value)
                sb.append(",")
            }

            val images = sb.toString()

            http {
                url = Path.RETURN_DEVICE

                params["images"] = if (images.length == 0) {
                    ""
                } else images.substring(0, images.lastIndex)

                params["damage"] = currentTagIndex.toString()
                params["retrun_host"] = hostIndex.toString()
                params["msg"] = msg
                params["device_id"] = FgtHomeBack.CURRENT_DEVICEID
                params["contract_id"] = FgtHomeBack.CURRENT_CONTRACT_ID

                onSuccessWithMsg { res, msg ->
                    EasyToast.DEFAULT.show(msg)
                    resetStatus()
                    srl_reapair_log.autoRefresh()
                }

            }

        }


        getBatteryInfo()

        //报修记录
        srl_reapair_log?.setOnRefreshListener { page = 1;getLog() }
        srl_reapair_log?.setOnLoadMoreListener { getLog() }

        rv_return_log.layoutManager = LinearLayoutManager(activity)
        rv_return_log.adapter = adapter

        getLog()

        btn_no_return.performClick()
        fl_battery_fine_return.performClick()
    }


    private fun startFgt(fgt: SupportFragment) {
        (parentFragment as FgtReturn).start(fgt)
    }

    private var signDialog: CustomDialog? = null
    private fun signDialog(context: Context,contractId:String) {
        signDialog = CustomDialog(context, R.layout.dialog_sign)
        signDialog!!.gravity = Gravity.CENTER
        signDialog!!.setCancelable(false)
        signDialog!!.show()

        signDialog!!.setOnItemClickListener(R.id.confirmBtn) {
            signDialog!!.dismiss()
            startFgt(FgtContractSignStep1.newInstance(
                contractId,
                "",
                0
            ))
        }

    }


    @SuppressLint("SetTextI18n")
    fun getBatteryInfo() {

        http {
            IS_SHOW_MSG = false
            url = "/apiv4/getonedevice"
            params["device_id"] = FgtHomeBack.CURRENT_DEVICEID
            IS_SHOW_MSG = false

            onSuccess {
                val result = it.toPOJO<DeviceDetailBean>().data

                if (1 == result.popmsg.show_msg) {
                    CommonPromptDialogHelper.promptCommonDialog(
                        activity!!,
                        "",
                        result.popmsg.msg,
                        "",
                        "",
                        true,
                        false,
                        false,
                        true,
                        object : CommonDialogCallBackHelper {
                            override fun back(viewId: Int, msg: String?) {

                            }

                        }
                    )
                }

                if ("0"==result.device_contract.is_sign){
                   signDialog(activity!!,result.device_contract.contract_id)
                }

                tv_model_return.text = "电池型号：" + result.device_base.devicenum
                tv_deposit_return.text = "押金：" + result.device_contract.deposit
                tv_rent_long_return.text = "租用时长：" + result.device_contract.rent_day + "月"
                tv_rent_start_return.text = "起租时间：" + result.device_contract.rent_time.toLong().getTime(false)
                tv_u_return.text = "电压：" + result.device_base.totalvoltage + "V"


                tv_pay_money_return.text = "电池：" + result.device_contract.total_rent_money + "元"
                tv_km_return.text = "共行驶：" + "*KM"
                tv_charge_num_return.text = "充电次数：" + result.device_base.loopnum
                tv_energy_return.text = "电量：" + result.device_base.rsoc + "%"
                tv_wh_return.text = "功率：" + "*W"


            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show("获取电池信息失败，无法进行后续操作")
                pop()
            }
        }
    }


    private var hostIndex = -1

    /**
     * 设置是否退还车架
     *
     * @param index  是否退还车架 1是0否
     */
    fun setHostIndex(index: Int) {

        hostIndex = index

        fun dealIsChecked(btn: QMUIRoundButton, isChecked: Boolean) {

            val appColor = resources.getColor(R.color.app_color)
            val white = resources.getColor(R.color.white)

            val bg = btn.background as QMUIRoundButtonDrawable
            bg.setStrokeData(SizeUtils.dp2px(1f), ColorStateList.valueOf(appColor))
            bg.setBgData(ColorStateList.valueOf(if (isChecked) appColor else white))

            btn.setTextColor(if (isChecked) white else appColor)


        }

        dealIsChecked(btn_no_return, index == 0)
        dealIsChecked(btn_yes_return, index == 1)
    }

    private var currentTagIndex = -1
    /**
     * 设置选中的tag
     *
     * @param index 0未损坏 1部分损坏 2完全损坏
     */
    fun setCheckedTag(index: Int) {

        currentTagIndex = index

        fun dealIsChecked(
            fl: QMUIRoundFrameLayout,
            tv: TextView,
            iv: ImageView,
            isChecked: Boolean,
            isBroken: Boolean
        ) {

            val appColor = resources.getColor(R.color.app_color)
            val white = resources.getColor(R.color.white)

            val bg = fl.background as QMUIRoundButtonDrawable
            bg.setStrokeData(SizeUtils.dp2px(2f), ColorStateList.valueOf(appColor))
            bg.setBgData(ColorStateList.valueOf(if (isChecked) appColor else white))

            tv.setTextColor(if (isChecked) white else appColor)

            iv.setImageResource(
                if (isChecked) {
                    if (!isBroken)
                        R.drawable.face_smile_white
                    else
                        R.drawable.face_bad_white
                } else {
                    if (!isBroken)
                        R.drawable.face_smile_black
                    else
                        R.drawable.face_bad
                }
            )
        }

        dealIsChecked(fl_battery_broke_return, tv_battery_broke_return, iv_battery_broke_return, index == 1, true)
        dealIsChecked(fl_battery_fine_return, tv_battery_fine_return, iv_battery_fine_return, index == 0, false)
    }


    private val adapter by lazy { RvReturnAdapter() }
    private var page = 1
    private var pageSize = 20

    fun getLog() {

        http {
            url = Path.RETURN_LIST
            params["page"] = page.toString()
            params["pagesize"] = pageSize.toString()

            onFinish {
                srl_reapair_log?.finishRefresh()
                srl_reapair_log?.finishLoadMore()
            }

            onSuccess {
                val result = it.toPOJO<ReturnLogBean>().data

                if (page == 1)
                    adapter.setNewData(result)
                else
                    adapter.addData(result)

                page++
            }
        }

    }


    inner class RvReturnAdapter : BaseQuickAdapter<ReturnLogBean.Data, BaseViewHolder>(R.layout.item_rv_return) {
        override fun convert(helper: BaseViewHolder, item: ReturnLogBean.Data?) {
            bothNotNull(helper, item) { a, b ->

                if (a.layoutPosition % 2 == 0) {
                    a.setBackgroundColor(R.id.ll_bg, context!!.resources.getColor(R.color.bg_gray))
                } else
                    a.setBackgroundColor(R.id.ll_bg, context!!.resources.getColor(R.color.white))

                a.setText(R.id.tv_time, b.created.toLong().getTime())
                a.setText(R.id.tv_type, b.handle_msg)
            }
        }
    }


    override fun onDestroyView() {
        myHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    //图片

    /**
     * 发送成功后，重置数据
     */
    private fun resetStatus() {
        imgUploadedMap.clear()
        getPermissions(activity, PermissionType.WRITE_EXTERNAL_STORAGE, PermissionType.CAMERA, allGranted = {
            initPicData()
            initRv()
        })




        et_return.setText("")

        btn_no_return.performClick()
        fl_battery_fine_return.performClick()

    }

    private fun initPicView() {
        tv = tv_delete_return
        rcvImg = rv_pic_return

    }

    val imgUploadedMap: MutableMap<String, String> = mutableMapOf()

    fun doPostImg() {

        val pd = ProgressDialog(activity)
        pd.setTitle("处理图片中")
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.setCanceledOnTouchOutside(false)
        pd.setCancelable(false)
        pd.show()

        originImages.forEach { imgPath ->
            if (!imgUploadedMap.values.contains(imgPath) && !imgPath.contains(getString(R.string.glide_plus_icon_string))) {
                http {
                    url = Path.UPLOAD_IMG
                    imgs["file"] = File(imgPath)

                    onSuccess {

                        /**
                         * {
                        "errcode":200,
                        "errmsg":"",
                        "data":{
                        "string":"http://cdnimg.qiniu.hr999999.com/201811021101454287.jpg"
                        }
                        }
                         */

                        val json = JSONObject(it)
                        val data = json.optJSONObject("data")
                        val imgUrl = data.optString("string")
                        imgUploadedMap[imgPath] = imgUrl
                    }

                    onFail { code, msg ->
                        EasyToast.DEFAULT.show("处理图片失败，请重试")
                        originImages.remove(imgPath)
                        postArticleImgAdapter.notifyDataSetChanged()
                        rcvImg.scrollToPosition(postArticleImgAdapter.itemCount - 1)
                    }

                    onFinish {
                        pd.dismiss()
                    }
                }
            }
        }
    }

    companion object {
        val FILE_DIR_NAME = Wongxd.instance.packageName//应用缓存地址
        val FILE_IMG_NAME = "images"//放置图片缓存
        val REQUEST_IMAGE = 1002
    }


    private var originImages: ArrayList<String> = ArrayList()//原始图片
    private var dragImages: ArrayList<String> = ArrayList()//压缩长宽后图片
    private lateinit var mContext: Context
    private lateinit var postArticleImgAdapter: PostImgAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var rcvImg: RecyclerView
    private lateinit var tv: TextView//删除区域提示

    private lateinit var plusPath: String

    private fun initPicData() {
        originImages = arguments?.getStringArrayList("img") ?: arrayListOf()
        mContext = _mActivity.applicationContext
        //清除图片缓存
        SdcardUtils.deleteDirectory(FILE_DIR_NAME)
        //添加按钮图片资源
        plusPath = getString(R.string.glide_plus_icon_string) + mContext.packageName + "/drawable/" +
                R.drawable.add_pic_return
        dragImages = ArrayList()
        originImages.add(plusPath)//添加按键，超过9张时在adapter中隐藏
        dragImages.addAll(originImages)
//        Thread(MyRunnable(dragImages, originImages, dragImages, myHandler, false)).start()//开启线程，在新线程中去压缩图片
    }


    private val lis by lazy {
        object : OnRecyclerItemClickListener(rcvImg) {

            override fun onItemClick(vh: RecyclerView.ViewHolder) {
                if (originImages.get(vh.adapterPosition).contains(getString(R.string.glide_plus_icon_string))) {//打开相册

                    Matisse.from(this@FgtReturn)
                        .choose(MimeType.allOf())
                        .capture(true)
                        .captureStrategy(
                            CaptureStrategy(
                                true,
                                Wongxd.instance.packageName + ".fileprovider"
                            )
                        )
                        .countable(true)
                        .maxSelectable(PostImgAdapter.IMAGE_SIZE - originImages.size + 1)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85f)
                        .imageEngine(PostGlideEngine())
                        .forResult(REQUEST_IMAGE)
                } else {
//                    TU.cT("预览图片")
                    EasyToast.DEFAULT.show("长按并向下拖拽删除此图片")
//                    if (vh.layoutPosition != dragImages.size - 1) {
//                        itemTouchHelper.startDrag(vh)
//                    }
                }
            }

            override fun onItemLongClick(vh: RecyclerView.ViewHolder) {
                //如果item不是最后一个，则执行拖拽
                if (vh.layoutPosition != dragImages.size - 1) {
                    itemTouchHelper.startDrag(vh)
                }
            }
        }
    }


    private fun initRv() {


        postArticleImgAdapter = PostImgAdapter(mContext, dragImages)
//        rcvImg.setLayoutManager(StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL))
        rcvImg.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rcvImg.setAdapter(postArticleImgAdapter)
        val myCallBack = MyCallBack(postArticleImgAdapter, dragImages, originImages)
        itemTouchHelper = ItemTouchHelper(myCallBack)
        itemTouchHelper.attachToRecyclerView(rcvImg)//绑定RecyclerView

        //事件监听
        rcvImg.removeOnItemTouchListener(lis)
        rcvImg.addOnItemTouchListener(lis)

        myCallBack.setDragListener(object : MyCallBack.DragListener {
            override fun clearView() {
            }

            override fun deleteState(delete: Boolean) {
                if (delete) {
                    tv.setBackgroundResource(R.color.app_red)
                    tv.text = resources.getString(R.string.post_delete_tv_s)
                } else {
                    tv.text = resources.getString(R.string.post_delete_tv_d)
                    tv.setBackgroundResource(R.color.app_red)
                }
            }

            override fun dragState(start: Boolean) {
                if (start) {
                    ll_content_return.visibility = View.GONE
                    tv.setVisibility(View.VISIBLE)
                } else {
                    ll_content_return.visibility = View.VISIBLE
                    tv.setVisibility(View.GONE)
                }
            }
        })
    }


    //------------------图片相关-----------------------------

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {//从相册选择完图片

            val arrylist = ArrayList<String>()
            Matisse.obtainResult(data).forEach { uri ->
                //                Logger.e(uri.toString())
                arrylist.add(
                    PostGlideEngine.getAbsoluteImagePath(mContext, uri).replace(
                        "/my_images/",
                        "/storage/emulated/0/"
                    )
                )
            }
            //压缩图片
            Thread(MyRunnable(arrylist, originImages, dragImages, myHandler, true)).start()

//            originImages.addAll(arrylist)
//            dragImages.addAll(originImages)
//            postArticleImgAdapter.notifyDataSetChanged()
        }
    }


    /**
     * 另起线程压缩图片
     */
    internal class MyRunnable(
        var images: ArrayList<String>,
        var originImages: ArrayList<String>,
        var dragImages: ArrayList<String>,
        var handler: Handler,
        var add: Boolean//是否为添加图片
    ) : Runnable {

        override fun run() {
            val sdcardUtils = SdcardUtils()
            var filePath: String
            var newBitmap: Bitmap? = null
            var addIndex = originImages.size - 1
            for (i in images.indices) {
                if (images[i].contains(Wongxd.instance.getString(R.string.glide_plus_icon_string))) {//说明是添加图片按钮
                    continue
                }
                //压缩
                newBitmap = ImageUtils.compressScaleByWH(
                    images[i],
                    ScreenUtils.getScreenWidth(),
                    ScreenUtils.getScreenHeight()
                )
                //文件地址
                filePath = (sdcardUtils.sdpath + FILE_DIR_NAME + "/"
                        + FILE_IMG_NAME + "/" + String.format("img_%d.jpg", System.currentTimeMillis()))
                //保存图片
                ImageUtils.save(newBitmap, filePath, Bitmap.CompressFormat.JPEG, true)
                //设置值
                if (!add) {
                    images[i] = filePath
                } else {//添加图片，要更新
                    dragImages.add(addIndex, filePath)
                    originImages.add(addIndex++, filePath)
                }
            }

            val message = handler.obtainMessage()
            message.what = 1
            handler.sendMessage(message)

            newBitmap?.recycle()
        }
    }

    private val myHandler by lazy { MyHandler(this) }

    @SuppressLint("HandlerLeak")
    private inner class MyHandler(fgt: FgtReturn) : Handler() {
        private val reference: WeakReference<FgtReturn> = WeakReference(fgt)

        override fun handleMessage(msg: Message) {
            val fgt = reference.get()
            if (fgt != null) {
                when (msg.what) {
                    1 -> {
                        doPostImg()
                        fgt.postArticleImgAdapter?.notifyDataSetChanged()
                        fgt.rcvImg.scrollToPosition(fgt.postArticleImgAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

}