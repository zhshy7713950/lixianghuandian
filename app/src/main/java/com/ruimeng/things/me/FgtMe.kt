package com.ruimeng.things.me

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.flyco.roundview.RoundViewDelegate
import com.ruimeng.things.*
import com.ruimeng.things.bean.NoReadBean
import com.ruimeng.things.bean.UserInfoBean
import com.ruimeng.things.home.FgtFollowWechatAccount
import com.ruimeng.things.me.activity.DistributionCenterActivity
import com.ruimeng.things.me.activity.WithdrawalAccountActivity
import com.ruimeng.things.me.contract.FgtMyContract
import com.ruimeng.things.me.credit.FgtCreditSystem
import kotlinx.android.synthetic.main.fgt_me.*
import me.yokeyword.fragmentation.SupportFragment
import wongxd.base.MainTabFragment
import wongxd.common.EasyToast
import wongxd.common.loadImg
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.SystemUtils

/**
 * Created by wongxd on 2018/11/9.
 */
class FgtMe : MainTabFragment() {
    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        initTopbar(mView?.findViewById(R.id.topbar), "我的", false)

        InfoViewModel.getDefault().userInfo.simpleObserver(this) { userinfo ->

            iv_header_me.loadImg(userinfo.logo)

            tv_username_me.text =
                if (userinfo.mobile.isBlank()) userinfo.nickname else userinfo.mobile


            rtv_truename_status.apply {


                val dra =
                    resources.getDrawable(if (userinfo.realname_auth == 1) R.drawable.icon_truename_me else R.drawable.icon_not_truename_me)
                        .apply {
                            setBounds(0, 0, minimumWidth, minimumHeight)
                        }

                setCompoundDrawables(dra, null, null, null)

                setTextColor(if (userinfo.realname_auth == 1) Color.GREEN else Color.WHITE)

                text = if (userinfo.realname_auth == 1) "已实名认证" else "未实名认证"


                val del = delegate as RoundViewDelegate
                del.strokeColor = if (userinfo.realname_auth == 1) Color.GREEN else Color.WHITE




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
                    resources.getDrawable(if (isMpFollow) R.drawable.icon_bind_wecaht_me else R.drawable.icon_not_bind_wecaht_me)
                        .apply {
                            setBounds(0, 0, minimumWidth, minimumHeight)
                        }

                setCompoundDrawables(dra, null, null, null)

                setTextColor(if (isMpFollow) Color.GREEN else Color.WHITE)

                text = if (isMpFollow) "已关注公众号" else "未关注公众号"

                val del = delegate as RoundViewDelegate
                del.strokeColor =
                    if (isMpFollow) Color.GREEN else Color.WHITE


                setOnClickListener {
                    if (isMpFollow) {
                        EasyToast.DEFAULT.show("已关注公众号")
                    } else {
                        start(FgtFollowWechatAccount())
                    }
                }
            }



            tv_money_me.text = "设备数量：" + userinfo.devicenumber

            tv_ya_money_me.text = "账户押金：" + userinfo.devicedeposit

        }


        ll_ticket_me.setOnClickListener {
            //            EasyToast.DEFAULT.show("功能开发中，敬请期待。")
            startFgt(FgtTicket())
        }

        ll_safe_center.setOnClickListener { startFgt(FgtSafeCenter()) }


        ll_agnet_me.setOnClickListener { startFgt(FgtAgnet()) }

        ll_setting_me.setOnClickListener { startFgt(FgtSetting()) }

        ll_credit_me.setOnClickListener { startFgt(FgtCreditSystem()) }

        ll_contract_me.setOnClickListener {
            startFgt(FgtMyContract())
        }

        ll_support_me.setOnClickListener {
            val tel = "4000283969"
            NormalDialog(activity).apply {
                style(NormalDialog.STYLE_TWO)
                title("售后支持")
                titleTextColor(Color.parseColor("#131414"))
                content(tel)
                contentGravity(Gravity.CENTER)
                btnText("取消", "拨打")
                btnTextColor(Color.parseColor("#ABABAB"), Color.parseColor("#000000"))
                setOnBtnClickL(OnBtnClickL {
                    dismiss()
                }, OnBtnClickL {
                    SystemUtils.call(activity, tel)
                    dismiss()
                })
                show()
            }
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
    }


    fun startFgt(toFgt: SupportFragment) {
        (parentFragment as FgtMain).start(toFgt)
    }

    override fun getLayoutRes(): Int = R.layout.fgt_me

}