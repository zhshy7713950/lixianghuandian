package com.ruimeng.things.me.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import com.qmuiteam.qmui.widget.QMUITabSegment
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.DistrCashInfoBean
import com.utils.*
import kotlinx.android.synthetic.main.activity_balance_withdrawal.*
import kotlinx.android.synthetic.main.activity_distribution_center.topbar
import wongxd.base.AtyBase
import wongxd.common.toPOJO
import wongxd.http


class BalanceWithdrawalActivity : AtyBase() {

    private val titleList = arrayOf("余额提现", "兑换优惠券")
    private var getBalanceWithdrawalType = "balance"
    private var getWithdrawalType = "alipay"
    private var getBalance = 0.0
    private var getServiceCharge = 0.0

    companion object {
        lateinit var mActivity: BalanceWithdrawalActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance_withdrawal)
        firstVisit = true
        initView()
        setListener()
        initData()
    }

    private fun initView() {
        mActivity = this
    }

    private fun setListener() {
        inputMoneyText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(editable: Editable) {
                if (editable.isNotEmpty()) {
                    try {
                        val getInputMoney = inputMoneyText?.text.toString().toDouble()
                        if (getInputMoney > -1) {
                            if (getInputMoney <= getBalance) {
                                if (getServiceCharge >= 0) {
                                    val getAllServiceCharge = getInputMoney * getServiceCharge
//                                    serviceChargeText?.text = "-$getAllServiceCharge"
//                                    finalMoneyText?.text = (getInputMoney + getAllServiceCharge).toString()
                                    setFinalMoney((getInputMoney - getAllServiceCharge).toString())
                                } else {
                                    ToastHelper.shortToast(mActivity, "手续费有误")
                                    inputMoneyText?.setText("")
                                    setFinalMoney("0")
                                }
                            } else {
                                ToastHelper.shortToast(mActivity, "您的可提现金额不足")
                                inputMoneyText?.setText("")
                                setFinalMoney("0")
                            }
                        } else {
                            ToastHelper.shortToast(mActivity, "请输入正确提现金额")
                            inputMoneyText?.setText("")
                            setFinalMoney("0")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        allTextView?.setOnClickListener {
            inputMoneyText?.setText(getBalance.toString())
            if (!TextUtils.isEmpty(inputMoneyText?.text)) {
                inputMoneyText?.setSelection(inputMoneyText?.text!!.length)
            }
        }
        withdrawalTypeLayout?.setOnClickListener {
            CommonWithdrawalTypeChoiceDialogHelper.commonWithdrawalTypeChoiceDialog(mActivity,
                getWithdrawalType,
                object : CommonDialogCallBackHelper {
                    override fun back(viewId: Int, msg: String?) {
                        getWithdrawalType = msg.toString()
                        if ("alipay" == getWithdrawalType) {
                            if ("1" == getIsAliPay) {
                                setWithdrawalTypeLayoutStatus()
                            } else {
                                ToastHelper.longToast(mActivity, "您还未绑定支付宝，请先绑定")
                                startActivity(
                                    Intent(
                                        mActivity,
                                        WithdrawalAccountActivity::class.java
                                    )
                                )
                            }
                        } else {

                            if ("1" == getIsWeChat) {
                                setWithdrawalTypeLayoutStatus()
                            } else {
                                ToastHelper.longToast(mActivity, "您还未绑定微信，请先绑定")
                                startActivity(
                                    Intent(
                                        mActivity,
                                        WithdrawalAccountActivity::class.java
                                    )
                                )
                            }
                        }
                    }
                })
        }
        confirmBtn?.setOnClickListener {
            if (TextUtils.isEmpty(inputMoneyText?.text)) {
                ToastHelper.shortToast(mActivity, inputMoneyText?.hint)
                return@setOnClickListener
            }
            LogHelper.i("data===","===getIsWeChat===${getIsWeChat}")
            LogHelper.i("data===","===getWithdrawalType===${getWithdrawalType}")
            if ("alipay" == getWithdrawalType) {
                if ("1" != getIsAliPay) {
                    ToastHelper.longToast(mActivity, "您还未绑定支付宝，请先绑定")
                    startActivity(
                        Intent(
                            mActivity,
                            WithdrawalAccountActivity::class.java
                        )
                    )
                    return@setOnClickListener
                }
            }
            if ("wxpay" == getWithdrawalType) {
                if ("1" != getIsWeChat) {
                    ToastHelper.longToast(mActivity, "您还未绑定微信，请先绑定")
                    startActivity(
                        Intent(
                            mActivity,
                            WithdrawalAccountActivity::class.java
                        )
                    )
                    return@setOnClickListener
                }
            }
            requestDistrCash(
                inputMoneyText?.text.toString(),
                getBalanceWithdrawalType,
                getWithdrawalType
            )
        }
    }

    private fun initData() {
        initTopbar(topbar, "余额提现")
        topbar.addRightTextButton("提现记录", R.id.right)?.apply {
            setTextColor(Color.WHITE)
            setOnClickListener {
                startActivity(Intent(mActivity, WithdrawalRecordActivity::class.java))
            }
        }

        initTabLayout()
        setLayoutShowStatus(0)
        setFinalMoney("0")
        requestDistrCashInfo()
    }

    private fun initTabLayout() {
        tabLayout?.reset()
        if (titleList.size > 7) {
            tabLayout?.mode = QMUITabSegment.MODE_SCROLLABLE
        } else {
            tabLayout?.mode = QMUITabSegment.MODE_FIXED
        }
        tabLayout?.setHasIndicator(true)
        tabLayout?.setIndicatorWidthAdjustContent(true)
        tabLayout?.setDefaultNormalColor(ColorHelper.getColor(mActivity, R.color.gray_6))
        tabLayout?.setDefaultSelectedColor(ColorHelper.getColor(mActivity, R.color.black_3))
        tabLayout?.addOnTabSelectedListener(object : QMUITabSegment.OnTabSelectedListener {
            override fun onTabSelected(index: Int) {
                setLayoutShowStatus(index)
            }

            override fun onTabUnselected(index: Int) {}
            override fun onTabReselected(index: Int) {}
            override fun onDoubleTap(index: Int) {}
        })
        for (i in titleList.indices) {
            tabLayout.addTab(QMUITabSegment.Tab(titleList[i]))
        }
        tabLayout.selectTab(0)
        tabLayout.notifyDataChanged()
    }

    private fun setLayoutShowStatus(position: Int) {
        when (position) {
            0 -> {
                getBalanceWithdrawalType = "balance"
                withdrawalTitleText?.text = "提现金额"
                withdrawalTypeLayout?.visibility = View.VISIBLE
                withdrawalAccountLayout?.visibility = View.VISIBLE
                finalCouponLayout?.visibility = View.GONE
                confirmBtn?.text = "立即提现"
            }
            1 -> {
                getBalanceWithdrawalType = "coupon"
                withdrawalTitleText?.text = "兑换金额"
                withdrawalTypeLayout?.visibility = View.GONE
                withdrawalAccountLayout?.visibility = View.GONE
                finalCouponLayout?.visibility = View.VISIBLE
                confirmBtn?.text = "立即兑换"
            }
            else -> {
                getBalanceWithdrawalType = ""
                withdrawalTitleText?.text = ""
                withdrawalTypeLayout?.visibility = View.GONE
                withdrawalAccountLayout?.visibility = View.GONE
                finalCouponLayout?.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setFinalMoney(money: String) {
        finalCouponText?.text = "￥${money}"
    }

    private fun setWithdrawalTypeLayoutStatus() {
        when (getWithdrawalType) {
            "alipay" -> {
                GlideHelper.loadImage(mActivity, withdrawalTypeImage, R.mipmap.ali_pay_image)
                withdrawalTypeText?.text = "支付宝"
                withdrawalAccountText?.text = getAliPayAccountName
            }
            "wxpay" -> {
                GlideHelper.loadImage(mActivity, withdrawalTypeImage, R.mipmap.we_chat_image)
                withdrawalTypeText?.text = "微信"
                withdrawalAccountText?.text = getWeChatName
            }
            else -> {
                GlideHelper.loadImage(mActivity, withdrawalTypeImage, "")
                withdrawalTypeText?.text = "请选择提现方式"
                withdrawalAccountText?.text = ""
            }
        }
    }



    private var firstVisit = true
    override fun onResume() {
        super.onResume()
        if (firstVisit) {
            firstVisit = false
        }
        if (!firstVisit) {
            requestDistrCashInfo()
        }
    }

    private var getIsAliPay = ""
    private var getIsWeChat = ""
    private var getAliPayAccountName = ""
    private var getWeChatName = ""
    @SuppressLint("SetTextI18n")
    private fun requestDistrCashInfo() {
        http {
            url = "apiv5/distrcashinfo"
            onSuccessWithMsg { res, _ ->
                val data = res.toPOJO<DistrCashInfoBean>().data
                getIsAliPay = data.is_alipay
                getIsWeChat = data.is_wx
                if (!TextUtils.isEmpty(data.distr_balance)) {
                    getBalance = data.distr_balance.toDouble()
                }
                balanceTextView?.text = "￥${getBalance}"
                getAliPayAccountName = data.alipay_acct
                getWeChatName = data.wx_nickname
                setWithdrawalTypeLayoutStatus()
            }

            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }
        }
    }

    private fun requestDistrCash(balance: String, cashType: String, payType: String) {
        http {
            url = "apiv5/distrcash"
            params["balance"] = balance
            params["cash_type"] = cashType
            params["pay_type"] = payType
            onSuccessWithMsg { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
                finish()
            }

            onFail { _, msg ->
                ToastHelper.shortToast(mActivity, msg)
            }
        }
    }

}