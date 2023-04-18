package com.ruimeng.things.me.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager

import com.ruimeng.things.R
import com.ruimeng.things.me.adapter.MyProfitAdapter
import com.ruimeng.things.me.bean.MyInComeBean
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_my_profit.*
import wongxd.base.AtyBase
import wongxd.common.toPOJO
import wongxd.http


class MyProfitActivity : AtyBase(){

    private var mAdapter: MyProfitAdapter? = null
    private var dataList = ArrayList<MyInComeBean.Data.LiatData>()

    companion object {
        lateinit var mActivity: MyProfitActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profit)
        firstVisit = true
        initView()
        setListener()
        initData()
    }

    private fun initView(){
        mActivity = this
        recyclerView?.layoutManager = GridLayoutManager(mActivity, 1)
    }

    private fun setListener(){
        withdrawalBtn?.setOnClickListener {
            startActivity(Intent(mActivity, BalanceWithdrawalActivity::class.java))
        }
    }

    private fun initData(){
        initTopbar(topbar, "我的收益")

        initAdapter()
        initRefresh()
    }
    private var firstVisit = true
    override fun onResume() {
        super.onResume()
        if (firstVisit) {
            firstVisit = false
        }
        if (!firstVisit) {
            refreshLayout?.autoRefresh()
        }
    }

    private fun initRefresh() {
        refreshLayout?.autoRefresh()
        refreshLayout?.setEnableLoadMore(true)
        refreshLayout?.setOnRefreshListener {
            page = 1
            dataList.clear()
            mAdapter?.notifyDataSetChanged()
            requestMyInCome()
        }
        refreshLayout?.setOnLoadMoreListener {
            requestMyInCome()
        }
    }

    private fun initAdapter() {
        mAdapter = MyProfitAdapter(dataList)
        recyclerView?.adapter = mAdapter
    }

    private var pageSize = 20
    private var page = 1
    @SuppressLint("SetTextI18n")
    private fun requestMyInCome() {
        http {
            url = "apiv5/myincome"
            params["page"] = page.toString()
            params["pagesize"] = pageSize.toString()

            onSuccessWithMsg { res, _ ->
                val data = res.toPOJO<MyInComeBean>().data
                allBalanceText?.text="￥${data.distr_all_income}"
                balanceTextView?.text="可提现余额：￥${data.distr_balance}"

                if (page == 1) {
                    mAdapter?.setNewData(data.list)
                } else {
                    mAdapter?.addData(data.list)
                }
                mAdapter?.notifyDataSetChanged()
                page++
            }
            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }

            onFinish {
                refreshLayout?.finishRefresh()
                refreshLayout?.finishLoadMore()
            }

        }


    }

}