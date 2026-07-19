package com.ruimeng.things.net_station.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.net_station.bean.NetStationBean
import com.utils.MODEL_48
import com.utils.MODEL_60
import com.utils.MODEL_72
import com.utils.MapUtils
import kotlinx.android.synthetic.main.view_net_station_item.view.ll_container
import kotlinx.android.synthetic.main.view_net_station_item.view.ll_model_container_1
import kotlinx.android.synthetic.main.view_net_station_item.view.ll_model_container_2
import kotlinx.android.synthetic.main.view_net_station_item.view.ll_model_container_3
import kotlinx.android.synthetic.main.view_net_station_item.view.ll_net_station_right
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_address
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_business_hours
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_cabinet_num
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_full_exchange
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_model_num_1
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_model_num_2
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_model_num_3
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_model_title_1
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_model_title_2
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_model_title_3
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_navigation
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_net_station_name
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_offline
import kotlinx.android.synthetic.main.view_net_station_item.view.tv_telephone
import org.jetbrains.anko.switch
import org.jetbrains.anko.textColor
import wongxd.utils.ConvertUtils.sp2px

class NetStationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
), INetStationView {

    private val batteryVQueue = ArrayDeque<String>().apply {
        add(MODEL_72)
        add(MODEL_60)
        add(MODEL_48)
    }

    private lateinit var ctl: INetStationController

    init {
        LayoutInflater.from(context).inflate(R.layout.view_net_station_item, this, true)
    }

    override fun setNewData(data: NetStationBean.Data.X) {
        //电池可换数
        if (data.isOnline == 0) {
            ll_net_station_right.visibility = GONE
        } else {
            ll_net_station_right.visibility = VISIBLE
            val batteryV = FgtHome.getBatteryV()
            if (!batteryV.isNullOrEmpty()) {
                batteryVQueue.remove(batteryV)
                batteryVQueue.addFirst(batteryV)
            }
            batteryVQueue.forEachIndexed { index, s ->
                var container: LinearLayout?
                var title: TextView?
                var num: TextView?
                when (index) {
                    0 -> {
                        container = ll_model_container_1
                        title = tv_model_title_1
                        num = tv_model_num_1
                    }
                    1 -> {
                        container = ll_model_container_2
                        title = tv_model_title_2
                        num = tv_model_num_2
                    }
                    else -> {
                        container = ll_model_container_3
                        title = tv_model_title_3
                        num = tv_model_num_3
                    }
                }
                var modelNum = 0
                when (s) {
                    MODEL_72 -> {
                        container.setBackgroundResource(R.drawable.bg_model_72)
                        modelNum = data.available_arr.model_72
                        // 特例：泸州市且用户仅支持 72V 下 50Ah 时，以 batTypeCount[72][50] 为准
                        if (data.cityId == "510500" && FgtHome.hasOnlyAh50For72) {
                            val override = data.batTypeCount[MODEL_72]?.get("50")
                            if (override != null) modelNum = override
                        }
                    }
                    MODEL_60 -> {
                        container.setBackgroundResource(R.drawable.bg_model_60)
                        modelNum = data.available_arr.model_60
                    }
                    MODEL_48 -> {
                        container.setBackgroundResource(R.drawable.bg_model_48)
                        modelNum = data.available_arr.model_48
                    }
                }
                if (batteryV.isNullOrEmpty() || s == batteryV) {
                    container.alpha = 1f
                    title.text = "可换数"
                    title.setTextColor(Color.WHITE)
                    if(modelNum > 0){
                        num.setTextColor(Color.parseColor("#29EBB6"))//绿
                    }else{
                        num.setTextColor(Color.parseColor("#FEB41E"))//黄
                    }
                } else {
                    container.alpha = 0.5f
                    title.text = "不匹配"
                    title.setTextColor(Color.parseColor("#B2C1CE"))
                    num.setTextColor(Color.parseColor("#B2C1CE"))
                }
                num.text = modelNum.toString()
            }
        }
        //站点名称
        tv_net_station_name.text = data.site_name
        //几仓柜
        tv_cabinet_num.text = "${data.cellNum}仓柜"
        //满仓换电
        tv_full_exchange.isVisible = data.fullExchange == 2
        //已离线
        tv_offline.isVisible = data.isOnline == 0
        //营业时间
        tv_business_hours.text = if(data.workTime.isNullOrEmpty()) "00:00-23:59" else data.workTime
        //距离 | 地址
        val distance = MapUtils.calculateDistance(data.lat, data.lng)
        val address = data.address
        val showDAStr = "$distance | $address"
        val span = SpannableString(showDAStr)
        span.setSpan(StyleSpan(Typeface.BOLD),0,distance.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        span.setSpan(AbsoluteSizeSpan(sp2px(12f)),0,distance.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        tv_address.text = span
        tv_telephone.setOnClickListener {
            ctl?.onTelClick(data,context)
        }
        tv_navigation.setOnClickListener {
            ctl?.onNavigationClick(data,context)
        }
        ll_container.setOnClickListener {
            this.ctl?.onItemClick(data,context)
        }
    }

    override fun bindCtl(ctl: INetStationController) {
        this.ctl = ctl
    }

    /**
     * 地图模式（网点页气泡卡片）：
     * 1. 顶部去掉圆角，配合上方 Tab 栏，避免圆角处出现缺口。
     * 2. 地址固定为两行，避免切换电柜（地址有单行/多行差异）时卡片高度忽大忽小。
     *
     * 列表页不调用此方法，保持原有圆角与自适应高度。
     */
    fun setMapMode(enabled: Boolean) {
        if (enabled) {
            ll_container.setBackgroundResource(R.drawable.bg_net_station_item_flat_top)
            tv_address.setLines(2)
        } else {
            ll_container.setBackgroundResource(R.drawable.bg_net_station_item)
            tv_address.minLines = 0
            tv_address.maxLines = 2
        }
    }

}