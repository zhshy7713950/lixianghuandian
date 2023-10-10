package com.ruimeng.things.home.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.MyCouponBean
import wongxd.common.bothNotNull

class CompanyDescPopup (private val activity: Activity,private val title:String,private val text:String

) : PopupWindow(activity)  {
    init {
        contentView = View.inflate(activity, R.layout.popup_company_descl, null)
        val titleView = contentView.findViewById<TextView>(R.id.tv_title)
        val contentText = contentView.findViewById<TextView>(R.id.tv_content)
        titleView.text = title
        contentText.text = text
        contentView.setOnClickListener { dismiss() }
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        contentView.findViewById<View>(R.id.ll_01).setOnClickListener{

        }
        contentView.findViewById<View>(R.id.iv_close).setOnClickListener{
            dismiss()
        }
        setBackgroundDrawable(ColorDrawable(0x55000000))
        show(activity.window.decorView)

    }

    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.BOTTOM, 0, 0)
        }
    }
}