package com.ruimeng.things.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.bean.MyDevicesBean
import com.utils.TextUtil
import kotlinx.android.synthetic.main.fgt_switch_battery.*
import org.greenrobot.eventbus.EventBus
import wongxd.base.BaseBackFragment
import wongxd.base.custom.caneffect.CanRippleLayout
import wongxd.base.custom.caneffect.CanShadowDrawable
import wongxd.common.dp2px
import wongxd.common.recycleview.yaksa.linear
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2018/11/12.
 */
class FgtSwitchBattery : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_switch_battery


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "切换电池")

        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s ?: return
                val q = s.toString()
                doSearch(q)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        CanShadowDrawable.Builder.on(qfl_search)

            .radius(5.dp2px)
//            .shadowColor(Color.parseColor("#333333"))
            .bgColor(resources.getColor(R.color.app_color))
//            .shadowRange(5.dp2px)
            .offsetTop(5.dp2px)
            .offsetBottom(5.dp2px)
            .offsetLeft(5.dp2px)
            .offsetRight(5.dp2px)
            .create()

        CanRippleLayout.Builder.on(qfl_search).rippleCorner(5.dp2px).create()

        qfl_search.setOnClickListener {
            val q = et_search.text.toString()
            doSearch(q)
        }

        getMyBattery()
    }

    private fun doSearch(q: String) {
        if (q.isNotBlank()) {
            val temp =
                allList.filter { it.device_id.contains(q) || it.device_id == q.trim() }
            renderRv(temp)
        } else {
            renderRv(allList)
        }
    }


    private val allList: MutableList<MyDevicesBean.Data> = mutableListOf()

    /**
     * 获取我的电池列表
     */
    private fun getMyBattery() {
        http {
            url = Path.GET_MY_DEVICE
            onSuccess {
                val result = it.toPOJO<MyDevicesBean>().data
                allList.clear()
                allList.addAll(result)
                renderRv(allList)
            }
        }
    }

    private fun renderRv(result: List<MyDevicesBean.Data>) {

        rv_switch_battery?.linear {

            if (result.isEmpty()) {
                itemDsl {
                    xml(R.layout.layout_empty)
                }
            } else
                result.forEach { item ->
                    itemDsl {
                        xml(R.layout.item_rv_switch_battery)

                        renderX { position, view ->


                            val tvNum = view.findViewById<TextView>(R.id.tv_battery_num)

                            val tvStatus = view.findViewById<TextView>(R.id.tv_battery_status)

                            val tvHole = view.findViewById<TextView>(R.id.tv_battery_hole)
                            val tvRemark = view.findViewById<TextView>(R.id.tv_battery_remark)
                            var textColors = arrayOf("#FFFFFF","#929FAB")

                            tvNum.text = "电池编号：" + item.device_id
                            if (item.device_id.startsWith("8") && item.device_id.length == 8 ){
                                tvStatus.text = TextUtil.getSpannableString(arrayOf("电池状态：","-"),textColors)
                                tvHole.text = TextUtil.getSpannableString(arrayOf("电池电量：","-"),textColors)
                                tvRemark.text = TextUtil.getSpannableString(arrayOf("电池备注：","虚拟编号-待取电"),textColors)
                            }else{
                                var status = ""
                                if (item.protect != "0" && item.protect != "4096"){
                                    status = "故障"
                                }else{
                                    status = if (item.device_status == 1)  "通电" else "断电"
                                }
                                tvStatus.text = TextUtil.getSpannableString(arrayOf("电池状态：",status),textColors)
                                tvHole.text = TextUtil.getSpannableString(arrayOf("电池电量：",item.rsoc+"%"),textColors)
                                tvRemark.text = TextUtil.getSpannableString(arrayOf("电池备注：",item.remark),textColors)
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
}