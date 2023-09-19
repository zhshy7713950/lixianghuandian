package com.ruimeng.things.me.activity

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.ruimeng.things.R
import com.ruimeng.things.me.adapter.MyTeamAdapter
import com.ruimeng.things.me.bean.MyTeamBean
import kotlinx.android.synthetic.main.activity_my_team.*
import wongxd.base.AtyBase
import wongxd.common.toPOJO
import wongxd.http


class MyTeamActivity : AtyBase() {

    private var mAdapter: MyTeamAdapter? = null
    private var dataList = ArrayList<MyTeamBean.Data>()

    companion object {
        lateinit var mActivity: MyTeamActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_team)
        initView()
        setListener()
        initData()
    }

    private fun initView() {
        mActivity = this
        tv_team_member.text = ""+intent.getStringExtra("member")
        recyclerView?.layoutManager = GridLayoutManager(mActivity, 1)
    }

    private fun setListener() {

    }

    private fun initData() {
        initTopbar(topbar, "我的团队")
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
            requestMyTeam()
        }
        refreshLayout?.setOnLoadMoreListener {
            requestMyTeam()
        }
    }

    private fun initAdapter() {
        mAdapter = MyTeamAdapter(dataList)
        recyclerView?.adapter = mAdapter
        mAdapter!!.setEmptyView(R.layout.layout_empty,recyclerView)
    }

    private var pageSize = 20
    private var page = 1
    private fun requestMyTeam() {
        http {
            url = "apiv5/myteam"
            params["page"] = page.toString()
            params["pagesize"] = pageSize.toString()

            onSuccess {
                val result = it.toPOJO<MyTeamBean>().data

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