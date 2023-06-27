package com.ruimeng.things.home

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.flyco.roundview.RoundTextView
import com.ruimeng.things.Path
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_package_bind.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.recycleview.yaksa.linear
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2019/7/17.
 *
 * 套餐绑定
 *
 */
class FgtPackageBind : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_package_bind


    companion object {

        val PACKAGE_BIND_SCAN_PREFIX = "package_bind_scan_prefix"

        fun newInstance(contractId: String): FgtPackageBind {
            return FgtPackageBind().apply {
                arguments = Bundle().apply {
                    putString("contractId", contractId)
                }
            }


        }


    }

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        EventBus.getDefault().register(this)

        topbar = rootView.findViewById(R.id.topbar)
        initTopbar(topbar, "套餐绑定")
        topbar.addRightTextButton("添加", R.id.right)?.apply {
            setTextColor(Color.WHITE)
            setOnClickListener {
                FgtHomeBack.IsWholeBikeRent = false
                FgtHomeBack.tryToScan(PACKAGE_BIND_SCAN_PREFIX)
            }
        }


        srl_package_bind.setOnRefreshListener { getList() }

        srl_package_bind?.isRefreshing = true
        getList()

    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    data class FgtPackageBindScanResultEvent(val device_id: String)

    @Subscribe
    fun dealScanResult(event: FgtPackageBindScanResultEvent) {
        http {
            url = Path.COMPOSE_BIND
            params["contract_id"] = contractId
            params["device_id"] = event.device_id

            onSuccessWithMsg { res, msg ->
                srl_package_bind?.isRefreshing = true
                getList()
            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }

    data class PackageBindListBean(var errcode: Int = 0, var errmsg: String = "", var data: List<Data> = listOf()) {
        data class Data(var contract_id: String = "", var device_id: String = "", var model_str: String = "")
    }

    private fun getList() {
        http {
            url = Path.CONTRACT_COMPOSE
            params["contract_id"] = contractId

            onFinish {
                srl_package_bind?.isRefreshing = false
            }

            onSuccessWithMsg { res, msg ->

                val list = res.toPOJO<PackageBindListBean>().data
                rv_package_bind?.linear {
                    list.forEach { item ->
                        itemDsl {
                            xml(R.layout.item_rv_package_bind)
                            renderX { position, view ->
                                val tvId = view.findViewById<TextView>(R.id.tv_id)
                                val tvModel = view.findViewById<TextView>(R.id.tv_model)
                                val tvUnbind = view.findViewById<RoundTextView>(R.id.tv_unbind)
                                tvId.text = "设备ID:${item.device_id}"
                                tvModel.text = "设备型号:${item.model_str}"
                                tvUnbind.setOnClickListener {
                                    http {
                                        url = Path.UNBIND_BATTERY
                                        params["contract_id"] = item.contract_id
                                        params["device_id"] = item.device_id

                                        onSuccessWithMsg { res, msg ->
                                            EasyToast.DEFAULT.show(msg)
                                            getList()
                                        }

                                        onFail { code, msg ->
                                            EasyToast.DEFAULT.show(msg)
                                        }
                                    }
                                }
                                view.setOnClickListener {
                                    EventBus.getDefault().post(BatteryInfoChangeEvent(item.device_id))
                                    pop()
                                }
                            }
                        }
                    }
                }
            }

            onFail { code, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }
}