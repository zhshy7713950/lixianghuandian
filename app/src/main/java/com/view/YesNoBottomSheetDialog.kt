package com.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ruimeng.things.R

class YesNoBottomSheetDialog(context: Context, private val callback: YesNoDialogCallback) {

    private val dialog: BottomSheetDialog = BottomSheetDialog(context,R.style.TransparentDialog)

    init {
        // 设置底部选择器的内容视图
        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_yes_no, null)
        dialog.setContentView(view)

        // 获取“是”按钮和“否”按钮
        val yesButton = view.findViewById<TextView>(R.id.yesButton)
        val noButton = view.findViewById<TextView>(R.id.noButton)

        // 设置“是”按钮的点击事件
        yesButton.setOnClickListener {
            callback.onClickedItem(yesButton.text.toString())
            dismiss()
        }
        view.findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        // 设置“否”按钮的点击事件
        noButton.setOnClickListener {
            callback.onClickedItem(noButton.text.toString())
            dismiss()
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
     interface YesNoDialogCallback{
       fun onClickedItem(item:String)
    }
}
