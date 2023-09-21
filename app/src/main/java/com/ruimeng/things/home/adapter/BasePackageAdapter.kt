package com.ruimeng.things.home.adapter

import android.graphics.Paint
import android.text.TextUtils
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.NewGetRentBean
import com.ruimeng.things.home.bean.PaymentInfo


class BasePackageAdapter :BaseQuickAdapter<PaymentInfo,BaseViewHolder>(R.layout.item_rv_rent_long_pay_rent_money) {
    var selectPos = 0
    override fun convert(p0: BaseViewHolder,item: PaymentInfo?) {
        if (item != null){
            p0.setText(R.id.tv_price,"¥"+item.price)
                .setText(R.id.tv_name, item.pname)
                .setText(R.id.tv_base_price,"¥"+item.basePrice)
                .setVisible(R.id.tv_base_price,!TextUtils.isEmpty(item.id) && item.gdiscount != "100")
                .setVisible(R.id.tv_no_package,TextUtils.isEmpty(item.id))
                .setText(R.id.tv_no_package, item.pname)
                .setVisible(R.id.tv_price,!TextUtils.isEmpty(item.id))
                .setVisible(R.id.tv_name,!TextUtils.isEmpty(item.id))
            val textView = p0.getView<TextView>(R.id.tv_base_price)
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            if (p0.layoutPosition == selectPos){
                p0.setBackgroundRes(R.id.item_bg,R.drawable.rectangle_gray_bg_1)
                p0.setVisible(R.id.iv_select,true)
            }else{
                p0.setBackgroundRes(R.id.item_bg,R.drawable.rectangle_gray_bg)
                p0.setVisible(R.id.iv_select,false)
            }
        }

    }

}