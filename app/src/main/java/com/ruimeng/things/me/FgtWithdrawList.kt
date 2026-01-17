package com.ruimeng.things.me

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.entity.local.WithdrawListLocal
import com.net.call.BizService
import com.net.whenError
import com.net.whenSuccess
import com.ruimeng.things.R
import com.ruimeng.things.me.activity.AtyWeb2
import com.ruimeng.things.me.adapter.WithdrawListAdapter
import com.utils.StatusBarUtil
import kotlinx.android.synthetic.main.fgt_withdraw_list.*
import kotlinx.coroutines.launch
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast

class FgtWithdrawList : BaseBackFragment() {

    companion object {
        fun newInstance(): FgtWithdrawList {
            return FgtWithdrawList()
        }
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        activity?.let { StatusBarUtil.setColor(it, Color.parseColor("#ED5A2E")) }
    }

    override fun onSupportInvisible() {
        super.onSupportInvisible()
        activity?.let { StatusBarUtil.setColor(it, resources.getColor(R.color.app_color)) }
    }

    private val adapter by lazy { WithdrawListAdapter(null) }

    override fun getLayoutRes(): Int = R.layout.fgt_withdraw_list

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "提现明细")
        // Make topbar transparent to show the orange background view behind it
        // Transparent topbar
        topbar.setBackgroundColor(0)
        topbar.setTitle("提现明细").setTextColor(android.graphics.Color.WHITE)
        topbar.addRightImageButton(R.drawable.ic_question_white, R.id.right).setOnClickListener {
            AtyWeb2.start("规则说明", "http://xianglilai.scxll.cn/appH5/newClientRewardRule.html")
        }

        recycler_view.layoutManager = LinearLayoutManager(activity)
        recycler_view.adapter = adapter

        adapter.setOnItemClickListener { _, _, position ->
            val item = adapter.data[position]
            start(FgtWithdrawDetail.newInstance(item))
        }

        fetchData()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            BizService.getWithdrawList(WithdrawListLocal("1", "10000"))
                .whenSuccess { response ->
                    adapter.setNewData(response.data.data)
                }
                .whenError { _, msg ->
                    EasyToast.DEFAULT.show(msg)
                }
        }
    }
}
