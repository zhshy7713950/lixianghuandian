package com.ruimeng.things.me

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemChildClickListener
import com.chad.library.adapter.base.BaseQuickAdapter.OnItemClickListener
import com.chad.library.adapter.base.BaseViewHolder
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.ruimeng.things.FgtMain
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.common.BannerHelper
import com.ruimeng.things.home.AtyScanQrcode
import com.ruimeng.things.home.FgtDeposit
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.FgtPayRentMoney
import com.ruimeng.things.home.bean.BannerInfo
import com.ruimeng.things.home.bean.ScanResult
import com.ruimeng.things.home.bean.ScanResultEvent
import com.ruimeng.things.me.bean.MyCouponBean
import com.ruimeng.things.me.vm.TicketViewModel
import com.ruimeng.things.showTipDialog
import com.utils.OptionPickerUtil
import com.utils.TextUtil
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_my_team.recyclerView
import kotlinx.android.synthetic.main.aty_order.view.refresh
import kotlinx.android.synthetic.main.fgt_ticket.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import wongxd.base.MainTabFragment
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.getCurrentAppAty
import wongxd.common.getCurrentAty
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import me.yokeyword.fragmentation.SupportFragment
import com.ruimeng.things.home.bean.BannerData

/**
 * Created by wongxd on 2018/11/13.
 */
