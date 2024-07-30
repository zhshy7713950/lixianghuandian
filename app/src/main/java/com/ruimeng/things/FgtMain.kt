package com.ruimeng.things


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.FgtMe
import com.ruimeng.things.me.contract.FgtMyContract
import com.ruimeng.things.net_station.FgtNetStation
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
        fgts[currentIndex].onHiddenChanged(hidden)
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        EventBus.getDefault().register(this)
        instance = this
        fgts = arrayOf(
            FgtHome(),
            FgtNetStation(),
            FgtMyContract(),
            FgtMe()
        )



        val tabs = arrayOf(R.id.iv_home,R.id.iv_nearby,R.id.iv_contract,R.id.iv_me)
        for (i in 0..3){
            var tabView = rootView.findViewById(tabs[i]) as ImageView
            tabView.setOnClickListener { initTab(i,fgts) }
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

        loadMultipleRootFragment(R.id.fl_fgt_main, 0, *fgts)


//        NoReadLiveData.getInstance().simpleObserver(this) { data: NoReadBean.Data ->
//            msgTab.setMessageNumber(data.msg.msg_noread)
//            meTab.setMessageNumber(data.my.contract_total)
//        }

        NoReadLiveData.refresh { }
    }

    class SwitchPageEvent(i: Int) {
        var page :Int = 0
    }
    @Subscribe
    public fun switchPage(switchPageEvent: SwitchPageEvent) {
        initTab(switchPageEvent.page,fgts)
    }

    private fun initTab(index:Int,fgts:Array< MainTabFragment>){
        this.currentIndex = index
        val tabImg = arrayOf(R.mipmap.tab_home,R.mipmap.tab_nearby,R.mipmap.tab_contract,R.mipmap.tab_me)
        val tabImgSe = arrayOf(R.mipmap.tab_home_se,R.mipmap.tab_nearby_se,R.mipmap.tab_contract_se,R.mipmap.tab_me_se)
        val tabs = arrayOf(R.id.iv_home,R.id.iv_nearby,R.id.iv_contract,R.id.iv_me)
        for (i in 0..3){
            var tabView = rootView.findViewById(tabs[i]) as ImageView
            if (i == index){
                tabView.setImageResource(tabImgSe[i])
            }else{
                tabView.setImageResource(tabImg[i])
            }
            showHideFragment(fgts[index])
        }

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