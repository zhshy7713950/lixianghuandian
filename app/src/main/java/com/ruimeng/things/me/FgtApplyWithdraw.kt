package com.ruimeng.things.me

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.ruimeng.things.R
import com.ruimeng.things.me.activity.AtyWeb2
import com.utils.StatusBarUtil
import kotlinx.android.synthetic.main.fgt_apply_withdraw.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.toPOJO
import wongxd.http
import wongxd.utils.utilcode.util.ScreenUtils
import wongxd.utils.utilcode.util.SizeUtils

class FgtApplyWithdraw : BaseBackFragment() {

    companion object {
        fun newInstance(balance: String): FgtApplyWithdraw {
            val fgt = FgtApplyWithdraw()
            val bundle = Bundle()
            bundle.putString("balance", balance)
            fgt.arguments = bundle
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_apply_withdraw

    override fun onSupportVisible() {
        super.onSupportVisible()
        activity?.let { StatusBarUtil.setColor(it, Color.parseColor("#ED5A2E")) }
    }

    override fun onSupportInvisible() {
        super.onSupportInvisible()
        activity?.let { StatusBarUtil.setColor(it, resources.getColor(R.color.app_color)) }
    }


    private var balanceStr = "0.00"
    private var lastAccountName = ""
    private var lastAccountNum = ""

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "申请提现")
        topbar.setTitle("申请提现").setTextColor(android.graphics.Color.WHITE)
        topbar.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        topbar.addRightImageButton(R.drawable.ic_question_white, R.id.topbar_right_button).setOnClickListener {
            AtyWeb2.start("规则说明", "http://xianglilai.scxll.cn/appH5/newClientRewardRule.html")
        }
        
        balanceStr = arguments?.getString("balance") ?: "0.00"
        tv_balance.text = balanceStr

        fetchHistory()
        initListener()
    }

    private fun initListener() {
        btn_all.setOnClickListener {
            et_amount.setText(balanceStr)
            try {
                et_amount.setSelection(et_amount.text.length)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btn_import_last.setOnClickListener {
            if (lastAccountName.isNotEmpty() && lastAccountNum.isNotEmpty()) {
                et_name.setText(lastAccountName)
                et_account.setText(lastAccountNum)
            }
        }

        btn_confirm.setOnClickListener {
            val amountStr = et_amount.text.toString().trim()
            val name = et_name.text.toString().trim()
            val account = et_account.text.toString().trim()

            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val balance = balanceStr.toDoubleOrNull() ?: 0.0

            if (amount <= 0.0) {
                EasyToast.DEFAULT.show("请输入有效的提现金额")
                return@setOnClickListener
            }
            if (amount > balance) {
                EasyToast.DEFAULT.show("提现金额不能超出可提现金额")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(account)) {
                EasyToast.DEFAULT.show("请输入支付宝姓名及支付宝账号")
                return@setOnClickListener
            }

            showConfirmDialog(name, account, String.format("%.2f", amount))
        }

        btn_history.setOnClickListener {
            start(FgtWithdrawList.newInstance())
        }
    }

    private fun showConfirmDialog(name: String, account: String, amount: String) {
        val act = activity ?: return
        val dialog = Dialog(act, R.style.TransparentDialog)
        dialog.setContentView(R.layout.dialog_confirm_withdraw)
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setDimAmount(0.3f)

        val params = dialog.window?.attributes
        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(80f)
        dialog.window?.attributes = params

        dialog.findViewById<TextView>(R.id.tv_confirm_name).text = name
        dialog.findViewById<TextView>(R.id.tv_confirm_account).text = account
        dialog.findViewById<TextView>(R.id.tv_confirm_amount).text = "${amount}元"

        dialog.findViewById<View>(R.id.btn_reedit).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btn_submit).setOnClickListener {
            dialog.dismiss()
            submitWithdraw(name, account, amount)
        }

        dialog.show()
    }

    private fun submitWithdraw(name: String, account: String, amount: String) {
        http {
            url = "/apiv6/distribute/withdraw"
            params["cashType"] = "balance"
            params["pay_type"] = "alipay"
            params["accuntName"] = name
            params["accuntNum"] = account
            params["amount"] = amount
            
            onSuccess {
                EasyToast.DEFAULT.show("提交成功，请稍后查看提现结果")
                view?.postDelayed({ pop() }, 1500)
            }
        }
    }

    private fun fetchHistory() {
        http {
            url = "/apiv6/distribute/withdrawlist"
            params["page"] = "1"
            params["pageSize"] = "10000"

            onSuccess { res ->
                try {
                    val bean = res.toPOJO<WithdrawListResponse>()
                    if (bean.errcode == 200 && bean.data != null) {
                        val list = bean.data.data
                        if (!list.isNullOrEmpty()) {
                            val first = list[0]
                            val info = first.payInfo
                            if (info != null && !TextUtils.isEmpty(info.accountName) && !TextUtils.isEmpty(info.accountNum)) {
                                lastAccountName = info.accountName ?: ""
                                lastAccountNum = info.accountNum ?: ""
                                btn_import_last.text = "点击导入上次提现账号：$lastAccountName ($lastAccountNum)"
                                btn_import_last.visibility = View.VISIBLE
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    data class WithdrawListResponse(val errcode: Int, val errmsg: String, val data: WithdrawListPage?)
    data class WithdrawListPage(val page: Int, val pageSize: Int, val totalCount: Int, val data: List<WithdrawItem>?)
    data class WithdrawItem(val payInfo: PayInfo?)
    data class PayInfo(val accountName: String?, val accountNum: String?)
}
