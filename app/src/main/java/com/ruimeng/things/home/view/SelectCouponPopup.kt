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
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemChildClickListener
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.CouponsInfoBean
import com.ruimeng.things.me.bean.MyCouponBean
import wongxd.common.bothNotNull

class SelectCouponPopup (private val activity: Activity,
                         private val coupons:MutableList<CouponsInfoBean>,
                         private var selectId : Int,
                         private val listener:OnCouponSelect

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
        adapter.setOnItemChildClickListener(object :OnItemChildClickListener{
            override fun onItemChildClick(p0: BaseQuickAdapter<*, *>?, p1: View?, p2: Int) {
                if (p1 != null) {
                    if (p1.id == R.id.cl_info){
                        coupons.get(p2).expond = !coupons.get(p2).expond

                    }else if (p1.id == R.id.iv_select){
                        if (selectId == coupons.get(p2).id){
                            selectId = 0
                        }else{
                            selectId = coupons.get(p2).id
                        }
                        listener.selectId(selectId,coupons.get(p2).coupon_label)
                    }
                    adapter.notifyDataSetChanged()
                }
            }

        })
        show(activity.window.decorView)

    }

    interface OnCouponSelect{
       fun  selectId(id:Int,label:String)
    }
    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.BOTTOM, 0, 0)
        }
    }
    inner class CouponSelectAdapter : BaseQuickAdapter<CouponsInfoBean, BaseViewHolder>(R.layout.item_coupon_select) {
        override fun convert(p0: BaseViewHolder, p1: CouponsInfoBean?) {
            bothNotNull(p0, p1) { a, b ->
                a.setText(R.id.tv_money,"¥"+ b.coupon_price)
                    .setText(R.id.tv_limit,b.limit_day)
                    .setImageResource(R.id.iv_select,if (selectId == b.id) R.mipmap.ic_radio_select else R.mipmap.ic_radio_unselect)
                    .setText(R.id.tv_time,"有效期至：${b.exp_time}")
                a.setGone(R.id.cl_time,b.expond)
                a.setBackgroundRes(R.id.ll_content,if (b.expond) R.mipmap.bg_ticket_me_big else R.mipmap.bg_ticket_me)
                a.addOnClickListener(R.id.cl_info)
                a.addOnClickListener(R.id.iv_select)
            }
        }

    }
}