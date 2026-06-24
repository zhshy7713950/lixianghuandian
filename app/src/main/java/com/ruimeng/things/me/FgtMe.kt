package com.ruimeng.things.me

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ruimeng.things.*
import com.ruimeng.things.ads.AMPSNativeAdLoader
import com.ruimeng.things.ads.AdManager
import com.ruimeng.things.bean.myVipLevel
import com.ruimeng.things.bean.NoReadBean
import com.ruimeng.things.bean.UserInfoBean
import com.ruimeng.things.bean.bgImage
import com.ruimeng.things.bean.description
import com.ruimeng.things.bean.isCD
import com.ruimeng.things.bean.isGA
import com.ruimeng.things.bean.isNC
import com.ruimeng.things.bean.isSH
import com.ruimeng.things.bean.showName
import com.ruimeng.things.bean.stateImage
import com.ruimeng.things.common.BannerHelper
import com.ruimeng.things.home.CustomerServiceFragment
import com.ruimeng.things.home.FgtChangeMobile
import com.ruimeng.things.home.FgtFollowWechatAccount
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.bean.BannerInfo
import com.ruimeng.things.me.activity.AtyWeb2
import com.ruimeng.things.me.activity.DistributionCenterActivity
import com.ruimeng.things.me.activity.WithdrawalAccountActivity
import com.ruimeng.things.msg.FgtMsg
import com.ruimeng.things.me.contract.FgtMyContract
import com.ruimeng.things.utils.CustomerServiceManager
import com.utils.WeChatHelper
import com.utils.safeToFloat
import kotlinx.android.synthetic.main.fgt_me.*
import kotlinx.android.synthetic.main.fgt_scan_open.ad_container
import kotlinx.android.synthetic.main.fgt_setting.tv_version_setting
import kotlinx.android.synthetic.main.home_status_item.banner
import kotlinx.coroutines.launch
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wongxd.base.MainTabFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.getCurrentAppAty
import wongxd.common.loadBackgroundImg
import wongxd.common.loadCircleImg
import wongxd.common.loadImg
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.SystemUtils

/**
 * Created by wongxd on 2018/11/9.
 */
class FgtMe : MainTabFragment() {

    private val vmMain: MainViewModel by activityViewModels()
    private var adLoader: AMPSNativeAdLoader? = null

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
//        initTopbar(mView?.findViewById(R.id.topbar), "我的", false)
        EventBus.getDefault().register(this)
        initEvent()

        // 初始化广告加载器
        initAdLoader()
        InfoViewModel.getDefault().userInfo.simpleObserver(this) { userinfo ->

            myVipLevel(userinfo.online_time)?.let {
                llVipState.loadBackgroundImg(it.bgImage())
                ivVipState.loadImg(it.stateImage())
                tvVipState.text = it.stateCN
                tvVipState.setTextColor(it.stateColor)
                tvVipDescription.text = it.description(userinfo.online_time)
                tvVipDescription.setTextColor(it.descriptionColor)
            } ?: run {
                llVipState.isVisible = false
            }

            if (!TextUtils.isEmpty(userinfo.logo)) {
                iv_header_me.loadCircleImg(userinfo.logo)
            }

            tv_username_me.text = userinfo.showName()

            rtv_truename_status.apply {


                val dra =
                    resources.getDrawable(if (userinfo.realname_auth == 1) R.mipmap.icon_truename_me else R.mipmap.icon_not_truename_me)
                        .apply {
                            setBounds(0, 0, minimumWidth, minimumHeight)
                        }

                setCompoundDrawables(dra, null, null, null)

                setTextColor(if (userinfo.realname_auth == 1) Color.parseColor("#F79C26") else Color.WHITE)

                text = if (userinfo.realname_auth == 1) "已认证" else "未认证"


//                val del = delegate as RoundViewDelegate
//                del.strokeColor = if (userinfo.realname_auth == 1) Color.GREEN else Color.WHITE


                setOnClickListener {
                    if (userinfo.realname_auth != 1)
                        startFgt(FgtTrueName())
                    else {
                        EasyToast.DEFAULT.show("您已实名")
                    }
                }
            }


            rtv_my_referrer_status.apply {
                isVisible = userinfo.hasRecom != 0
                if (userinfo.hasRecom != 0) {
                    text = "我的推荐官"
                    val iconRes =
                        if (userinfo.hasRecom == 2) R.drawable.ic_my_referrer_se else R.drawable.ic_my_referrer
                    val textColorStr = if (userinfo.hasRecom == 2) "#FBC045" else "#B2C1CE"

                    val dra = resources.getDrawable(iconRes).apply {
                        setBounds(0, 0, minimumWidth, minimumHeight)
                    }
                    setCompoundDrawables(dra, null, null, null)
                    setTextColor(Color.parseColor(textColorStr))
                }

                setOnClickListener {
                    start(FgtMyRecommendOfficer())
                }
            }



            tv_money_me.text = "" + userinfo.devicenumber

            updateDepositUI(userinfo)

            renderMenus(userinfo)
        }

