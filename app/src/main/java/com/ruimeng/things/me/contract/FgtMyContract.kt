package com.ruimeng.things.me.contract

import android.graphics.Color
import android.os.Bundle

import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.qmuiteam.qmui.util.QMUIDisplayHelper
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.ruimeng.things.NoReadLiveData
import com.ruimeng.things.R
import com.ruimeng.things.net_station.FgtNetStationItem
import kotlinx.android.synthetic.main.fgt_my_contract.*
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import wongxd.base.BaseBackFragment
import wongxd.base.MainTabFragment
import wongxd.base.custom.caneffect.CanRippleLayout
import wongxd.base.custom.caneffect.CanShadowDrawable
import wongxd.common.dp2px

/**
 * Created by wongxd on 2019/12/24.
 */
class FgtMyContract : MainTabFragment() {

    companion object{
        var qStr = ""
    }
    override fun getLayoutRes(): Int = R.layout.fgt_my_contract



    private var currentTabIndex = 0

    data class EventDoContractSearch(val q: String = "", val type: Int = 1)
    var  currentPage : FgtMyContractItem? = null

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        val tabv = arrayOf(R.id.v1,R.id.v2,R.id.v3,R.id.v4)

        vp_my_contract.apply {
            //0正常1过期2未完成3历史
            val vpList = listOf(
                VpBean(FgtMyContractItem.newInstance(0), "正常合约"),
                VpBean(FgtMyContractItem.newInstance(1), "过期合约"),
                VpBean(FgtMyContractItem.newInstance(2), "未完合约"),
                VpBean(FgtMyContractItem.newInstance(3), "历史合约")
            )
            currentPage = vpList[0].fgt
            offscreenPageLimit = vpList.size
            adapter = VpAdapter(vpList)
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {

                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    currentTabIndex = position
                    qfl_search_my_contract.performClick()
                    for (i in 0..3){
                        var tabBg = rootView.findViewById(tabv[i]) as View
                        if (i == position){
                            tabBg.setBackgroundResource( R.drawable.rectangle_btn_bg)
                        }else{
                            tabBg.setBackgroundColor(resources.getColor(R.color.transparent))
                        }
                    }
                    vpList[position].fgt.refresh()
                    currentPage = vpList[position].fgt
                }
            })
        }

        setTab()






        et_search_my_contract.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                qStr = s?.toString() ?: ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        CanShadowDrawable.Builder.on(qfl_search_my_contract)

            .radius(5.dp2px)
//            .shadowColor(Color.parseColor("#333333"))
            .bgColor(resources.getColor(R.color.app_color))
//            .shadowRange(5.dp2px)
            .offsetTop(5.dp2px)
            .offsetBottom(5.dp2px)
            .offsetLeft(5.dp2px)
            .offsetRight(5.dp2px)
            .create()

        CanRippleLayout.Builder.on(qfl_search_my_contract).rippleCorner(5.dp2px).create()

        qfl_search_my_contract.setOnClickListener {
            EventBus.getDefault().post(EventDoContractSearch(qStr, currentTabIndex))
        }

        NoReadLiveData.getInstance().simpleObserver(this) { data ->
            suffixExp = data.my.contract_exp
            suffixNotComplete = data.my.contract_nocomplete
            setTab()
        }


        NoReadLiveData.refresh()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            currentPage?.refresh()
        }
    }

    private fun setTab() {
        tab_my_contract.apply {
            reset()
            mode = QMUITabSegment.MODE_FIXED
            setDefaultNormalColor(Color.parseColor("#929FAB"))
            setDefaultSelectedColor(Color.parseColor("#29EBB6"))
            setHasIndicator(false)
            setupWithViewPager(vp_my_contract)
            if (suffixExp > 0){
                var tab = tab_my_contract.getTab(1)
                tab.setSignCountMargin(-QMUIDisplayHelper.dp2px(context, 8), -QMUIDisplayHelper.dp2px(context, 8))
                tab.showSignCountView(context,suffixExp)
            }
            if (suffixNotComplete > 0){
                var tab = tab_my_contract.getTab(2)
                tab.setSignCountMargin(-QMUIDisplayHelper.dp2px(context, 8), -QMUIDisplayHelper.dp2px(context, 8))
                tab.showSignCountView(context,suffixNotComplete)
            }

            notifyDataChanged()
        }


    }

    private var suffixExp = 0
    private var suffixNotComplete = 0

    inner class VpBean(val fgt: FgtMyContractItem, val title: String)

    inner class VpAdapter(val list: List<VpBean>) :
        FragmentStatePagerAdapter(childFragmentManager) {
        override fun getItem(position: Int): Fragment = list[position].fgt

        override fun getCount(): Int = list.size

        override fun getPageTitle(position: Int): CharSequence? {
            val oriTitle = list[position].title

            var title: CharSequence = oriTitle

//            if (position == 1) {
//                if (suffixExp != 0) {
//                    title = Html.fromHtml("$oriTitle<font color='#FF5757'>(${suffixExp})</font>")
//                }
//
//            } else if (position == 2) {
//                if (suffixNotComplete != 0) {
//                    title =
//                        Html.fromHtml("$oriTitle<font color='#FF5757'>(${suffixNotComplete})</font>")
//                }
//
//            }

            return title
        }
    }

}