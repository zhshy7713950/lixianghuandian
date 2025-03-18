package com.ruimeng.things.home.adapter

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.NewGetRentBean
import com.ruimeng.things.home.bean.PaymentOption
import com.utils.safeToInt

class ChangePackageAdapter(private val margin: Int = R.dimen.packageLeft_24) :BaseQuickAdapter<PaymentOption,BaseViewHolder>(R.layout.item_rv_rent_long_pay_change) {
    var selectPos = 0
    override fun convert(helper: BaseViewHolder, item: PaymentOption?) {
        if (item != null ){
            // 设置套餐名称和价格
            val packageName = when {
                item.change_times.safeToInt() >= 999 -> {
                    "次数无限制"
                }
                else -> "${item.change_times}次换电"
            }

            helper.setText(R.id.tv_name, packageName)
                .setText(R.id.tv_price, "¥${item.price}")
                .setVisible(R.id.tv_price, !TextUtils.isEmpty(item.id))
                .setVisible(R.id.no_pay, TextUtils.isEmpty(item.id))
                .setText(R.id.no_pay, item.name)
                .setVisible(R.id.tv_name, !TextUtils.isEmpty(item.id))

            // 设置差价提示
            if (item.spread > 0) {
                helper.getView<TextView>(R.id.tv_spread_tip).apply {
                    visibility = View.VISIBLE
                    text = "(需补￥${item.spread})"
                }
            } else {
                helper.getView<TextView>(R.id.tv_spread_tip)?.visibility = View.GONE
            }

            // 设置选中状态
            if (helper.layoutPosition == selectPos) {
                helper.setBackgroundRes(R.id.item_bg, R.drawable.rectangle_gray_bg_1)
                helper.setVisible(R.id.iv_select, true)
            } else {
                helper.setBackgroundRes(R.id.item_bg, R.drawable.rectangle_gray_bg)
                helper.setVisible(R.id.iv_select, false)
            }

            if (helper.layoutPosition % 2 == 0){
                val layoutParam = helper.itemView.layoutParams as ViewGroup.MarginLayoutParams
                val marginInPixels = helper.itemView.context.resources.getDimensionPixelSize(margin)
                layoutParam.leftMargin = marginInPixels
                helper.itemView.layoutParams = layoutParam
            }else{
                val layoutParam = helper.itemView.layoutParams as ViewGroup.MarginLayoutParams
                val marginInPixels = helper.itemView.context.resources.getDimensionPixelSize(margin)
                layoutParam.rightMargin = marginInPixels
                helper.itemView.layoutParams = layoutParam
            }
        }
    }



}