package com.ruimeng.things.me.contract

import MyContractListBean
import android.os.Bundle
import android.text.TextUtils
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundFrameLayout
import com.ruimeng.things.NoReadLiveData
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.utils.TextUtil
import kotlinx.android.synthetic.main.fgt_my_contract_item.*
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wongxd.base.MainTabFragment
import wongxd.common.EasyToast
import wongxd.common.bothNotNull
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2019/12/24.
 */
class FgtMyContractItem : MainTabFragment() {


    companion object {
        fun newInstance(contractType: Int): FgtMyContractItem {
            return FgtMyContractItem().apply {
                arguments = Bundle().apply {
                    putInt("contractType", contractType)
                }
            }
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_my_contract_item


    private val contractType by lazy { arguments?.getInt("contractType") ?: 1 }


    override fun initView(mView: View?, savedInstanceState: Bundle?) {

        EventBus.getDefault().register(this)

        srl_my_contract?.apply {
            setOnRefreshListener {
                page = 1
                getInfo()
            }


            setOnLoadMoreListener {
                getInfo()
            }
        }

        rv_my_contract?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = rvAdapter.apply {
                setEmptyView(R.layout.layout_empty, rv_my_contract)
            }
        }

        getInfo()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe
    fun doSearch(event: FgtMyContract.EventDoContractSearch) {

        if (event.type != contractType) return
        if (event.q == qStr) return

        qStr = event.q

        page = 1
        getInfo()
    }

    private var qStr = ""
    private var page = 1
    private val pageSize = 10

    private fun getInfo() {

        NoReadLiveData.refresh()

        http {
            url = PathV3.MY_CONTRACT

            //	0正常1过期2未完成3历史
            params["type"] = contractType.toString()
            params["q"] = qStr
            params["page"] = page.toString()
            params["pagesize"] = pageSize.toString()


            onFinish {
                srl_my_contract?.apply {
                    finishRefresh()
                    finishLoadMore()
                }
            }

            onSuccess { res ->

                srl_my_contract?.let {
                    val bean = res.toPOJO<MyContractListBean>().data
                    if (page == 1) {
                        rvAdapter.setNewData(bean)
                    } else {
                        rvAdapter.addData(bean)
                    }
                    page++
                }

            }


            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }

    }


    private fun tryReturnDeposit(contractId: String) {

        fun doNetReq() {
            http {

                url = PathV3.RETURN_DEPOIST
                params["contract_id"] = contractId

                onSuccessWithMsg { res, msg ->
                    if (null != activity) {
                        NormalDialog(activity)
                            .apply {
                                style(NormalDialog.STYLE_TWO)
                                btnNum(1)
                                title("提示")
                                content(msg)
                                btnText("确认")
                                setOnBtnClickL(OnBtnClickL {
                                    dismiss()
                                    srl_my_contract?.autoRefresh()
                                })

                            }.show()
                    } else {
                        EasyToast.DEFAULT.show(msg)
                    }

                }


                onFail { code, msg ->
                    EasyToast.DEFAULT.show(msg)
                }

            }
        }



        NormalDialog(activity)
            .apply {
                style(NormalDialog.STYLE_TWO)
                btnNum(2)
                title("请阅读后点击确认!")
                content("是否确认退还押金？")
                btnText("确认", "取消")
                setOnBtnClickL(OnBtnClickL {
                    dismiss()
                    doNetReq()
                }, OnBtnClickL {
                    dismiss()
                })

            }.show()


    }


    private val rvAdapter by lazy { RvAdapter() }

    private fun startFgt(fgt: SupportFragment) {
        (parentFragment as FgtMyContract).start(fgt)
    }

    inner class RvAdapter :
        BaseQuickAdapter<MyContractListBean.Data, BaseViewHolder>(R.layout.item_rv_my_contract) {
        override fun convert(helper: BaseViewHolder, item: MyContractListBean.Data?) {
            bothNotNull(helper, item) { a, b ->
                var textColors = arrayOf("#929FAB", "#FFFFFF")
                a.getView<TextView>(R.id.tv_num).text = "${b.device_id}"
                a.getView<TextView>(R.id.tv_model).text = "${b.model_str}"
                a.getView<TextView>(R.id.tv_rent_long).text = "${b.renttime_str}"
                a.getView<TextView>(R.id.tv_base_package).text =
                    TextUtil.getSpannableString(arrayOf("基本套餐：", b.renttime_str), textColors)
                a.setText(
                    R.id.tv_base_package_time,
                    "${formatTime(b.begin_time)}至${formatTime(b.end_time)}"
                )


                val option = b.userOptions.filter { it.option_type == "2" }
                if (option.isEmpty()) {
                    a.setVisible(R.id.tv_change_package_time, false)
                        .setText(R.id.tv_change_package,  TextUtil.getSpannableString(arrayOf("换电套餐：", "暂无"), textColors))
                } else {
                    a.setVisible(R.id.tv_change_package_time, true)
                        .setText(
                            R.id.tv_change_package_time,
                            "${formatTime(option[0].start_time)}至${formatTime(option[0].end_time)}"
                        )
                        .setText(R.id.tv_change_package, TextUtil.getSpannableString(arrayOf("换电套餐：", "${option[0].name}"), textColors))
                }


                a.getView<QMUIRoundFrameLayout>(R.id.qrf_sign).apply {
                    visibility = if (b.btn_sign == 1) View.VISIBLE else View.GONE
                    setOnClickListener {
                        startFgt(
                            FgtContractSignStep1.newInstance(
                                b.contract_id,
                                qStr,
                                contractType
                            )
                        )
                    }
                }

                a.getView<FrameLayout>(R.id.qrf_return).apply {
                    visibility = if (b.btn_return == 1) View.VISIBLE else View.GONE
                    setOnClickListener { tryReturnDeposit(b.contract_id) }
                }

                a.getView<View>(R.id.ll_content).setOnClickListener {
                    startFgt(FgtMyContractDetail.newInstance(b.contract_id, b.device_id))
                }
            }
        }

        private fun formatTime(time: String): String {
            return if ( !TextUtils.isEmpty(time) && time.length > 10) time.replace("-","/").substring(0, 10) else time
        }

    }
}