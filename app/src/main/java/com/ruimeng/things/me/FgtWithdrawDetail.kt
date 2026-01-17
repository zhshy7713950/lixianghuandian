package com.ruimeng.things.me

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import com.ruimeng.things.R
import com.ruimeng.things.me.activity.AtyWeb2
import com.ruimeng.things.me.bean.WithdrawItem
import com.utils.StatusBarUtil
import kotlinx.android.synthetic.main.fgt_withdraw_detail.*
import wongxd.base.BaseBackFragment

class FgtWithdrawDetail : BaseBackFragment() {

    override fun onSupportVisible() {
        super.onSupportVisible()
        activity?.let { StatusBarUtil.setColor(it, Color.parseColor("#ED5A2E")) }
    }

    override fun onSupportInvisible() {
        super.onSupportInvisible()
        activity?.let { StatusBarUtil.setColor(it, resources.getColor(R.color.app_color)) }
    }

    companion object {
        fun newInstance(item: WithdrawItem): FgtWithdrawDetail {
            val fgt = FgtWithdrawDetail()
            val bundle = Bundle()
            bundle.putSerializable("data", item)
            fgt.arguments = bundle
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_withdraw_detail

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "提现详情")
        // Transparent topbar
        topbar.setBackgroundColor(0)
        topbar.setTitle("提现详情").setTextColor(android.graphics.Color.WHITE)
        topbar.addRightImageButton(R.drawable.ic_question_white, R.id.right).setOnClickListener {
            AtyWeb2.start("规则说明", "http://xianglilai.scxll.cn/appH5/newClientRewardRule.html")
        }


        val item = arguments?.getSerializable("data") as? WithdrawItem
        if (item == null) {
            pop()
            return
        }

        // Setup Details
        tv_user.text = item.payInfo?.accountName ?: ""
        tv_method.text = "支付宝"
        tv_account.text = item.payInfo?.accountNum ?: ""
        tv_time.text = item.created ?: ""
        tv_amount.text = "${item.balance ?: "0"}元"

        // Setup Status
        val status = item.pay_status
        when (status) {
            "已支付" -> {
                updateStatusUI(
                    bgColor = "#D83D3E",
                    iconRes = R.drawable.ic_withdraw_success,
                    title = "提现成功",
                    desc = "请及时查看到账情况，若未到账，请联系客服人员"
                )
            }
            "支付失败" -> {
                updateStatusUI(
                    bgColor = "#656565",
                    iconRes = R.drawable.ic_withdraw_fail,
                    title = "提现失败",
                    desc = "请检查提现账号，如账号有误，请重新填写后提交申请"
                )
            }
            else -> {
                updateStatusUI(
                    bgColor = "#D83D3E",
                    iconRes = R.drawable.ic_withdraw_success,
                    title = "提现中",
                    desc = "提现后24小时内到账，若未及时到账，请联系客服人员"
                )
            }
        }

        btn_back.setOnClickListener {
            pop()
        }
    }

    private fun updateStatusUI(bgColor: String, iconRes: Int, title: String, desc: String) {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.OVAL
        drawable.setColor(Color.parseColor(bgColor))
        fl_icon_bg.background = drawable

        iv_status_icon.setImageResource(iconRes)
        tv_status_title.text = title
        tv_status_title.setTextColor(Color.parseColor("#ED5A2E"))
        tv_status_desc.text = desc
    }
}
