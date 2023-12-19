package com.ruimeng.things.me

import android.os.Bundle
import android.view.View
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.me.activity.AtyWeb2
import kotlinx.android.synthetic.main.fgt_agent.*
import wongxd.AtyWeb
import wongxd.base.BaseBackFragment

/**
 * Created by wongxd on 2018/11/28.
 */
class FgtAgnet : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_agent


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "代理商")

        InfoViewModel.getDefault().userInfo.simpleObserver(this) { userInfo ->

            if (userInfo.agent_id.isNotBlank() && userInfo.agent_id != "0") {
                tv_tips_agent.text = "您的代理ID为：${userInfo.agent_id}"
                btn_request_agent.visibility = View.GONE
            }
        }

        btn_request_agent.setOnClickListener { startWithPop(FgtApplyAgent()) }
        agentLogin?.setOnClickListener {
            AtyWeb2.start("代理商登录", "${getString(R.string.home_url)}agent/login")
        }
    }
}