package com.ruimeng.things.home

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundFrameLayout
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.RepairLogBean
import kotlinx.android.synthetic.main.fgt_repair.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.getTime
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2018/11/23.
 */
class FgtRepair : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_repair

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "维修")

        tab_repair.addTab(QMUITabSegment.Tab("报修"))
            .addTab(QMUITabSegment.Tab("报修记录"))
            .setDefaultSelectedColor(resources.getColor(R.color.app_color))

        tab_repair.setDefaultNormalColor(Color.parseColor("#BDBDBD"))

        tab_repair.mode = QMUITabSegment.MODE_FIXED
        tab_repair.setHasIndicator(true)
        tab_repair.notifyDataChanged()
        tab_repair.selectTab(0)

        tab_repair.addOnTabSelectedListener(object : QMUITabSegment.OnTabSelectedListener {
            override fun onDoubleTap(index: Int) {

            }

            override fun onTabReselected(index: Int) {
            }

            override fun onTabUnselected(index: Int) {
            }

            override fun onTabSelected(index: Int) {
                scrollView_repair.visibility = if (index == 0) View.VISIBLE else View.GONE
                srl_reapair_log.visibility = if (index == 1) View.VISIBLE else View.GONE
            }
        })



        fl_energy_repair.setOnClickListener { setCheckedTag(0) }
        fl_u_repair.setOnClickListener { setCheckedTag(1) }
        fl_skin_repair.setOnClickListener { setCheckedTag(2) }
        fl_other_repair.setOnClickListener { setCheckedTag(3) }

        btn_submit_repair.setOnClickListener {
            val msg = et_repair.text.toString()

            if (currentTagIndex == -1) {
                EasyToast.DEFAULT.show("请选择报修的类型")
                return@setOnClickListener
            }


            http {
                url = Path.REPAIR
                params["tag"] = when (currentTagIndex) {
                    0 -> {
                        "电量问题"
                    }
                    1 -> {
                        "电压问题"
                    }
                    2 -> {
                        "外壳损坏"
                    }
                    else -> {
                        "其他问题"
                    }
                }

                params["msg"] = msg
                params["device_id"] = FgtHomeBack.CURRENT_DEVICEID
                params["contract_id"] = FgtHomeBack.CURRENT_CONTRACT_ID

                onSuccessWithMsg { res, msg ->
                    EasyToast.DEFAULT.show(msg)
                    et_repair.setText("")
                    srl_reapair_log.autoRefresh()
                }

            }

        }


        //报修记录
        srl_reapair_log.setOnRefreshListener { page = 1;getLog() }
        srl_reapair_log.setOnLoadMoreListener { getLog() }

        rv_repair_log.layoutManager = LinearLayoutManager(activity)
        rv_repair_log.adapter = adapter

        getLog()
    }


    private var currentTagIndex = -1
    /**
     * 设置选中的tag
     *
     * @param index 0电量问题   1电压问题   2外壳损坏  3其他问题
     */
    fun setCheckedTag(index: Int) {

        currentTagIndex = index

        fun dealIsChecked(fl: QMUIRoundFrameLayout, tv: TextView, iv: ImageView, isChecked: Boolean) {

            val appColor = resources.getColor(R.color.app_color)
            val white = resources.getColor(R.color.bg_gray)

            val bg = fl.background as QMUIRoundButtonDrawable
            bg.setStrokeData(1, ColorStateList.valueOf(if (isChecked) appColor else white))
            bg.setBgData(ColorStateList.valueOf(if (isChecked) appColor else white))

            tv.setTextColor(if (isChecked) white else appColor)

            iv.setImageResource(if (isChecked) R.drawable.face_bad_white else R.drawable.face_bad)
        }

        dealIsChecked(fl_energy_repair, tv_energy_repair, iv_energy_repair, index == 0)
        dealIsChecked(fl_u_repair, tv_u_repair, iv_u_repair, index == 1)
        dealIsChecked(fl_skin_repair, tv_skin_repair, iv_skin_repair, index == 2)
        dealIsChecked(fl_other_repair, tv_other_repair, iv_other_repair, index == 3)
    }


    private val adapter by lazy { RvRepairAdapter() }
    private var page = 1
    private var pageSize = 20

    fun getLog() {

        http {
            url = Path.REPAIR_LOG
            params["page"] = page.toString()
            params["pagesize"] = pageSize.toString()

            onFinish {
                srl_reapair_log?.finishRefresh()
                srl_reapair_log?.finishLoadMore()
            }

            onSuccess {
                val result = it.toPOJO<RepairLogBean>().data

                if (page == 1)
                    adapter.setNewData(result)
                else
                    adapter.addData(result)

                page++
            }
        }

    }


    inner class RvRepairAdapter : BaseQuickAdapter<RepairLogBean.Data, BaseViewHolder>(R.layout.item_rv_repair) {
        override fun convert(helper: BaseViewHolder, item: RepairLogBean.Data?) {
            bothNotNull(helper, item) { a, b ->

                if (a.layoutPosition % 2 == 0){
                    a.setBackgroundColor(R.id.ll_bg,context!!.resources.getColor(R.color.bg_gray))
                }else
                    a.setBackgroundColor(R.id.ll_bg,context!!.resources.getColor(R.color.white))

                a.setText(R.id.tv_time, b.created.toLong().getTime())
                a.setText(R.id.tv_type, b.tag)
            }
        }
    }

}