class FgtTicket : MainTabFragment() {
    private val vm: TicketViewModel by viewModels()
    override fun getLayoutRes(): Int = R.layout.fgt_ticket

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)

        // 设置刷新按钮点击事件
        tv_refresh.setOnClickListener {
            srl_ticket.autoRefresh()
        }

        // 初始化轮播广告
        initBanner()

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
        adapter!!.setEmptyView(R.layout.layout_empty, rv_ticket)
        adapter.onItemChildClickListener =
            OnItemChildClickListener { p0, p1, p2 ->
                if (p1 != null) {
                    if (p1.id == R.id.tv_use) {
                        //                        if (isUsed == 0 ){
                        //                            if (FgtHome.CURRENT_DEVICEID.isNullOrEmpty() && FgtHome.NO_PAY_DEVICEID.isNullOrEmpty()){
                        //                                ToastHelper.shortToast(context, "请先完成押金支付")
                        //                                return
                        //                            }
                        //                            vm.getUserPaymentInfo(FgtHome.userId,FgtHome.CURRENT_DEVICEID)
                        //                        }
                    } else {
                        if (isUsed == 0) {
                            val data = adapter.data[p2]
                            data.expond = !data.expond
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }


        srl_ticket?.setOnRefreshListener { page = 1;getInfo() }
        srl_ticket?.setOnLoadMoreListener { getInfo() }
        ll_scan?.setOnClickListener {
            ToastHelper.shortToast(context, "请扫描兑换码")
            getPermissions(getCurrentAty(), PermissionType.CAMERA, allGranted = {
                AtyScanQrcode.start(getCurrentAty(), AtyScanQrcode.TYPE_TICKET)
            })
        }
        initEvent()
    }

    @Subscribe
    fun dealScanResult(event: ScanResult) {
        http {
            url = "apiv6/couponcheck/checkcode"
            params["couponCode"] = event.result
            onSuccess {
                bindCoupon(event.result)
            }
            onFail { _, s ->
                ToastHelper.shortToast(context, s)
            }
        }
    }

    private fun bindCoupon(couponCode: String){
        http {
            url = "apiv6/couponcheck/bindcoupon"
            params["couponCode"] = couponCode
            onSuccess {
                ToastHelper.shortToast(context, "兑换成功，已添加到待使用列表")
                ll_scan?.postDelayed({
                    tab_ticket.selectTab(0)
                    srl_ticket.autoRefresh()
                },1500)
            }
            onFail { _, s ->
                ToastHelper.shortToast(context, s)
            }
        }
    }

    private fun initEvent() {
        vm.userPaymentInfo.observe(this@FgtTicket, Observer { info ->
            if (info.deposit_status != "1" && info.rent_status != "1") {//无押金、无租金
                ToastHelper.shortToast(context, "请先完成押金支付")
            } else if (info.active_status == "2" && info.deposit_status == "1" && info.rent_status == "0") {//有押金、无租金、未租
                //扫码后跳转
                FgtHome.tryToScan(AtyScanQrcode.TYPE_PAY_RENT)
            } else if (info.active_status == "1" && info.deposit_status == "1" && info.rent_status == "1") {//有押金、有租金、待取电/已取电，跳续期升级
                FgtMain.instance?.start(
                    FgtPayRentMoney.newInstance(
                        FgtHome.CURRENT_DEVICEID,
                        FgtPayRentMoney.PAGE_TYPE_UPDATE
                    )
                )
            } else if (info.active_status == "3" && info.deposit_status == "1" && info.rent_status == "1") {//有押金 + 有租金（已冻结）
                ToastHelper.shortToast(context, "请先完成解冻操作")
            } else if (info.active_status == "2" && info.deposit_status == "1" && info.rent_status == "1") {//有押金 + 有租金（已过期）
                FgtMain.instance?.start(
                    FgtPayRentMoney.newInstance(
                        FgtHome.CURRENT_DEVICEID,
                        FgtPayRentMoney.PAGE_TYPE_CREATE
                    )
                )
            }
        })
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


    class RvTicketAdapter :
        BaseQuickAdapter<MyCouponBean.Data, BaseViewHolder>(R.layout.item_rv_ticket) {
        var isUsed = 0
        override fun convert(helper: BaseViewHolder, item: MyCouponBean.Data?) {
            bothNotNull(helper, item) { a, b ->
                a.setText(R.id.tv_money, b.coupon_price)
                    .setText(R.id.tv_limit, "${b.act_time}~${b.exp_time}")
                    .setText(R.id.tv_use, b.is_use)
                if (item != null) {
                    when (isUsed) {
                        0 -> {
                            a.setTextColor(R.id.tvRmb, Color.parseColor("#F9BB6C"))
                                .setTextColor(R.id.tv_money, Color.parseColor("#F9BB6C"))
                                .setTextColor(R.id.tv_coupon_name, Color.parseColor("#F9BB6C"))
                                .setTextColor(R.id.tv_limit, Color.parseColor("#FFFFFF"))
                                .setText(R.id.tv_use, "待使用")
                                .setTextColor(R.id.tv_use, Color.parseColor("#f9bb6c"))
                                .setBackgroundRes(
                                    R.id.cl_coupon,
                                    if (b.expond) R.drawable.bg_ticket_me else R.drawable.bg_ticket_unuse
                                )
                        }

                        1 -> {
                            a.setTextColor(R.id.tvRmb, Color.parseColor("#C3B199"))
                                .setTextColor(R.id.tv_money, Color.parseColor("#C3B199"))
                                .setTextColor(R.id.tv_coupon_name, Color.parseColor("#C3B199"))
                                .setTextColor(R.id.tv_limit, Color.parseColor("#D7D7D7"))
                                .setText(R.id.tv_use, "已使用")
                                .setTextColor(R.id.tv_use, Color.parseColor("#C4CAD0"))
                                .setBackgroundRes(R.id.cl_coupon, R.drawable.bg_ticket_used)
                        }

                        else -> {
                            a.setTextColor(R.id.tvRmb, Color.parseColor("#706D65"))
                                .setTextColor(R.id.tv_money, Color.parseColor("#706D65"))
                                .setTextColor(R.id.tv_coupon_name, Color.parseColor("#706D65"))
                                .setTextColor(R.id.tv_limit, Color.parseColor("#797F83"))
                                .setText(R.id.tv_use, "已过期")
                                .setTextColor(R.id.tv_use, Color.parseColor("#798289"))
                                .setBackgroundRes(R.id.cl_coupon, R.drawable.bg_ticket_expire)
                        }
                    }
                    a.setText(R.id.tv_coupon_name, "${b.coupon_category}")
                        .setText(R.id.tv_coupon_type, "优惠类型：${b.coupon_type}")
                        .setText(R.id.tv_app_type, "适用品牌：${b.app_type}")
                        .setText(R.id.tv_limit_city, "适用城市：${b.limit_city}")
                        .setText(R.id.tv_limit_voltage, "适用伏数：${b.limit_voltage}")
                        .setText(R.id.tv_limit_day_desc, "适用天数：${b.limit_day_desc}")
                        .setText(R.id.tv_act_time, "生效时间：${b.act_time}")
                        .setText(R.id.tv_exp_time, "过期时间：${b.exp_time}")
                        .setGone(R.id.cl_time, b.expond)
                        .addOnClickListener(R.id.cl_coupon_info)
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            if(srl_ticket != null){
                srl_ticket.autoRefresh()
            }
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    fun startFgt(toFgt: SupportFragment) {
        (parentFragment as FgtMain).start(toFgt)
    }

    // 初始化轮播广告
    private fun initBanner() {
        // 观察轮播广告数据变化
        vm.bannerData.observe(this) { bannerList ->
            setupBanner(bannerList)
        }
        
        // 获取轮播广告数据
        vm.fetchBannerData(requireContext(), FgtHome.userId)
    }

    // 设置轮播广告
    private fun setupBanner(bannerList: List<BannerInfo>) {
        if (bannerList.isEmpty()) {
            banner.visibility = View.GONE
            return
        }

        BannerHelper.setupBanner(banner, bannerList, this)
    }
}