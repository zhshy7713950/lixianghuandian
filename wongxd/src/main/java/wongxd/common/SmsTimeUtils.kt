package wongxd.common

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.widget.TextView
import java.lang.ref.WeakReference
import java.util.*

/**
 * 发送验证码倒计时工具类
 *
 */
object SmsTimeUtils {


    private var CURR_COUNT = 60

    private var countdownTimer: Timer? = null
    private var tvSendCode: WeakReference<TextView>? = null


    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                if (countdownTimer != null) {
                    countdownTimer!!.cancel()
                    countdownTimer = null
                }
                tvSendCode?.get()?.text = "获取验证码"
                tvSendCode?.get()?.isEnabled = true
            } else {
                tvSendCode?.get()?.setText(msg.what.toString() + "s")
                tvSendCode?.get()?.isEnabled = false
            }
            super.handleMessage(msg)
        }
    }


    /**
     *
     * @param textView 控制倒计时的view
     *
     * @param long  时长 秒
     */
    fun startCountdown(textView: WeakReference<TextView>, long: Int = 60) {
        tvSendCode = textView
        CURR_COUNT = long
        if (countdownTimer == null) {
            countdownTimer = Timer()
            countdownTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    val msg = handler.obtainMessage()
                    msg.what = CURR_COUNT--
                    handler.sendMessage(msg)
                }
            }, 0, 1000)
        }
    }
}
