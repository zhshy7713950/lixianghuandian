package com.ruimeng.things.me.activity

import android.os.Bundle
import android.widget.ImageView
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.ShareInfoBean
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.xianglilai.lixianghuandian.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fgt_share.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import wongxd.base.AtyBase
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.ShareThing
import wongxd.common.loadImg
import wongxd.common.toPOJO
import wongxd.http


class ShareFriendActivity : AtyBase() {


    companion object {
        lateinit var mActivity: ShareFriendActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fgt_share)
        mActivity = this
        initTopbar(topbar, "分享好友")

        getInfo()
    }

    private fun getInfo() {

        http {
            url = "/api/shareinfo"

            onSuccess {
                val result = it.toPOJO<ShareInfoBean>().data

                iv_share.loadImg(result.share_bgimg)

                doAsync {
                    val png = WXEntryActivity.getLocalOrNetBitmap(result.share_url_qr)
                    uiThread {
                        val obj: WXMediaMessage.IMediaObject = if (result.share_url.isNotBlank()) {
                            WXWebpageObject().apply {
                                this.webpageUrl = result.share_url
                            }
                        } else {
                            WXImageObject(png)
                        }



                        try {
                            ll_wechat_share.setOnClickListener { _ ->

                                doAsync {
                                    val bmp = WXEntryActivity.getLocalOrNetBitmap(result.share_ico)
                                    uiThread {
                                        WXEntryActivity.wxShare(
                                            mActivity,
                                            obj,
                                            result.share_title,
                                            result.share_bgimg,
                                            bmp,
                                            false
                                        )
                                        bmp?.recycle()
                                    }

                                }


                            }


                            ll_qq_share.setOnClickListener {
                                result.share_url?.let {
                                    ShareThing.shareText(result.share_url)
                                }
                            }

                            ll_qrcode_share.setOnClickListener {

                                if (result.share_url_qr.isNotBlank()) {

                                    mActivity?.let { aty ->

                                        AnyLayer.with(aty).contentView(R.layout.dialog_qrcode_share)
                                            .bindData { anyLayer ->
                                                val iv =
                                                    anyLayer.contentView.findViewById<ImageView>(R.id.iv)
                                                iv.loadImg(result.share_url_qr)
                                            }.backgroundBlurRadius(8f)
                                            .backgroundBlurScale(8f)
                                            .show()
                                    }
                                } else
                                    EasyToast.DEFAULT.show("没有二维码数据")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            EasyToast.DEFAULT.show("尝试分享失败，可能是没有获取到分享信息")
                        }

                        png?.recycle()
                    }

                }


            }
        }
    }

}