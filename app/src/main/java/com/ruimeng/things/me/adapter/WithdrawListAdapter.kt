package com.ruimeng.things.me.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.WithdrawItem
import com.utils.TextUtil

class WithdrawListAdapter(data: List<WithdrawItem>?) :
    BaseQuickAdapter<WithdrawItem, BaseViewHolder>(R.layout.item_withdraw_list, data) {

    override fun convert(helper: BaseViewHolder, item: WithdrawItem) {
        // 1. Time: yyyy-MM-dd
        val time = if (!item.created.isNullOrEmpty() && item.created.length >= 10) {
            item.created.substring(0, 10)
        } else {
            item.created ?: ""
        }
        helper.setText(R.id.tv_time, time)

        // 2. Account: 支付宝(last 4)
        val accountNum = item.payInfo?.accountNum
        val accountDisplay = if (!accountNum.isNullOrEmpty() && accountNum.length > 4) {
            "支付宝(${accountNum.substring(accountNum.length - 4)})"
        } else {
            "支付宝($accountNum)"
        }
        helper.setText(R.id.tv_account, accountDisplay)

        // 3. Amount
        helper.setText(R.id.tv_amount, "${item.balance ?: "0"}元")

        // 4. Status
        val statusTv = helper.getView<TextView>(R.id.tv_status)
        val statusText: String
        val bgColor: String

        when (item.pay_status) {
            "已支付" -> {
                statusText = "提现成功"
                bgColor = "#D83D3E"
            }
            "支付失败" -> {
                statusText = "提现失败"
                bgColor = "#333643"
            }
            else -> {
                statusText = "提现中"
                bgColor = "#F39130"
            }
        }

        helper.setText(R.id.tv_status, statusText)

        // Set background with rounded corners
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor(Color.parseColor(bgColor))
        drawable.cornerRadius = 12.5f * helper.itemView.context.resources.displayMetrics.density // Convert dp to px roughly or rely on context
        // Better:
        // drawable.cornerRadius = com.qmuiteam.qmui.util.QMUIDisplayHelper.dp2px(helper.itemView.context, 12)
        // I will use a safe float value
        drawable.cornerRadius = 25f // Just a safe radius to make it pill-shaped
        statusTv.background = drawable
    }
}
