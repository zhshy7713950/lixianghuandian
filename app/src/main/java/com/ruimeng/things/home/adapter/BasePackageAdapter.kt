package com.ruimeng.things.home.adapter

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.NewGetRentBean

class BasePackageAdapter :BaseQuickAdapter<NewGetRentBean.Data,BaseViewHolder>(R.layout.item_rv_rent_long_pay_rent_money) {
    var selectPos = 0
    override fun convert(p0: BaseViewHolder,item: NewGetRentBean.Data?) {
        if (item != null){
            p0.setText(R.id.tv_price,"¥"+item.basePrice)
                .setText(R.id.tv_name, item.sname);
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