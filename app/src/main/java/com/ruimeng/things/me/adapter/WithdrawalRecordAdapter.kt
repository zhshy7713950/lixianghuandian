package com.ruimeng.things.me.adapter

import android.graphics.Color
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.DistrCashLogBean


class WithdrawalRecordAdapter (data: List<DistrCashLogBean.Data>?) :
    BaseQuickAdapter<DistrCashLogBean.Data, BaseViewHolder>(R.layout.item_withdrawal_record, data) {

    override fun convert(holper: BaseViewHolder, bean: DistrCashLogBean.Data) {

        val titleTextView = holper.getView<TextView>(R.id.titleTextView)
        titleTextView?.text=bean.title
        holper.setText(R.id.priceTextView,"-${bean.balance}")
        holper.setText(R.id.timeTextView,bean.created)
        val statusTextView = holper.getView<TextView>(R.id.statusTextView)
        statusTextView?.text=bean.status_msg
        when (bean.status) {
            "0" -> {//0=待审核,1=审核成功,2=审核失败
                statusTextView?.setTextColor(Color.parseColor("#002BD2"))
            }
            "1" -> {
                statusTextView?.setTextColor(Color.parseColor("#276F22"))
            }
            else -> {
                statusTextView?.setTextColor(Color.parseColor("#EE4447"))
            }
        }

    }

}