        ll_setting_me.setOnClickListener { startFgt(FgtSetting()) }



        NoReadLiveData.getInstance().simpleObserver(this) { data: NoReadBean.Data ->

            tvMyContractUnread?.apply {
                visibility = if (data.my.contract_total == 0) View.GONE else View.VISIBLE
                text = data.my.contract_total.toString()
            }

        }


        srl_me?.setEnableLoadMore(false)

        srl_me.setOnRefreshListener {

            http {
                url = Path.USERINFO

                onSuccess {
                    val result = it.toPOJO<UserInfoBean>().data.userinfo
                    UserInfoLiveData.setToString(result)
                }

                onFinish {
                    srl_me.finishRefresh()
                }


            }

        }
        tv_ya_money_me.setOnClickListener {
            startFgt(FgtMeDeposit())
        }
        tv_ya_money_me_title.setOnClickListener {
            startFgt(FgtMeDeposit())
        }

        //1,获取包 管理器
        val packageManager = activity?.packageManager
        //2,通过上下文获取包名
        val packageName = activity?.packageName
        //3,获取包的信息
        val packageInfo = packageManager?.getPackageInfo(packageName ?: "", 0)
        //4,获取版本号
        val versionCode = packageInfo?.versionCode
        //5,获取版本名
        val versionName = packageInfo?.versionName ?: "未知版本"

        tv_version_setting.text = "当前版本:v$versionName($versionCode)"
    }

    private fun initEvent() {
//        lifecycleScope.launchWhenCreated {
//            launch {
//                // 观察 banner 数据
//                vmMain.meBannerData.observe(viewLifecycleOwner) { bannerList ->
//                    setupBanner(bannerList)
//                }
//            }
//        }
    }

    // 设置 Banner
