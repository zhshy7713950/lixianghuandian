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
import com.ruimeng.things.bean.showName
import com.ruimeng.things.bean.stateImage
import com.ruimeng.things.common.BannerHelper
import com.ruimeng.things.home.FgtChangeMobile
import com.ruimeng.things.home.FgtFollowWechatAccount
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
                    EasyToast.DEFAULT.show("功能开发中")

                }
            }



            tv_money_me.text = "" + userinfo.devicenumber

            tv_ya_money_me.text = showDeposit(userinfo.freeMark, userinfo.devicedeposit)

            ll_my_referrer.isVisible = "上海市" == userinfo.city

            if ("成都市" != userinfo.city && tv_ya_money_me.isEnabled) {
                llTerminate.isVisible = true
                llPlaceHolder5.visibility = View.GONE
            } else {
                llTerminate.isVisible = false
                llPlaceHolder5.visibility = View.INVISIBLE
            }
        }

        llTerminate.setOnClickListener {
            startFgt(FgtMeDeposit())
        }

        llChangeMobile.setOnClickListener {
            startFgt(FgtChangeMobile.newInstance(FgtChangeMobile.VERIFY_TYPE))
        }

        ll_ticket_me.setOnClickListener {
            startFgt(FgtMyContract())
        }

        ll_safe_center.setOnClickListener { startFgt(FgtSafeCenter()) }


        ll_about_us.setOnClickListener {
            http {
                method = "get"
                url = Path.ABOUT_ME

                onResponse {
                    AtyWeb2.start("关于我们", it)
                }
            }
        }

        ll_setting_me.setOnClickListener { startFgt(FgtSetting()) }

        ll_msg.setOnClickListener { startFgt(FgtMsg()) }

        ll_support_me.setOnClickListener {
            CustomerServiceManager.showDialSheet(requireActivity())
//            NormalDialog(activity).apply {
            //                style(NormalDialog.STYLE_TWO)
            //                title("售后支持")
            //                titleTextColor(Color.parseColor("#131414"))
            //                content(tel)
            //                contentGravity(Gravity.CENTER)
            //                btnText("取消", "拨打")
            //                btnTextColor(Color.parseColor("#ABABAB"), Color.parseColor("#000000"))
            //                setOnBtnClickL(OnBtnClickL {
            //                    dismiss()
            //                }, OnBtnClickL {
            //                    SystemUtils.call(activity, tel)
            //                    dismiss()
            //                })
            //                show()
            //            }
        }

        ll_follow_wechat.setOnClickListener {
            WeChatHelper.launchWXMiniProgram(
                requireContext(),
                resources.getString(R.string.wx_appid),
                "/pages/基础/关注公众号/followWechat"
            )
        }

        ll_my_referrer.setOnClickListener {
            startFgt(FgtRecommendGift())
        }



        NoReadLiveData.getInstance().simpleObserver(this) { data: NoReadBean.Data ->

            tv_my_contract_unread?.apply {
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
        distributionCenterLayout?.setOnClickListener {
            startActivity(Intent(activity, DistributionCenterActivity::class.java))
        }
        withdrawalAccountLayout?.setOnClickListener {
            startActivity(Intent(activity, WithdrawalAccountActivity::class.java))
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

    private fun showDeposit(freeMark: String?, deviceDeposit: String?): String {
        return if (deviceDeposit.safeToFloat() > 0) {
            tv_ya_money_me.isEnabled = true
            tv_ya_money_me_title.isEnabled = true
            return deviceDeposit ?: "0.00"
        } else if (freeMark == "1") {//存在免押
            tv_ya_money_me.isEnabled = true
            tv_ya_money_me_title.isEnabled = true
            "已免押"
        } else {
            tv_ya_money_me.isEnabled = false
            tv_ya_money_me_title.isEnabled = false
            deviceDeposit ?: "0.00"
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
}
