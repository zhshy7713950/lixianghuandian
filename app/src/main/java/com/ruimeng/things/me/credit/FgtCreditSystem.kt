package com.ruimeng.things.me.credit

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundFrameLayout
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.me.credit.bean.CreditContractListBean
import kotlinx.android.synthetic.main.fgt_credit_system.*
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.bothNotNull
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2019/1/2.
 */
class FgtCreditSystem : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_credit_system


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "信用体系")

        btn_req_credit_sys.setOnClickListener { start((FgtCreditAuthentication())) }

        rv_credit_sys.layoutManager = LinearLayoutManager(activity)
        rv_credit_sys.adapter = adapter

        srl_credit_sys.setOnRefreshListener { page = 1;getInfo() }
        srl_credit_sys.setOnLoadMoreListener { getInfo() }

        srl_credit_sys.autoRefresh()
    }


    override fun onSupportVisible() {
        super.onSupportVisible()
        srl_credit_sys?.autoRefresh()
    }


    private var isAuthentication = false

    private fun getInfo() {

        http {
            url = Path.CREDITINFO

            onSuccess {
                isAuthentication = true

                rl_pass_info_credit_sys?.visibility = View.VISIBLE
                ll_not_pass_credit_sys?.visibility = View.GONE

                val json = JSONObject(it)
                val data = json.optJSONObject("data")

                //name string 人名
                //idcard string 身份证号
                //sex string 男|女
                rl_pass_info_credit_sys?.let {
                    val name = data.optString("name")
                    val gender = data.optString("sex")
                    val idcard = data.optString("idcard")

                    tv_name.text = "姓名：$name"
                    tv_gender.text = "性别：$gender"
                    tv_card_no.text = "证件号码：$idcard"
                }


            }

            onFail { i, s ->
                rl_pass_info_credit_sys?.visibility = View.GONE
                ll_not_pass_credit_sys?.visibility = View.VISIBLE
            }

            onFinish {

                getContractList()
            }
        }
    }

    private var page = 1
    private val pageSize = 20
    private fun getContractList() {

        http {
            url = Path.CREDITLIST
            params["page"] = page.toString()
            params["pagesize"] = pageSize.toString()

            onSuccess {

                val bean = it.toPOJO<CreditContractListBean>()
                if (page == 1) {
                    adapter.setNewData(bean.data)
                } else {
                    adapter.addData(bean.data)
                }

                page++

            }

            onFinish {
                srl_credit_sys?.finishRefresh()
                srl_credit_sys?.finishLoadMore()
            }
        }
    }


    private val adapter: RvCreditContractAdapter by lazy { RvCreditContractAdapter() }


    inner class RvCreditContractAdapter :
        BaseQuickAdapter<CreditContractListBean.Data, BaseViewHolder>(R.layout.item_rv_credit_sys) {
        override fun convert(helper: BaseViewHolder, item: CreditContractListBean.Data?) {
            bothNotNull(helper, item) { a, b ->
                a.setText(R.id.tv_battery_num_credit_sys, "设备编号：" + b.device_id)
                    .setText(R.id.tv_battery_model_credit_sys, "设备型号：" + b.device_mode)
                    .setText(R.id.tv_rent_long_credit_sys, "租赁周期：" + b.loanperiod + "个月")
                    .setText(R.id.tv_first_pay_credit_sys, "首付:" + b.credit_first_money)

                a.getView<QMUIRoundFrameLayout>(R.id.fl_go_pay_first_credit_sys).setOnClickListener {
                    start(FgtCreditFirstPay.newInstance(b.contract_id, b.credit_first_money))
                }

                a.setVisible(R.id.fl_go_pay_first_credit_sys, b.show_firstpay == 1)

                a.setVisible(R.id.fl_req_sign_credit_sys, b.status == 0)
                a.getView<QMUIRoundFrameLayout>(R.id.fl_req_sign_credit_sys)
                    .setOnClickListener {
                        if (isAuthentication) {
                            start(FgtCreditContract.newInstance(b.contract_id))
                        } else {
                            if (activity == null)
                                return@setOnClickListener
                            AnyLayer.with(activity!!)
                                .contentView(R.layout.layout_credit_tips)
                                .backgroundBlurScale(5f)
                                .backgroundBlurRadius(5f)
                                .bindData { anyLayer ->
                                    anyLayer.getView<TextView>(R.id.tv).text = "请先完成信用认证后再进行签约"
                                    anyLayer.getView<QMUIRoundButton>(R.id.btn).setOnClickListener {
                                        anyLayer.dismiss()
                                    }
                                }
                                .show()
                        }
                    }

                var tipsSign = "签约状态："
                var tipsNextPay = "下一还款日：" + b.next_day

                if (b.status == 0) {
                    tipsNextPay = "下一还款日：暂无"
                } else if (b.status == 1) {
                    a.itemView.setOnClickListener {
                        start(FgtCreditReckoning.newInstance(b.contract_id))
                    }
                    tipsSign += b.cur_period
                } else {
                    tipsSign = "已还款"
                }

                a.setText(R.id.tv_sign_status_credit_sys, tipsSign)
                    .setText(R.id.tv_next_pay_time_credit_sys, tipsNextPay)
            }
        }
    }


}