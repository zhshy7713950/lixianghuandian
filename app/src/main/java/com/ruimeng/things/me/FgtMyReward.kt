package com.ruimeng.things.me

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.net.Server
import com.ruimeng.things.R
import com.ruimeng.things.me.activity.AtyWeb2
import com.utils.StatusBarUtil
import kotlinx.android.synthetic.main.fgt_my_reward.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.utilcode.util.ScreenUtils
import java.net.URLEncoder
import wongxd.utils.utilcode.util.SizeUtils

class FgtMyReward : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_my_reward

    private val friendAdapter by lazy { FriendAdapter() }
    private var allFriends = listOf<FriendRewardBean>()
    private var currentFilter = "全部"

    override fun onSupportVisible() {
        super.onSupportVisible()
        activity?.let { StatusBarUtil.setColor(it, Color.parseColor("#D83D3E")) }
    }

    override fun onSupportInvisible() {
        super.onSupportInvisible()
        activity?.let { StatusBarUtil.setColor(it, resources.getColor(R.color.app_color)) }
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "我的奖励")
        topbar.setTitle("我的奖励").setTextColor(Color.WHITE)
        topbar.addRightImageButton(R.drawable.ic_question_white, R.id.topbar_right_button).setOnClickListener {
            AtyWeb2.start("规则说明", "http://xianglilai.scxll.cn/appH5/newClientRewardRule.html")
        }
        topbar.setBackgroundColor(android.graphics.Color.parseColor("#D83D3E"))

        initView()
        initListener()
        fetchData()
    }

    private fun initView() {
        rv_friends.layoutManager = LinearLayoutManager(context)
        rv_friends.adapter = friendAdapter
    }

    private fun initListener() {
        val policyListener = View.OnClickListener {
            fetchPolicyAndJump()
        }
        iv_monthly_standard_fixed.setOnClickListener(policyListener)

        btn_withdraw.setOnClickListener {
            val withdrawableStr = tv_withdrawable_amount.text.toString().replace("元", "").trim()
            val amount = withdrawableStr.toDoubleOrNull() ?: 0.0
            if (amount <= 0.0) {
                EasyToast.DEFAULT.show("暂无可提现金额")
            } else if (amount < 10.0) {
                EasyToast.DEFAULT.show("可提现金额需大于等于￥10.0")
            } else {
                start(FgtApplyWithdraw.newInstance(withdrawableStr))
            }
//            start(FgtApplyWithdraw.newInstance("30"))
        }

        ll_total_withdrawn.setOnClickListener {
            start(FgtWithdrawList.newInstance())
        }

        val explanationListener = View.OnClickListener {
            showRewardExplanationDialog()
        }
        ll_pending_label.setOnClickListener(explanationListener)
        ll_potential_header.setOnClickListener(explanationListener)

        ll_filter_status.setOnClickListener { v ->
            showFilterPopup(v)
        }
    }

    private fun showFilterPopup(v: View) {
        val act = activity ?: return
        val builder = com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet.BottomListSheetBuilder(act)

        val items = listOf("全部", "待购买套餐", "已购买套餐", "已续期", "已冻结", "已过期")
        items.forEach { builder.addItem(it, it) }

        val redSpan = android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#FF3B30"))
        val cancelText = android.text.SpannableString("取消")
        cancelText.setSpan(redSpan, 0, cancelText.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.addItem(cancelText.toString(), "取消")

        builder.setOnSheetItemClickListener { sheet, _, _, tag ->
            sheet.dismiss()
            if (tag != "取消") {
                filterList(tag)
            }
        }
        builder.build().show()
    }

    private fun showRewardExplanationDialog() {
        val act = activity ?: return
        val dialog = android.app.Dialog(act, R.style.TransparentDialog)
        dialog.setContentView(R.layout.dialog_reward_explanation)
        
        // Remove default background (handled by style, but safe to keep)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setDimAmount(0.3f)
        
        // Positioning
        val params = dialog.window?.attributes
        params?.gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
        params?.y = SizeUtils.dp2px(120f)
        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(120f)
        dialog.window?.attributes = params

        dialog.findViewById<View>(R.id.btn_i_know).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun filterList(status: String) {
        currentFilter = status
        if (status == "全部") {
            friendAdapter.setNewData(allFriends)
        } else {
            friendAdapter.setNewData(allFriends.filter { it.userStatus == status })
        }
        // Need to dismiss popup? 
        // I'll re-implement showFilterPopup cleanly.
    }

    private fun fetchData() {
        http {
            url = "/apiv6/distribute/myrewards"
            onSuccess { res ->
                val bean = res.toPOJO<MyRewardBean>().data
                updateUI(bean)
            }
        }
    }

    private fun updateUI(data: MyRewardData) {
        tv_pending_amount.text = getSpannableAmount("${data.unreachedIncome}元")
        tv_received_amount.text = getSpannableAmount("${data.totalIncome}元")
        tv_withdrawable_amount.text = getSpannableAmount("${data.withdrawable}元")
        tv_total_used.text = "您已累计提现${data.usedIncome}元"
        
        allFriends = data.inviteUserList
        tv_invite_count.text = "(共${allFriends.size}位)"
        filterList(currentFilter)
    }

    private fun getSpannableAmount(amountStr: String): android.text.SpannableString {
        val spannable = android.text.SpannableString(amountStr)
        if (amountStr.endsWith("元")) {
            spannable.setSpan(
                android.text.style.AbsoluteSizeSpan(16, true),
                amountStr.length - 1,
                amountStr.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }

    private fun fetchPolicyAndJump() {
        http {
            url = "/apiv6/distribute/policy"
            onSuccess { res ->
                val policy = res.toPOJO<PolicyBean>().data
                val paraMap = mapOf(
                    "newPrice" to policy.new_user,
                    "oldPrice" to policy.upkeep_fee,
                    "startDate" to policy.start_time,
                    "endDate" to policy.end_time
                )
                val json = Server.gson.toJson(paraMap)
                val encodedPara = URLEncoder.encode(json, "UTF-8")
                val url = "http://xianglilai.scxll.cn/appH5/monthlyRewardRule.html?para=$encodedPara"
                AtyWeb2.start("本月奖励标准", url)
            }
        }
    }

    data class MyRewardBean(val code: Int, val msg: String, val data: MyRewardData)
    data class MyRewardData(
        val unreachedIncome: String,
        val totalIncome: String,
        val withdrawable: String,
        val usedIncome: String,
        val inviteUserList: List<FriendRewardBean>
    )

    data class FriendRewardBean(
        val realname: String,
        val userStatus: String,
        val unreachedIncome: String,
        val totalIncome: String
    )

    data class PolicyBean(val code: Int, val msg: String, val data: PolicyData)
    data class PolicyData(
        val new_user: String,
        val upkeep_fee: String,
        val start_time: String,
        val end_time: String
    )

    inner class FriendAdapter : BaseQuickAdapter<FriendRewardBean, BaseViewHolder>(R.layout.item_friend_reward) {
        override fun convert(helper: BaseViewHolder, item: FriendRewardBean) {
            helper.setText(R.id.tv_name, item.realname)
            helper.setText(R.id.tv_status, item.userStatus)
            helper.setText(R.id.tv_potential, item.unreachedIncome)
            helper.setText(R.id.tv_received, item.totalIncome)
        }
    }
}
