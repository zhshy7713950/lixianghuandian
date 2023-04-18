package com.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import com.ruimeng.things.CustomDialog
import com.ruimeng.things.R

object CommonWithdrawalTypeChoiceDialogHelper {

    private var mDialog: CustomDialog? = null
    private var getPayType = "alipay"

    private var aliPayLayout: LinearLayout? = null
    private var aliPayImage: ImageView? = null
    private var weChatLayout: LinearLayout? = null
    private var weChatImage: ImageView? = null

    @SuppressLint("SetTextI18n")
    fun commonWithdrawalTypeChoiceDialog(
        context: Context?,
        payType: String,
        commonDialogCallBackHelper: CommonDialogCallBackHelper
    ) {
        mDialog = CustomDialog(context, R.layout.dialog_common_withdrawal_type_choice)
        mDialog?.gravity = Gravity.BOTTOM
        mDialog?.show()


        aliPayLayout = mDialog?.getView(R.id.aliPayLayout) as LinearLayout
        aliPayImage = mDialog?.getView(R.id.aliPayImage) as ImageView
        weChatLayout = mDialog?.getView(R.id.weChatLayout) as LinearLayout
        weChatImage = mDialog?.getView(R.id.weChatImage) as ImageView

        getPayType = payType
        setPayState(getPayType)

        aliPayLayout?.setOnClickListener {
            setPayState("alipay")
        }
        weChatLayout?.setOnClickListener {
            setPayState("wxpay")
        }

        mDialog?.setOnItemClickListener(R.id.confirmBtn) {
            mDialog?.dismiss()
            if (!TextUtils.isEmpty(getPayType)) {
                commonDialogCallBackHelper.back(R.id.confirmBtn, getPayType)
            } else {
                ToastHelper.shortToast(context, "请选择提现方式")
            }
        }
    }


    private fun setPayState(type: String) {
        getPayType = when (type) {
            "alipay" -> {
                aliPayImage?.setBackgroundResource(R.mipmap.withdrawal_type_image_checked)
                weChatImage?.setBackgroundResource(R.mipmap.withdrawal_type_image_normal)
                "alipay"
            }
            "wxpay" -> {
                aliPayImage?.setBackgroundResource(R.mipmap.withdrawal_type_image_normal)
                weChatImage?.setBackgroundResource(R.mipmap.withdrawal_type_image_checked)
                "wxpay"
            }
            else -> {
                ""
            }
        }
    }

}