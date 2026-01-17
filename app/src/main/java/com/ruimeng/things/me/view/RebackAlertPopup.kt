package com.ruimeng.things.me.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.MyCouponBean
import com.ruimeng.things.me.contract.FgtContractSignStep1
import com.ruimeng.things.me.contract.FgtContractSignStep2
import kotlinx.android.synthetic.main.fgt_contract_sign_step_1.*
import org.json.JSONObject
import wongxd.common.MainLooper

class RebackAlertPopup (private val activity: Activity,
                        private val hasRecomActivity: Boolean = false,
                        private val listener: OnClickListener
) : PopupWindow(activity){
    init {
        contentView = View.inflate(activity, R.layout.popup_reback_alert, null)
        var tv_switch_battery = contentView.findViewById<TextView>(R.id.tv_switch_battery)
        var tv_cancel = contentView.findViewById<TextView>(R.id.tv_cancel)
        var tv_alert_info = contentView.findViewById<TextView>(R.id.tv_alert_info)

        if (hasRecomActivity) {
            tv_alert_info.text = "现在正在进行退网操作！接下来需要将电池扫码放入柜内，成功放回电池后合约将失效，剩余天数将清零，且无法退款！押金将原路退回！提现功能将暂停(提现账号保留3个月)！请确认操作！"
        }

        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(0x55000000))

        tv_cancel?.setOnClickListener {
            dismiss()
        }
        object : CountDownTimer((10 * 1000).toLong(), 1000.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                MainLooper.runOnUiThread {
                    tv_switch_battery.text = "确认（${millisUntilFinished / 1000}s）"
                    tv_switch_battery?.setOnClickListener {}
                }
            }
            override fun onFinish() {
                tv_switch_battery.text = "确认"
                tv_switch_battery?.setOnClickListener {
                    listener.onClick(tv_switch_battery)
                    dismiss()
                }
            }
        }.start()

    }

    fun show(view: View) {
        if (activity.window.decorView.windowToken != null) {
            showAtLocation(view, Gravity.CENTER, 0, 0)
        }
    }
}