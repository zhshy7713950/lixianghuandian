package com.ruimeng.things.home.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.NewGetRentBean

class ChangePackageAdapter :BaseQuickAdapter<NewGetRentBean.Data.Option,BaseViewHolder>(R.layout.item_rv_rent_long_pay_change) {
    var selectPos = 0
    override fun convert(p0: BaseViewHolder, item: NewGetRentBean.Data.Option?) {
        if (item != null ){
            p0.setText(R.id.tv_price,"¥"+item.price)
                .setText(R.id.tv_name, "换电"+item.change_times+"次")
                .setVisible(R.id.tv_price,item.name != "")
                .setVisible(R.id.no_pay,item.name == "")
                .setVisible(R.id.tv_name,item.name != "")
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