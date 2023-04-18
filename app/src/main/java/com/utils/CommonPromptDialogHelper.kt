package com.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.ruimeng.things.CustomDialog
import com.ruimeng.things.R


@SuppressLint("StaticFieldLeak")
object CommonPromptDialogHelper {

    private var promptCommonDialog: CustomDialog? = null
    private var titleText: TextView? = null
    private var contentText: TextView? = null
    private var cancelBtn: TextView? = null
    private var confirmBtn: TextView? = null
    private var topLine: View? = null
    private var centerLine: View? = null
    fun promptCommonDialog(
        context: Context,
        getTitle: String,
        getContent: String,
        leftBtnName: String,
        rightBtnName: String,
        isShowTopLine: Boolean,
        isShowCenterLine: Boolean,
        isShowLeftBtn: Boolean,
        isShowRightBtn: Boolean,
        commonDialogCallBackHelper: CommonDialogCallBackHelper
    ) {
        promptCommonDialog = CustomDialog(context, R.layout.dialog_prompt_common)
        promptCommonDialog?.gravity = Gravity.CENTER
        promptCommonDialog?.show()

        titleText = promptCommonDialog?.getView(R.id.titleText) as TextView
        contentText = promptCommonDialog?.getView(R.id.contentText) as TextView
        cancelBtn = promptCommonDialog?.getView(R.id.cancelBtn) as TextView
        confirmBtn = promptCommonDialog?.getView(R.id.confirmBtn) as TextView
        topLine = promptCommonDialog?.getView(R.id.topLine) as View
        centerLine = promptCommonDialog?.getView(R.id.centerLine) as View
        if (!TextUtils.isEmpty(getTitle)) {
            titleText?.text = getTitle
        } else {
            titleText?.text = "温馨提示"
        }
        contentText?.text = getContent
        if (!TextUtils.isEmpty(leftBtnName)) {
            cancelBtn?.text = leftBtnName
        } else {
            cancelBtn?.text = "取消"
        }
        if (!TextUtils.isEmpty(rightBtnName)) {
            confirmBtn?.text = rightBtnName
        } else {
            confirmBtn?.text = "确定"
        }
        if (isShowTopLine) {
            topLine?.visibility = View.VISIBLE
        } else {
            topLine?.visibility = View.GONE
        }
        if (isShowCenterLine) {
            centerLine?.visibility = View.VISIBLE
        } else {
            centerLine?.visibility = View.GONE
        }
        if (isShowLeftBtn) {
            cancelBtn?.visibility = View.VISIBLE
        } else {
            cancelBtn?.visibility = View.GONE
        }
        if (isShowRightBtn) {
            confirmBtn?.visibility = View.VISIBLE
        } else {
            confirmBtn?.visibility = View.GONE
        }

        promptCommonDialog?.setOnItemClickListener(R.id.cancelBtn) {
            promptCommonDialog?.dismiss()
            commonDialogCallBackHelper.back(R.id.cancelBtn, cancelBtn?.text.toString())
        }
        promptCommonDialog?.setOnItemClickListener(R.id.confirmBtn) {
            promptCommonDialog?.dismiss()
            commonDialogCallBackHelper.back(R.id.confirmBtn,confirmBtn?.text.toString())
        }
    }

}