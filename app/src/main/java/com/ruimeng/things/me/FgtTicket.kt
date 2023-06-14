package com.ruimeng.things.me

import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.MyCouponBean
import kotlinx.android.synthetic.main.fgt_ticket.*
import wongxd.base.BaseBackFragment
import wongxd.common.bothNotNull
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2018/11/13.
 */
class FgtTicket : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_ticket

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "优惠券")

        tab_ticket.addTab(QMUITabSegment.Tab("未使用"))
            .addTab(QMUITabSegment.Tab("已使用"))
            .setDefaultNormalColor(Color.parseColor("#929FAB"))
        tab_ticket.setDefaultSelectedColor(Color.parseColor("#29EBB6"))

        tab_ticket.addOnTabSelectedListener(object : QMUITabSegment.OnTabSelectedListener {
            override fun onTabReselected(index: Int) {
            }

            override fun onTabUnselected(index: Int) {
            }

            override fun onTabSelected(index: Int) {
                isUsed = index
                page = 1
                getInfo()
            }

            override fun onDoubleTap(index: Int) {
            }
        })

        tab_ticket.selectTab(0)

        rv_ticket.layoutManager = LinearLayoutManager(activity)
        rv_ticket.adapter = adapter

        srl_ticket?.setOnRefreshListener { page = 1;getInfo() }
        srl_ticket?.setOnLoadMoreListener { getInfo() }

    }

    private var isUsed = 0
    private val adapter: RvTicketAdapter by lazy { RvTicketAdapter() }
    private var pageSize = 20
    private var page = 1
    private fun getInfo() {

        http {
            url = Path.GET_MY_COUPON
            params["used"] = isUsed.toString()
            params["page"] = page.toString()
            params["pagesize"] = pageSize.toString()

            onSuccess {
                val result = it.toPOJO<MyCouponBean>().data

                if (page == 1) {
                    adapter.setNewData(result)
                } else {
                    adapter.addData(result)
                }

                page++
            }

            onFinish {
                srl_ticket?.finishRefresh()
                srl_ticket?.finishLoadMore()
            }

        }


    }


    class RvTicketAdapter : BaseQuickAdapter<MyCouponBean.Data, BaseViewHolder>(R.layout.item_rv_ticket) {
        override fun convert(helper: BaseViewHolder, item: MyCouponBean.Data?) {
            bothNotNull(helper, item) { a, b ->
                a.setText(R.id.tv_money,"¥"+ b.coupon_price)
                    .setText(R.id.tv_limit,b.limit_day)
                    .setText(R.id.tv_use,b.is_use)
            }
        }
    }
}