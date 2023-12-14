package com.ruimeng.things.me.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.SharePosterBean
import com.ruimeng.things.me.bean.SharePosterListBean
import com.utils.*
import kotlinx.android.synthetic.main.activity_share_poster.*
import wongxd.base.AtyBase
import wongxd.common.toPOJO
import wongxd.http


class SharePosterActivity : AtyBase() {

    private var getCurrentPosition = 0
    private var mAdapter: SharePosterAdapter? = null
    private var dataList = ArrayList<SharePosterListBean.Data>()


    companion object {
        lateinit var mActivity: SharePosterActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_poster)
        initView()
        setListener()
        initData()
    }

    private fun initView() {
        mActivity = this
    }

    private fun setListener() {
        copyUrlLayout?.setOnClickListener {
            if (dataList.isNotEmpty()) {
                requestSharePoster(dataList[getCurrentPosition].id,"copy")
            }
        }
        shareLayout?.setOnClickListener {
            if (dataList.isNotEmpty()) {
                requestSharePoster(dataList[getCurrentPosition].id,"share")
            }
        }
    }

    private fun initData() {
        initTopbar(topbar, "分享赚收益")
        requestSharePosterList()
    }

    private fun initViewPager(dataList: List<SharePosterListBean.Data>) {
        mAdapter = SharePosterAdapter(mActivity, dataList)
        viewPager?.setPageTransformer(false, ScaleTransformer(mActivity))
        viewPager?.offscreenPageLimit = 2
        viewPager?.pageMargin = DensityUtil.dip2px(40f, mActivity)
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                getCurrentPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        viewPager?.currentItem = 0
        viewPager?.adapter = mAdapter
    }

    @Suppress("UNCHECKED_CAST")
    private fun requestSharePosterList() {
        http {
            url = "apiv5/shareposetlist"
            params["appType"] = "lxhd"
            onSuccess {
                val result = it.toPOJO<SharePosterListBean>().data
                if (result.isNotEmpty()) {
                    dataList.clear()
                    dataList.addAll(result)
                    initViewPager(dataList)
                }
            }

            onFinish {

            }

            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }

        }


    }

    private fun requestSharePoster(posterId: String,clickType: String) {
        http {
            url = "apiv5/shareposter"
            params["poster_id"] = posterId
            params["appType"] = "lxhd"
            onSuccess {
                val result = it.toPOJO<SharePosterBean>().data
                if ("copy"==clickType){
                    ClipboardManagerHelper.copy(mActivity, result.reg_url, "复制成功", "复制失败")
                }else{
                    CommonShareDialogHelper.commonShareDialog(mActivity,result.img_url,object :CommonDialogCallBackHelper{
                        override fun back(viewId: Int, imageUrl: String?) {
                            if (viewId==R.id.shareWeChatLayout){
                                WeChatHelper.weChatShareImage(mActivity,getString(R.string.wx_appid),0,imageUrl.toString())
                            }else if (viewId==R.id.shareFriendsLayout){
                                WeChatHelper.weChatShareImage(mActivity,getString(R.string.wx_appid),1,imageUrl.toString())
                            }
                        }
                    })
                }
            }
            onFinish {

            }

            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }
        }
    }

    inner class SharePosterAdapter(
        private val context: Context,
        private val dataList: List<SharePosterListBean.Data>
    ) : PagerAdapter() {
        override fun getCount(): Int {
            return dataList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        // PagerAdapter只缓存三张要显示的图片，如果滑动的图片超出了缓存的范围，就会调用这个方法，将图片销毁
        override fun destroyItem(view: ViewGroup, position: Int, `object`: Any) {
            view.removeView(`object` as View)
        }

        // 当要显示的图片可以进行缓存的时候，会调用这个方法进行显示图片的初始化，我们将要显示的ImageView加入到ViewGroup中，然后作为返回值返回即可
        override fun instantiateItem(container: ViewGroup, position: Int): Any { //getView
            val view = View.inflate(context, R.layout.item_share_poster, null)
            val imageView = view.findViewById<ImageView>(R.id.imageView)

                GlideHelper.loadImage(context, imageView, dataList[position].img)

                //获取图片真正的宽高
                //获取图片真正的宽高
//                val options = RequestOptions()
//                    .dontAnimate()
//                Glide.with(context)
//                    .asBitmap()
//                    .load(dataList[position].img)
//                    .apply(options)
//                    .into(object : SimpleTarget<Bitmap?>() {
//                        override fun onResourceReady(
//                            resource: Bitmap,
//                            transition: Transition<in Bitmap?>?
//                        ) {
//                            val width = resource.width
//                            val height = resource.height
//                            LogHelper.i("data===", "===width===${width}")
//                            LogHelper.i("data===", "===height===${height}")
//                        }
//                    })
//

            //添加到容器中
            container.addView(view)


            return view
        }

    }


//    private var commonShareDialog: CustomDialog? = null
//    private fun commonShareDialog(context: Context, imageUrl: String) {
//        commonShareDialog = CustomDialog(context, R.layout.dialog_common_share)
//        commonShareDialog?.gravity = Gravity.BOTTOM
//        commonShareDialog?.show()
//
//        commonShareDialog?.setOnItemClickListener(R.id.shareWeChatLayout) {
//            commonShareDialog?.dismiss()
//            weChatShareImage(mActivity, "getString(R.string.wx_appid)", 0, imageUrl)
//        }
//        commonShareDialog?.setOnItemClickListener(R.id.shareFriendsLayout) {
//            commonShareDialog?.dismiss()
//            weChatShareImage(mActivity, "getString(R.string.wx_appid)", 1, imageUrl)
//        }
//        commonShareDialog?.setOnItemClickListener(R.id.shareCancelLayout) {
//            commonShareDialog?.dismiss()
//        }
//    }

//    var mIWXAPI: IWXAPI? = null
//    @Suppress("DEPRECATION")
//    private fun weChatShareImage(context: Context?, appId: String, isWeChat: Int, imageUrl: String) {
//        // 微信OpenAPI访问入口，通过WXAPIFactory创建实例
//        mIWXAPI = WXAPIFactory.createWXAPI(context, appId, true)
//        // 将应用的AppId注册到微信
//        mIWXAPI?.registerApp(appId)
//        if (mIWXAPI?.isWXAppInstalled!!) {
//            val options = RequestOptions()
//                .dontAnimate()
//            Glide.with(context!!)
//                .asBitmap()
//                .load(if (TextUtils.isEmpty(imageUrl)) R.drawable.ic_launcher else imageUrl)
//                .apply(options)
//                .into(object : SimpleTarget<Bitmap>() {
//                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                        resource.let {
//                            val wXImageObject = WXImageObject(it)
//                            val wXMediaMessage = WXMediaMessage(wXImageObject)
//                            val thmbBmp = Bitmap.createScaledBitmap(it, 150, 150, true)
//                            it.recycle()
//                            wXMediaMessage.setThumbImage(thmbBmp)
//                            val req = SendMessageToWX.Req()
//                            req.transaction = "img"
//                            req.message = wXMediaMessage
//                            req.scene = if (isWeChat == 0) SendMessageToWX.Req.WXSceneSession
//                            else
//                                SendMessageToWX.Req.WXSceneTimeline
//                            mIWXAPI?.sendReq(req)
//                        }
//                    }
//                })
//        } else {
//            ToastHelper.shortToast(mActivity, "未发现微信客户端")
//        }
//    }

}