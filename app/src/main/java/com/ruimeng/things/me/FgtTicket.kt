package com.ruimeng.things.me

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemChildClickListener
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
import com.chad.library.adapter.base.BaseViewHolder
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.ruimeng.things.FgtMain
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtDeposit
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.FgtPayRentMoney
import com.ruimeng.things.me.bean.MyCouponBean
import com.ruimeng.things.showTipDialog
import com.utils.TextUtil
import kotlinx.android.synthetic.main.activity_my_team.recyclerView
import kotlinx.android.synthetic.main.fgt_ticket.*
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.getCurrentAppAty
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

        tab_ticket.addTab(QMUITabSegment.Tab("待使用"))
            .addTab(QMUITabSegment.Tab("已使用"))
            .addTab(QMUITabSegment.Tab("已过期"))
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
        adapter!!.setEmptyView(R.layout.layout_empty,rv_ticket)
        adapter.setOnItemChildClickListener(object :OnItemChildClickListener{
            override fun onItemChildClick(p0: BaseQuickAdapter<*, *>?, p1: View?, p2: Int) {
                if (p1 != null) {
                    if (p1.id == R.id.tv_use){
                        if (isUsed == 0 ){
                            http {
                                url = "apiv4/rentstep1"
                                params["device_id"] = FgtHome.CURRENT_DEVICEID
                                params["cg_mode"] = "1"
                                onSuccess {
                                    val json = JSONObject(it)
                                    val data = json.optJSONObject("data")
                                    val status = data.optInt("status")
                                    when (status) {
                                        1 -> FgtMain.instance?.start(FgtPayRentMoney.newInstance(FgtHome.CURRENT_DEVICEID,if(FgtHome.hasChangePackege) FgtPayRentMoney.PAGE_TYPE_UPDATE else FgtPayRentMoney.PAGE_TYPE_CREATE))
                                    }
                                }
                            }
                        }
                    }else{
                        adapter.data.get(p2).expond = !adapter.data.get(p2).expond
                        adapter.notifyDataSetChanged()
                    }
                }
            }

        })


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
                adapter.isUsed = isUsed

                page++
            }

            onFinish {
                srl_ticket?.finishRefresh()
                srl_ticket?.finishLoadMore()
            }

        }


    }


    class RvTicketAdapter : BaseQuickAdapter<MyCouponBean.Data, BaseViewHolder>(R.layout.item_rv_ticket) {
        var isUsed = 0
        override fun convert(helper: BaseViewHolder, item: MyCouponBean.Data?) {
            bothNotNull(helper, item) { a, b ->
                a.setText(R.id.tv_money,TextUtil.getMoneyText(b.coupon_price))
                    .setText(R.id.tv_limit,b.limit_day)
                    .setText(R.id.tv_use,b.is_use)
                    .setText(R.id.tv_time,"有效期至：${b.exp_time}")
                if (item != null) {
                    if (isUsed== 0){
                        a.setTextColor(R.id.tv_money,Color.parseColor("#F9BB6C"))
                            .setTextColor(R.id.tv_coupon_name,Color.parseColor("#F9BB6C"))
                            .setTextColor(R.id.tv_limit,Color.parseColor("#FFFFFF"))
                            .setText(R.id.tv_use,"未使用")

                    }else if (isUsed == 1){
                        a.setTextColor(R.id.tv_money,Color.parseColor("#C3B199"))
                            .setTextColor(R.id.tv_coupon_name,Color.parseColor("#C3B199"))
                            .setTextColor(R.id.tv_limit,Color.parseColor("#D7D7D7"))
                            .setText(R.id.tv_use,"已使用")

                    }
                    else{
                        a.setTextColor(R.id.tv_money,Color.parseColor("#C3B199"))
                            .setTextColor(R.id.tv_coupon_name,Color.parseColor("#C3B199"))
                            .setTextColor(R.id.tv_limit,Color.parseColor("#D7D7D7"))
                            .setText(R.id.tv_use,"已过期")
                    }

                    a.setGone(R.id.cl_time,b.expond)
                    a.setBackgroundRes(R.id.ll_content,if (b.expond) R.mipmap.bg_ticket_me_big else R.mipmap.bg_ticket_me)


                }
                a.addOnClickListener(R.id.tv_use)
                a.addOnClickListener(R.id.cl_coupon_info)
            }
        }
    }
}