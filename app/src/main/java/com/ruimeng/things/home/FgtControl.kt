package com.ruimeng.things.home

import android.os.Bundle
import com.ruimeng.things.Path
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_control.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.dissmissProgressDialog
import wongxd.common.showProgressDialog
import wongxd.http

/**
 * Created by wongxd on 2018/11/12.
 */
class FgtControl : BaseBackFragment() {


    override fun getLayoutRes(): Int = R.layout.fgt_control


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "控制")

        switcher_fgt_control.isChecked = FgtHomeBack.IS_OPEN

        switcher_fgt_control.setOnCheckedChangeListener { buttonView, isChecked ->
            changeBatteryStatus(isChecked)
        }


    }

    fun changeBatteryStatus(isOpen: Boolean) {
        showProgressDialog("操作电池中")
        http {
            url = Path.OPT_DEVICE
            params["device_id"] = FgtHomeBack.CURRENT_DEVICEID
            params["device_status"] = if (isOpen) "1" else "2"

            onSuccess {
                //                device_status int  1当前为开 2当前为关
                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val device_status = data.optInt("device_status")

                if (device_status == 1) {
                    EventBus.getDefault().post(BatteryOpenEvent(true))
                } else {
                    EventBus.getDefault().post(BatteryOpenEvent(false))
                }

            }

            onFinish {
                dissmissProgressDialog()
            }
        }

    }

}