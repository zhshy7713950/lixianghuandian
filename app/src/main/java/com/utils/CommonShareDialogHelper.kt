package com.utils

import android.content.Context
import android.view.Gravity
import com.ruimeng.things.CustomDialog
import com.ruimeng.things.R


object CommonShareDialogHelper {

    private var mDialog: CustomDialog? = null

     fun commonShareDialog(
        context: Context,
        imageUrl: String,
        commonDialogCallBackHelper: CommonDialogCallBackHelper
    ) {
        mDialog = CustomDialog(context, R.layout.dialog_common_share)
        mDialog?.gravity = Gravity.BOTTOM
        mDialog?.show()

        mDialog?.setOnItemClickListener(R.id.shareWeChatLayout) {
            mDialog?.dismiss()
            commonDialogCallBackHelper.back(R.id.shareWeChatLayout, imageUrl)
        }
        mDialog?.setOnItemClickListener(R.id.shareFriendsLayout) {
            mDialog?.dismiss()
            commonDialogCallBackHelper.back(R.id.shareFriendsLayout, imageUrl)
        }
        mDialog?.setOnItemClickListener(R.id.shareCancelLayout) {
            mDialog?.dismiss()
        }
    }

}