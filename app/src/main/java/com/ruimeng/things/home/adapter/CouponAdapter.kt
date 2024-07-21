package com.ruimeng.things.home.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.entity.remote.OperationInnerData
import com.ruimeng.things.R

class CouponAdapter : BaseQuickAdapter<OperationInnerData, BaseViewHolder>(R.layout.item_coupon_purchase) {
    var selectPos = 0
    override fun convert(vh: BaseViewHolder, data: OperationInnerData?) {
        data?.let {
            with(vh){
                setText(R.id.tvMoney,it.price)
                setText(R.id.tvDes,it.description)
                if(selectPos == vh.layoutPosition){
                    setImageResource(R.id.ivCheck,R.mipmap.ic_radio_select)
                }else{
                    setImageResource(R.id.ivCheck,R.mipmap.ic_radio_unselect)
                }
            }
        }
    }
}