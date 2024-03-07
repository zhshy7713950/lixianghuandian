package com.ruimeng.things.me

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ruimeng.things.*
import com.ruimeng.things.bean.NoReadBean
import com.ruimeng.things.bean.UserInfoBean
import com.ruimeng.things.home.FgtFollowWechatAccount
import com.ruimeng.things.me.activity.AtyWeb2
import com.ruimeng.things.me.activity.DistributionCenterActivity
import com.ruimeng.things.me.activity.WithdrawalAccountActivity
import com.ruimeng.things.msg.FgtMsg
import com.ruimeng.things.net_station.FgtNetStationItem
import kotlinx.android.synthetic.main.fgt_home.srl_home
import kotlinx.android.synthetic.main.fgt_me.*
import kotlinx.android.synthetic.main.fgt_net_station_item.srl_station
import me.yokeyword.fragmentation.SupportFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wongxd.base.MainTabFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.EasyToast
import wongxd.common.getCurrentAppAty
import wongxd.common.loadImg
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.SystemUtils

/**
 * Created by wongxd on 2018/11/9.
 */
class FgtMe : MainTabFragment() {
    override fun initView(mView: View?, savedInstanceState: Bundle?) {
//        initTopbar(mView?.findViewById(R.id.topbar), "我的", false)
        EventBus.getDefault().register(this)
        InfoViewModel.getDefault().userInfo.simpleObserver(this) { userinfo ->

            if (!TextUtils.isEmpty(userinfo.logo)){
                iv_header_me.loadImg(userinfo.logo)
            }

            tv_username_me.text =
                if (userinfo.nickname.isBlank()) userinfo.mobile else userinfo.nickname


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


            rtv_bind_wechat_status.apply {


                val isMpFollow = userinfo.mp_follow == 1

                val dra =
                    resources.getDrawable(if (isMpFollow) R.mipmap.icon_bind_wecaht_me else R.mipmap.icon_not_bind_wecaht_me)
                        .apply {
                            setBounds(0, 0, minimumWidth, minimumHeight)
                        }

                setCompoundDrawables(dra, null, null, null)

                setTextColor(if (isMpFollow) Color.GREEN else Color.WHITE)

                text = if (isMpFollow) "已绑定" else "未绑定"

//                val del = delegate as RoundViewDelegate
//                del.strokeColor =
//                    if (isMpFollow) Color.GREEN else Color.WHITE


                setOnClickListener {
                    if (isMpFollow) {
                        EasyToast.DEFAULT.show("已关注公众号")
                    } else {
                        start(FgtFollowWechatAccount())
                    }
                }
            }



            tv_money_me.text = "" + userinfo.devicenumber

            tv_ya_money_me.text = "" + userinfo.devicedeposit

        }


        ll_ticket_me.setOnClickListener {
            startFgt(FgtTicket())
        }

        ll_safe_center.setOnClickListener { startFgt(FgtSafeCenter()) }


        ll_about_us.setOnClickListener {   http {
            method = "get"
            url = Path.ABOUT_ME

            onResponse {
                AtyWeb2.start("关于我们", it)
            }
        } }

        ll_setting_me.setOnClickListener { startFgt(FgtSetting()) }

        ll_msg.setOnClickListener { startFgt(FgtMsg()) }

        ll_support_me.setOnClickListener {
            val tel = "4000283969"
            AnyLayer.with(getCurrentAppAty())
                .contentView(R.layout.alert_phone_call_dialog)
                .bindData { anyLayer ->
                    anyLayer.contentView.findViewById<TextView>(R.id.tvTitle).setText(tel)
                    anyLayer.contentView.findViewById<View>(R.id.fl_call).setOnClickListener{
                        SystemUtils.call(activity, tel)
                        anyLayer.dismiss()
                    }
                    anyLayer.contentView.findViewById<ImageView>(R.id.ivClose).setOnClickListener{
                        anyLayer.dismiss()
                    }
                }.backgroundColorInt(Color.parseColor("#85000000"))
                .backgroundBlurRadius(10f)
                .backgroundBlurScale(10f)
                .show()
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
            if ( tv_ya_money_me.text != "0.00"){
                startFgt(FgtMeDeposit())
            }

        }
    }

    fun startFgt(toFgt: SupportFragment) {
        (parentFragment as FgtMain).start(toFgt)
    }

    override fun getLayoutRes(): Int = R.layout.fgt_me
    class RefreshMe
    @Subscribe
    public fun refreshStation(event: RefreshMe) {
        srl_me?.autoRefresh()
    }
}