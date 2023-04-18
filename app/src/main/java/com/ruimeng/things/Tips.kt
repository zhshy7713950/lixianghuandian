package com.ruimeng.things

import android.app.Activity
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog

/**
 * Created by wongxd on 2020/1/9.
 */

fun showTipDialog(
    aty: Activity?,
    title: String = "提示",
    msg: String,
    clickText: String = "确定",
    click: () -> Unit = {}
) {
    aty ?: return
    NormalDialog(aty)
        .apply {
            style(NormalDialog.STYLE_TWO)
            btnNum(1)
            title(title)
            content(msg)
            btnText(clickText)
            setOnBtnClickL(OnBtnClickL {
                dismiss()
                click.invoke()
            })

        }.show()
}


fun showConfirmDialog(
    aty: Activity?,
    title: String = "提示",
    msg: String,
    dismissText: String = "取消",
    dismissClick: () -> Unit = {},
    confirmText: String = "确定",
    confirmClick: () -> Unit = {}
) {
    aty ?: return
    NormalDialog(aty)
        .apply {
            style(NormalDialog.STYLE_TWO)
            btnNum(2)
            title(title)
            content(msg)
            btnText(confirmText, dismissText)
            setOnBtnClickL(OnBtnClickL {
                dismiss()
                confirmClick.invoke()
            }, OnBtnClickL {
                dismiss()
                dismissClick.invoke()
            })

        }.show()
}