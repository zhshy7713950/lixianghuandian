package com.ruimeng.things


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.ruimeng.things.bean.NoReadBean
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.FgtMe
import com.ruimeng.things.msg.FgtMsg
import com.ruimeng.things.net_station.FgtNetStation
import com.ruimeng.things.shop.FgtShoppingMall
import me.majiajie.pagerbottomtabstrip.NavigationController
import me.majiajie.pagerbottomtabstrip.PageNavigationView
import me.majiajie.pagerbottomtabstrip.item.BaseTabItem
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wongxd.base.FgtBase
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


    var navigationController: NavigationController? = null

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        EventBus.getDefault().register(this)
        instance = this

        val fgts = arrayOf(
            FgtHome(),
            FgtNetStation(),
            FgtMsg(),
            FgtShoppingMall(),
            FgtMe()
        )

        val tab = rootView.findViewById<View>(R.id.tab) as PageNavigationView


        val msgTab = newItem(R.drawable.tab_msg, R.drawable.tab_msg_se, "消息")
        val meTab = newItem(R.drawable.tab_me, R.drawable.tab_me_se, "我的")
        navigationController = tab.custom()
            .addItem(newItem(R.drawable.tab_home, R.drawable.tab_home_se, "首页"))
            .addItem(newItem(R.drawable.tab_nearby, R.drawable.tab_nearby_se, "网点"))
            .addItem(msgTab)
            .addItem(newItem(R.drawable.tab_shop, R.drawable.tab_shop_se, "商城"))
            .addItem(meTab)
            .build()

        navigationController?.addSimpleTabItemSelectedListener { index, old ->
            showHideFragment(fgts[index])
            NoReadLiveData.refresh { }
        }

        loadMultipleRootFragment(R.id.fl_fgt_main, 0, *fgts)


        NoReadLiveData.getInstance().simpleObserver(this) { data: NoReadBean.Data ->
            msgTab.setMessageNumber(data.msg.msg_noread)
            meTab.setMessageNumber(data.my.contract_total)
        }

        NoReadLiveData.refresh { }
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
        navigationController?.setSelect(event.pos)
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        instance = null
        super.onDestroyView()
    }

}