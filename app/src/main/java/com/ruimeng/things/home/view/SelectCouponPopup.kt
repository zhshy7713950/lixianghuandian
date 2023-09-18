package com.ruimeng.things.home.view

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.MyCouponBean
import wongxd.common.bothNotNull

class SelectCouponPopup (private val activity: Activity,
                         private val coupons:MutableList<MyCouponBean.Data>,
                         private val selectId : Int,
                         private val listener: BaseQuickAdapter.OnItemClickListener

) : PopupWindow(activity)  {
    init {
        contentView = View.inflate(activity, R.layout.popup_select_coupon, null)
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
        val rvCoupon = contentView.findViewById<RecyclerView>(R.id.rv_coupon)
        rvCoupon.layoutManager = LinearLayoutManager(activity)
        var adapter = CouponSelectAdapter()
        adapter.setNewData(coupons)
        rvCoupon.adapter = adapter
        adapter.setOnItemClickListener { baseQuickAdapter, view, i ->
            run {
                listener.onItemClick(baseQuickAdapter, view, i)
                dismiss()
            }
        }

        show(activity.window.decorView)

    }

    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.BOTTOM, 0, 0)
        }
    }
    inner class CouponSelectAdapter : BaseQuickAdapter<MyCouponBean.Data, BaseViewHolder>(R.layout.item_coupon_select) {
        override fun convert(p0: BaseViewHolder, p1: MyCouponBean.Data?) {
            bothNotNull(p0, p1) { a, b ->
                a.setText(R.id.tv_money,"¥"+ b.coupon_price)
                    .setText(R.id.tv_limit,b.limit_day)
                    .setImageResource(R.id.iv_select,if (selectId == b.id) R.mipmap.ic_radio_select else R.mipmap.ic_radio_unselect)

            }
        }

    }
}