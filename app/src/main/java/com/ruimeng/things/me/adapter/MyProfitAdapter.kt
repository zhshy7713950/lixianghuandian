package com.ruimeng.things.me.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.MyInComeBean


class MyProfitAdapter (data: List<MyInComeBean.Data.LiatData>?) :
    BaseQuickAdapter<MyInComeBean.Data.LiatData, BaseViewHolder>(R.layout.item_my_profit, data) {

    override fun convert(holper: BaseViewHolder, bean: MyInComeBean.Data.LiatData) {

        holper.setText(R.id.titleTextView,bean.explain)
        holper.setText(R.id.timeTextView,bean.created)
        holper.setText(R.id.balanceTextView,"+￥${bean.balance}")

    }

}