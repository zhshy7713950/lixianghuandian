package com.ruimeng.things.me.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.DistrHomeBean
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_distribution_center.*
import wongxd.base.AtyBase
import wongxd.common.toPOJO
import wongxd.http


class DistributionCenterActivity : AtyBase() {

    companion object {
        lateinit var mActivity: DistributionCenterActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distribution_center)
        firstVisit = true
        initView()
        setListener()
        initData()
    }

    private fun initView(){
        mActivity = this
    }

    private fun setListener(){
        myProfitLayout?.setOnClickListener {
            startActivity(Intent(mActivity, MyProfitActivity::class.java))
        }
        balanceWithdrawalLayout?.setOnClickListener {
            startActivity(Intent(mActivity, BalanceWithdrawalActivity::class.java))
        }
        myTeamLayout?.setOnClickListener {
            val intent=Intent(mActivity, MyTeamActivity::class.java)
            intent.putExtra("member",textViewThree?.text.toString().replace("人",""))
            startActivity(intent)
        }
        shareFriendLayout?.setOnClickListener {
            startActivity(Intent(mActivity, SharePosterActivity::class.java))
        }
    }

    private fun initData(){
        initTopbar(topbar, "分享赚收益")
        textViewFour?.text = "赚收益"
        requestDistHome()
    }



    private var firstVisit = true
    override fun onResume() {
        super.onResume()
        if (firstVisit) {
            firstVisit = false
        }
        if (!firstVisit) {
            requestDistHome()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun requestDistHome() {
        http {
            url = "apiv5/distrhome"
            onSuccessWithMsg { res, _ ->
                val data = res.toPOJO<DistrHomeBean>().data
                textViewOne?.text = "￥${data.distr_all_income}"
                textViewTwo?.text = "￥${data.distr_balance}"
                textViewThree?.text = "${data.team_num}人"
            }

            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }
        }
    }

}