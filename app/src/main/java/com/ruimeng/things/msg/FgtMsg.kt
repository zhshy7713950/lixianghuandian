package   com.ruimeng.things.msg

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.NoReadLiveData
import com.ruimeng.things.R
import com.ruimeng.things.msg.bean.MsgListBean
import kotlinx.android.synthetic.main.fgt_msg.*
import wongxd.AtyWeb
import wongxd.base.MainTabFragment
import wongxd.common.bothNotNull
import wongxd.common.getTime
import wongxd.common.loadImg
import wongxd.common.toPOJO
import wongxd.http


class FgtMsg : MainTabFragment() {

    private var mAdapter: RvMsgAdapter? = null
    private var dataList = ArrayList<MsgListBean.Data>()
    private var pageIndex = 1
    private var isLoadMore = true

    override fun getLayoutRes(): Int = R.layout.fgt_msg

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        initTopbar(mView?.findViewById(R.id.topbar), "消息", false)
        firstVisit = true

        recyclerview?.layoutManager = LinearLayoutManager(activity)

        initAdapter()
        initRefresh()
    }

    private fun initRefresh() {
        refreshLayout?.autoRefresh()
        refreshLayout?.setEnableLoadMore(true)
        refreshLayout?.setOnRefreshListener {
            pageIndex = 1
            dataList.clear()
            mAdapter?.notifyDataSetChanged()
            getInfo()
        }
        refreshLayout?.setOnLoadMoreListener {
            if (isLoadMore) {
                pageIndex++
                isLoadMore = false
                getInfo()
            }
        }
    }

    private fun initAdapter() {
        mAdapter = RvMsgAdapter(dataList)
        recyclerview?.adapter = mAdapter
    }


    fun getInfo() {
        http {
            url = "/apiv4/appmsg"
            params["page"] = pageIndex.toString()
            params["pagesize"] = "20"

            onFinish {
                refreshLayout?.finishRefresh()
                refreshLayout?.finishLoadMore()
                isLoadMore = true
            }

            onSuccess {
                val result = it.toPOJO<MsgListBean>().data
                if (result.isNotEmpty()) {
                    dataList.addAll(result)
                }
                mAdapter?.notifyDataSetChanged()
            }
        }
    }


    inner class RvMsgAdapter (data: List<MsgListBean.Data>?):
        BaseQuickAdapter<MsgListBean.Data, BaseViewHolder>(R.layout.item_rv_msg,data) {
        override fun convert(holper: BaseViewHolder, item: MsgListBean.Data?) {

            bothNotNull(holper, item) { a, b ->
                if (b.img.isNotBlank())
                    a.getView<ImageView>(R.id.imageView).loadImg(b.img)
                a.setText(R.id.tv_title, b.title)
                a.setText(R.id.tv_time, b.created.toLong().getTime())

                val redPointView = a.getView<View>(R.id.redPointView)
                if (1 == b.is_read) {
                    redPointView.visibility = View.GONE
                } else {
                    redPointView.visibility = View.VISIBLE
                }

                a.itemView.setOnClickListener {
                    AtyWeb.start("消息详情", b.url)
                    NoReadLiveData.refresh()
                }
            }
        }
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
}