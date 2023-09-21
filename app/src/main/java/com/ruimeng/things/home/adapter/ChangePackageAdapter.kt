package com.ruimeng.things.home.adapter

import android.text.TextUtils
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.NewGetRentBean
import com.ruimeng.things.home.bean.PaymentOption

class ChangePackageAdapter :BaseQuickAdapter<PaymentOption,BaseViewHolder>(R.layout.item_rv_rent_long_pay_change) {
    var selectPos = 0
    override fun convert(p0: BaseViewHolder, item: PaymentOption?) {
        if (item != null ){
            p0.setText(R.id.tv_price,"¥"+item.price)
                .setText(R.id.tv_name, "换电"+item.change_times+"次")
                .setVisible(R.id.tv_price,!TextUtils.isEmpty(item.id))
                .setVisible(R.id.no_pay,TextUtils.isEmpty(item.id))
                .setText(R.id.no_pay, item.name)
                .setVisible(R.id.tv_name,!TextUtils.isEmpty(item.id))
            if (p0.layoutPosition == selectPos){
                p0.setBackgroundRes(R.id.item_bg,R.drawable.rectangle_gray_bg_1)
                p0.setVisible(R.id.iv_select,true)
            }else{
                p0.setBackgroundRes(R.id.item_bg,R.drawable.rectangle_gray_bg)
                p0.setVisible(R.id.iv_select,false)
            }
            if (p0.layoutPosition % 2 ==0){
                val layoutParam = p0.itemView.layoutParams as ViewGroup.MarginLayoutParams
                val marginInPixels = p0.itemView.context.resources.getDimensionPixelSize(R.dimen.packageLeft)
                layoutParam.leftMargin = marginInPixels
                p0.itemView.layoutParams = layoutParam
            }else{
                val layoutParam = p0.itemView.layoutParams as ViewGroup.MarginLayoutParams
                val marginInPixels = p0.itemView.context.resources.getDimensionPixelSize(R.dimen.packageLeft)
                layoutParam.rightMargin = marginInPixels
                p0.itemView.layoutParams = layoutParam
            }
        }
    }



}