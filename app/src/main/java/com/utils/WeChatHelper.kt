package com.utils

import android.R.attr.path
import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.ruimeng.things.R
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory


object WeChatHelper {

    fun launchWXMiniProgram(context: Context?, appId: String) {
        mIWXAPI = WXAPIFactory.createWXAPI(context, appId, true)
        val req = WXLaunchMiniProgram.Req()
        req.userName = "gh_5495b84a5dcd" // 填小程序原始id
        req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE // 可选打开 开发版，体验版和正式版
        mIWXAPI?.sendReq(req)
    }

    var mIWXAPI: IWXAPI? = null

    @Suppress("DEPRECATION")
    fun weChatShareImage(context: Context?, appId: String, isWeChat: Int, imageUrl: String) {
        // 微信OpenAPI访问入口，通过WXAPIFactory创建实例
        mIWXAPI = WXAPIFactory.createWXAPI(context, appId, true)
        // 将应用的AppId注册到微信
        mIWXAPI?.registerApp(appId)
        if (mIWXAPI?.isWXAppInstalled!!) {
            val options = RequestOptions()
                .dontAnimate()
            Glide.with(context!!)
                .asBitmap()
                .load(if (TextUtils.isEmpty(imageUrl)) R.drawable.ic_launcher else imageUrl)
                .apply(options)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        resource.let {
                            val wXImageObject = WXImageObject(it)
                            val wXMediaMessage = WXMediaMessage(wXImageObject)
                            val thmbBmp = Bitmap.createScaledBitmap(it, 150, 150, true)
                            it.recycle()
                            wXMediaMessage.setThumbImage(thmbBmp)
                            val req = SendMessageToWX.Req()
                            req.transaction = "img"
                            req.message = wXMediaMessage
                            req.scene = if (isWeChat == 0) SendMessageToWX.Req.WXSceneSession
                            else
                                SendMessageToWX.Req.WXSceneTimeline
                            mIWXAPI?.sendReq(req)
                        }
                    }
                })
        } else {
            ToastHelper.shortToast(context, "未发现微信客户端")
        }
    }

}