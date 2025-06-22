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
        adapter.onItemChildClickListener =
            OnItemChildClickListener { p0, p1, p2 ->
                if (p1 != null) {
                    if (p1.id == R.id.cl_coupon_info){
                        coupons[p2].expond = !coupons[p2].expond

                    }else if (p1.id == R.id.iv_select){
                        selectId = if (selectId == coupons[p2].id){
                            0
                        }else{
                            coupons[p2].id
                        }
                        listener.selectId(selectId, coupons[p2].coupon_label)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
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
    inner class CouponSelectAdapter : BaseQuickAdapter<CouponsInfoBean, BaseViewHolder>(R.layout.item_rv_ticket) {
        override fun convert(p0: BaseViewHolder, p1: CouponsInfoBean?) {
            bothNotNull(p0, p1) { a, b ->
                a.setText(R.id.tv_money,b.coupon_price)
                    .setText(R.id.tv_limit,"${b.act_time}~${b.exp_time}")
                    .setBackgroundRes(R.id.cl_coupon,if(b.expond) R.drawable.bg_ticket_me else R.drawable.bg_ticket_unuse)
                    .setText(R.id.tv_coupon_name, "${b.coupon_category}")
                    .setText(R.id.tv_coupon_type, "优惠类型：${b.coupon_type}")
                    .setText(R.id.tv_app_type, "适用品牌：${b.app_type}")
                    .setText(R.id.tv_limit_city, "适用城市：${b.limit_city}")
                    .setText(R.id.tv_limit_voltage, "适用伏数：${b.limit_voltage}")
                    .setText(R.id.tv_limit_day_desc, "适用天数：${b.limit_day_desc}")
                    .setText(R.id.tv_act_time, "生效时间：${b.act_time}")
                    .setText(R.id.tv_exp_time, "过期时间：${b.exp_time}")
                    .setGone(R.id.cl_time, b.expond)
                    .setVisible(R.id.iv_select,true)
                    .setVisible(R.id.tv_use,false)
                    .setImageResource(R.id.iv_select,if (selectId == b.id) R.mipmap.ic_radio_select else R.mipmap.ic_radio_unselect)
                    .addOnClickListener(R.id.cl_coupon_info)
                    .addOnClickListener(R.id.iv_select)
            }
        }

    }
}