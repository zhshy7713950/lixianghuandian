package com.ruimeng.things.home

import android.os.Bundle
import android.view.View
import com.ruimeng.things.R
import com.utils.AreaDataWorker
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.fgt_shipping_address.*
import wongxd.base.BaseBackFragment
import wongxd.http

class FgtShippingAddress : BaseBackFragment() {

    companion object {
        fun newInstance(receiptId: String): FgtShippingAddress {
            val fragment = FgtShippingAddress()
            val args = Bundle()
            args.putString("receiptId", receiptId)
            fragment.arguments = args
            return fragment
        }
    }

    private val receiptId: String by lazy { arguments?.getString("receiptId") ?: "" }
    private var selectedProvince = ""
    private var selectedCity = ""
    private var selectedArea = ""

    override fun getLayoutRes(): Int = R.layout.fgt_shipping_address

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "好礼免费领")
        AreaDataWorker.initJsonData()

        tvArea.setOnClickListener {
            // 打开省市区选择器
            AreaDataWorker.showOptionPicker(
                activity, 
                ""
            ) { province, city, area ->
                selectedProvince = province
                selectedCity = city
                selectedArea = area
                
                // 更新UI显示
                val areaText = when {
                    area.isNotEmpty() -> "$province $city $area"
                    city.isNotEmpty() -> "$province $city"
                    else -> province
                }
                tvArea.text = areaText
            }
        }

        btnSubmit.setOnClickListener {
            if (etName.text.isEmpty() || etPhone.text.isEmpty() || etAddress.text.isEmpty() || selectedProvince.isEmpty()) {
                ToastHelper.shortToast(context, "请先完善收货地址")
            } else {
                submitAddress()
            }
        }
    }

    private fun submitAddress() {
        http {
            url = "/apiv6/address/updategiftaddr"
            params["id"] = receiptId
            params["consignee"] = etName.text.toString()
            params["tel"] = etPhone.text.toString()
            params["province"] = selectedProvince
            params["city"] = selectedCity
            params["area"] = selectedArea
            params["address"] = etAddress.text.toString()

            onSuccess {
                ToastHelper.shortToast(context, "提交成功，我们将尽快为您安排发货")
                popTo(FgtHome::class.java, false)
            }
        }
    }
} 