//    private fun setupBanner(bannerList: List<BannerInfo>) {
//        BannerHelper.setupBanner(banner, bannerList, this)
//    }

    private fun updateDepositUI(userinfo: UserInfoBean.Data.UserInfo) {
        if (view == null) return
        tv_ya_money_me?.text = "无"
        tv_ya_money_me?.isEnabled = false
        tv_ya_money_me_title?.isEnabled = false

        val currentDeviceId = FgtHome.CURRENT_DEVICEID
        val depositInfoArr = userinfo.depositInfoArr

        if (!depositInfoArr.isNullOrEmpty() && currentDeviceId.isNotEmpty()) {
            val matchedDepositInfo = depositInfoArr.find { it.deviceId == currentDeviceId }
            if (matchedDepositInfo != null) {
                tv_ya_money_me?.isEnabled = true
                tv_ya_money_me_title?.isEnabled = true
                if (matchedDepositInfo.freeMark == 0) {
                    tv_ya_money_me?.text = matchedDepositInfo.amount
                } else {
                    tv_ya_money_me?.text = when (matchedDepositInfo.payType) {
                        99 -> "线下免押"
                        101 -> "芝麻免押"
                        102 -> "集团免押"
                        else -> "免押权益"
                    }
                }
            }
        }
    }

    fun startFgt(toFgt: SupportFragment) {
        (parentFragment as FgtMain).start(toFgt)
    }

    override fun getLayoutRes(): Int = R.layout.fgt_me
    class RefreshMe

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            srl_me?.autoRefresh()
            
            // 每次页面重新显示时，刷新押金UI，以防首页切换了电池
            val userInfo = UserInfoLiveData.getInstance().value
            if (userInfo != null && view != null) {
                updateDepositUI(userInfo)
            }
        }
    }

    @Subscribe
    public fun refreshStation(event: RefreshMe) {
        srl_me?.autoRefresh()
    }

    /**
     * 初始化广告加载器
     */
    private fun initAdLoader() {
        adLoader = AMPSNativeAdLoader(requireActivity(), viewLifecycleOwner.lifecycle)
        adLoader?.commonLoadInto(ad_container, AdManager.NATIVE_SPACE_ID_ME)
    }

    private var tvMyContractUnread: TextView? = null

    data class MeMenuItem(
        val id: String,
        val iconRes: Int,
        val title: String,
        val onClick: () -> Unit,
        var isVisible: Boolean = true,
        val hasBadge: Boolean = false
    )

    private fun renderMenus(userinfo: UserInfoBean.Data.UserInfo?) {
        ll_menus_container.removeAllViews()

        val menus = mutableListOf<MeMenuItem>()

        menus.add(MeMenuItem("ticket", R.drawable.ic_my_contract, "我的合约", { startFgt(FgtMyContract()) }, true))

        menus.add(MeMenuItem("customerService", R.mipmap.ic_service_center, "客服中心", { startFgt(CustomerServiceFragment.newInstance()) }, true))

        menus.add(MeMenuItem("support", R.mipmap.service_support_me, "客服热线", { CustomerServiceManager.showDialSheet(requireActivity()) }, true))

        menus.add(MeMenuItem("changeMobile", R.mipmap.ic_change_mobile, "变更手机号", { startFgt(FgtChangeMobile.newInstance(FgtChangeMobile.VERIFY_TYPE)) }, true))

        menus.add(MeMenuItem("myCoupon", R.mipmap.ic_my_coupon, "我的优惠券", { startFgt(FgtMyCoupon.newInstance()) }, true))

        val showReferrer = userinfo != null && (userinfo.isSH() || userinfo.isGA() || userinfo.isNC())
        menus.add(MeMenuItem("referrer", R.mipmap.service_my_referrer, "推荐有礼", { startFgt(FgtRecommendGift()) }, showReferrer))

        menus.add(MeMenuItem("followWechat", R.drawable.ic_wx, "关注公众号", {
            WeChatHelper.launchWXMiniProgram(requireContext(), resources.getString(R.string.wx_appid), "/pages/基础/关注公众号/followWechat")
        }, true))

        val showTerminate = userinfo != null && !userinfo.isCD() && tv_ya_money_me.isEnabled
        menus.add(MeMenuItem("terminate", R.drawable.ic_terminate, "退租", { startFgt(FgtMeDeposit()) }, showTerminate))

        menus.add(MeMenuItem("lixiangMiniProgram", R.drawable.ic_wx_mini_program, "锂享小程序", {
            WeChatHelper.launchWXMiniProgram(requireContext(), resources.getString(R.string.wx_appid), "")
        }, true))

        val visibleMenus = menus.filter { it.isVisible }

        val chunked = visibleMenus.chunked(4)
        for (rowMenus in chunked) {
            val rowLayout = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (20 * resources.displayMetrics.density).toInt()
                }
            }

            for (i in 0 until 4) {
                if (i < rowMenus.size) {
                    val item = rowMenus[i]
                    val itemView = createMenuItemView(item)
                    rowLayout.addView(itemView)
                } else {
                    val placeholder = createMenuItemView(MeMenuItem("", R.mipmap.ic_about, "", {}))
                    placeholder.visibility = View.INVISIBLE
                    rowLayout.addView(placeholder)
                }
            }
            ll_menus_container.addView(rowLayout)
        }
    }

    private fun createMenuItemView(item: MeMenuItem): View {
        val density = resources.displayMetrics.density
        val padding = (5 * density).toInt()

        val container = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            setPadding(padding, padding, padding, padding)
            if (item.id.isNotEmpty()) {
                setOnClickListener { item.onClick() }
            }
        }

        val iconContainer = android.widget.FrameLayout(requireContext()).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
        }

        val icon = ImageView(requireContext()).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            if (item.iconRes != 0) {
                setImageResource(item.iconRes)
            }
        }
        iconContainer.addView(icon)

        if (item.hasBadge) {
            tvMyContractUnread = com.flyco.roundview.RoundTextView(requireContext()).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.TOP or android.view.Gravity.END
                    leftMargin = (12 * density).toInt()
                }
                setPadding(padding, 0, padding, 0)
                setTextColor(Color.WHITE)
                textSize = 10f
                visibility = View.GONE
                delegate.backgroundColor = Color.parseColor("#E64141")
                delegate.cornerRadius = (5 * density).toInt()
            }
            iconContainer.addView(tvMyContractUnread)
            
            val data = NoReadLiveData.getInstance().value
            if (data != null) {
                tvMyContractUnread?.visibility = if (data.my.contract_total == 0) View.GONE else View.VISIBLE
                tvMyContractUnread?.text = data.my.contract_total.toString()
            }
        }

        container.addView(iconContainer)

        val title = TextView(requireContext()).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                topMargin = (8 * density).toInt()
            }
            gravity = android.view.Gravity.CENTER
            text = item.title
            setTextColor(Color.WHITE)
            textSize = 12f
        }
        container.addView(title)

        return container
    }
}
