package com.ruimeng.things.share

import android.os.Bundle
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import com.ruimeng.things.R
import com.utils.ShareData
import com.utils.WeChatHelper
import kotlinx.android.synthetic.main.fgt_share_layout.btnShareApp
import kotlinx.android.synthetic.main.fgt_share_layout.btnSharePic
import wongxd.base.BaseBackFragment

class FgtShare : BaseBackFragment() {

    companion object {
        fun newInstance(): FgtShare {
            return FgtShare()
        }
    }

    override fun getLayoutRes() = R.layout.fgt_share_layout

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "享锂来祝您新春快乐")

        btnSharePic.setOnClickListener {
            showSharePicChoose()
        }

        btnShareApp.setOnClickListener {
            WeChatHelper.weChatShareApp(requireContext(), ShareData(
                requireContext().resources.getString(R.string.wx_appid),
                shareUrl = "https://www.scxll.cn/",
                shareTitle = "蛇舞新春",
                description = "享锂来祝您春节愉快，阖家幸福！",
                thumbImgId = R.drawable.spring_festival_thumb
            ))
        }
    }

    private fun showSharePicChoose(){
        QMUIBottomSheet.BottomListSheetBuilder(activity).also {
            it.addItem("原图分享","origin")
            it.addItem("分享+二维码","lxhd")
            it.setOnSheetItemClickListener{dialog, _, _, tag ->
                when(tag){
                    "origin" ->{
                        WeChatHelper.weChatShareImage(requireContext(),requireContext().resources.getString(R.string.wx_appid),true,R.drawable.spring_festival_origin)
                    }
                    "lxhd" -> {
                        WeChatHelper.weChatShareImage(requireContext(),requireContext().resources.getString(R.string.wx_appid),true,R.drawable.spring_festival_xll)
                    }
                }
                dialog.dismiss()
            }
            it.build().show()
        }
    }
}