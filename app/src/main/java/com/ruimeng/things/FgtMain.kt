package com.ruimeng.things


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.FgtMe
import com.ruimeng.things.me.FgtTicket
import com.ruimeng.things.me.contract.FgtMyContract
import com.ruimeng.things.net_station.FgtNetStation
import com.ruimeng.things.net_station.FgtNetStationMap
import me.majiajie.pagerbottomtabstrip.item.BaseTabItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wongxd.base.FgtBase
import wongxd.base.MainTabFragment
import wongxd.base.custom.SpecialTab
import wongxd.common.EasyToast


/**
 * Created by wongxd on 2018/10/19.
 */
class FgtMain : FgtBase() {

    companion object {
        private const val KEY_CURRENT_INDEX = "fgt_main_current_index"

        @SuppressLint("StaticFieldLeak")
        var instance: FgtMain? = null

        data class SwitchTabEvent(val pos: Int)
    }

    override fun getLayoutRes(): Int = R.layout.fgt_main
    lateinit var fgts: Array<MainTabFragment>
    private var currentIndex: Int = 0

//    var navigationController: NavigationController? = null

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (::fgts.isInitialized) {
            fgts[currentIndex].onHiddenChanged(hidden)
        }
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        EventBus.getDefault().register(this)
        instance = this
        currentIndex = savedInstanceState
            ?.getInt(KEY_CURRENT_INDEX, 0)
            ?.coerceIn(0, 3)
            ?: 0
        fgts = if (savedInstanceState == null) {
            arrayOf(
                FgtHome(),
//                FgtNetStation(),
                FgtNetStationMap(),
                FgtTicket(),
                FgtMe()
            )
        } else {
            arrayOf(
                requireNotNull(findChildFragment(FgtHome::class.java)),
                requireNotNull(findChildFragment(FgtNetStationMap::class.java)),
                requireNotNull(findChildFragment(FgtTicket::class.java)),
                requireNotNull(findChildFragment(FgtMe::class.java))
            )
        }



        val imageTabs = arrayOf(R.id.iv_home,R.id.iv_nearby,R.id.iv_contract,R.id.iv_me)
        val containerTabs = arrayOf(R.id.ll_home,R.id.ll_nearby,R.id.ll_contract,R.id.ll_me)
        for (i in 0..3){
            val tabImageView = rootView.findViewById(imageTabs[i]) as ImageView
            tabImageView.setOnClickListener { initTab(i,fgts) }
            val tabContainer = rootView.findViewById<View>(containerTabs[i])
            tabContainer.setOnClickListener { initTab(i,fgts) }
        }

//
//        val msgTab = newItem(R.drawable.tab_msg, R.drawable.tab_msg_se, "消息")
//        val meTab = newItem(R.drawable.tab_me, R.drawable.tab_me_se, "我的")
//        navigationController = tab.custom()
//            .addItem(newItem(R.drawable.tab_home, R.drawable.tab_home_se, "首页"))
//            .addItem(newItem(R.drawable.tab_nearby, R.drawable.tab_nearby_se, "网点"))
//            .addItem(msgTab)
//            .addItem(newItem(R.drawable.tab_shop, R.drawable.tab_shop_se, "商城"))
//            .addItem(meTab)
//            .build()

//        navigationController?.addSimpleTabItemSelectedListener { index, old ->
//            showHideFragment(fgts[index])
//            NoReadLiveData.refresh { }
//        }

        if (savedInstanceState == null) {
            loadMultipleRootFragment(R.id.fl_fgt_main, currentIndex, *fgts)
        }
        renderTab(currentIndex)


//        NoReadLiveData.getInstance().simpleObserver(this) { data: NoReadBean.Data ->
//            msgTab.setMessageNumber(data.msg.msg_noread)
//            meTab.setMessageNumber(data.my.contract_total)
//        }

        NoReadLiveData.refresh { }
    }

    data class SwitchPageEvent(var page :Int)

    @Subscribe
    fun switchPage(switchPageEvent: SwitchPageEvent) {
        initTab(switchPageEvent.page,fgts)
    }

    private fun initTab(index:Int,fgts:Array< MainTabFragment>){
        this.currentIndex = index
        renderTab(index)
        // 显示选中的Fragment，隐藏其他Fragment
        showHideFragment(fgts[index])
    }

    private fun renderTab(index: Int) {
        // 修复tab图标数组，使其与Fragment数组正确对应
        // Fragment顺序：[FgtHome, FgtNetStationMap, FgtTicket, FgtMe]
        // Tab ID顺序：[iv_home, iv_nearby, iv_contract, iv_me]
        val tabImg = arrayOf(R.mipmap.tab_home,R.mipmap.tab_nearby,R.mipmap.tab_ticket,R.mipmap.tab_me)
        val tabImgSe = arrayOf(R.mipmap.tab_home_se,R.mipmap.tab_nearby_se,R.mipmap.tab_tickey_se,R.mipmap.tab_me_se)
        val imageTabs = arrayOf(R.id.iv_home,R.id.iv_nearby,R.id.iv_contract,R.id.iv_me)
        val textTabs = arrayOf(R.id.tv_home,R.id.tv_nearby,R.id.tv_contract,R.id.tv_me)
        for (i in 0..3){
            val tabView = rootView.findViewById(imageTabs[i]) as ImageView
            val tvView = rootView.findViewById(textTabs[i]) as android.widget.TextView
            if (i == index){
                tabView.setImageResource(tabImgSe[i])
                tvView.setTextColor(Color.parseColor("#2fe4af"))
            }else{
                tabView.setImageResource(tabImg[i])
                tvView.setTextColor(Color.parseColor("#637989"))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_INDEX, currentIndex)
    }

    /**
     * 正常tab
     */
    private fun newItem(drawable: Int, checkedDrawable: Int, text: String): BaseTabItem {
        val mainTab = SpecialTab(activity)
        mainTab.initialize(drawable, checkedDrawable, text)
        mainTab.setTextDefaultColor(-0x777778)
        mainTab.setTextCheckedColor(activity?.resources?.getColor(R.color.app_color)!!)
        return mainTab
    }


    private var lastBackClick = 0L
    override fun onBackPressedSupport(): Boolean {

        val nowBackClick = System.currentTimeMillis()
        if (nowBackClick - lastBackClick > 900) {
            EasyToast.DEFAULT.show("再次返回退出应用")
            lastBackClick = nowBackClick
        } else {
            activity?.finish()
        }

        return true

    }


    @Subscribe
    fun switchToTab(event: SwitchTabEvent) {
//        navigationController?.setSelect(event.pos)
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        instance = null
        super.onDestroyView()
    }


}
