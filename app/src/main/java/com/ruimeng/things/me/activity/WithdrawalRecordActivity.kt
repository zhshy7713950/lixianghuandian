package com.ruimeng.things.me.activity

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.ruimeng.things.R
import com.ruimeng.things.me.adapter.WithdrawalRecordAdapter
import com.ruimeng.things.me.bean.DistrCashLogBean
import kotlinx.android.synthetic.main.activity_withdrawal_record.*
import kotlinx.android.synthetic.main.fgt_ticket.rv_ticket
import wongxd.base.AtyBase
import wongxd.common.toPOJO
import wongxd.http


class WithdrawalRecordActivity : AtyBase(){

    private var mAdapter: WithdrawalRecordAdapter? = null
    private var dataList = ArrayList<DistrCashLogBean.Data>()


    companion object {
        lateinit var mActivity: WithdrawalRecordActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdrawal_record)
        mActivity = this
        initView()
        setListener()
        initData()
    }

    private fun initView(){
        recyclerView?.layoutManager = GridLayoutManager(mActivity, 1)
    }

    private fun setListener(){

    }

    private fun initData(){
        initTopbar(topbar, "提现记录")
        initAdapter()
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout?.autoRefresh()
        refreshLayout?.setEnableLoadMore(true)
        refreshLayout?.setOnRefreshListener {
            page = 1
            dataList.clear()
            mAdapter?.notifyDataSetChanged()
            requestDistrCashLog()
        }
        refreshLayout?.setOnLoadMoreListener {
            requestDistrCashLog()
        }
    }

    private fun initAdapter() {
        mAdapter = WithdrawalRecordAdapter(dataList)
        recyclerView?.adapter = mAdapter
        mAdapter!!.setEmptyView(R.layout.layout_empty,recyclerView)
    }

    private var pageSize = 20
    private var page = 1
    private fun requestDistrCashLog() {
        http {
            url = "apiv5/distrcashlog"
            params["page"] = page.toString()
            params["pagesize"] = pageSize.toString()

            onSuccess {
                val result = it.toPOJO<DistrCashLogBean>().data

                if (page == 1) {
                    mAdapter?.setNewData(result)
                } else {
                    mAdapter?.addData(result)
                }
                mAdapter?.notifyDataSetChanged()
                page++
            }

            onFinish {
                refreshLayout?.finishRefresh()
                refreshLayout?.finishLoadMore()
            }

        }


    }

}