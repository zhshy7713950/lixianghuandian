package com.utils

import android.app.Activity
import android.graphics.Color
import android.util.Log
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.ruimeng.things.PathV3
import com.ruimeng.things.net_station.net_city_data.AreaBean
import com.ruimeng.things.net_station.net_city_data.NetCityJsonBean
import org.json.JSONObject
import wongxd.Config
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2020/1/4.
 */
object AreaDataWorker {


    private var cityDataJson: String? = null

    private fun getCityData() {
        cityDataJson = Config.getDefault().stringCacheUtils.getAsString("downloadCityData")
        if (cityDataJson.isNullOrBlank()) {
            downloadCityData()
        } else {
            initJsonData()
        }
    }


    private fun downloadCityData() {

        http {
            url = PathV3.GET_AREA_LIST
            onSuccess { res ->
                cityDataJson = res
                Config.getDefault().stringCacheUtils.put("downloadCityData", cityDataJson)
                initJsonData()
            }
        }
    }

    private val provinceItems: MutableList<NetCityJsonBean.Data> = mutableListOf()

    private val cityItems: MutableList<MutableList<NetCityJsonBean.Data.Child>> = mutableListOf()

    private val areaItems: MutableList<MutableList<MutableList<AreaBean>>> = mutableListOf()

    /**
     * 解析数据
     */
    fun initJsonData() {
        if (cityDataJson.isNullOrBlank()) {
            getCityData()
            return
        }

        val jsonBean = cityDataJson?.toPOJO<NetCityJsonBean>()?.data ?: return//用Gson 转成实体

        /**
         *
         * 添加省份数据
         *
         *
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         *
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         *
         */

        provinceItems.clear()
        provinceItems.addAll(jsonBean)

        cityItems.clear()
        areaItems.clear()
        provinceItems.forEach { p ->
            val cityList = p.child.toMutableList()
            if (cityList.isEmpty()) {
                cityList.add(NetCityJsonBean.Data.Child("", "", ""))
            }
            cityItems.add(cityList)
            val areaList = mutableListOf<MutableList<AreaBean>>()
            cityList.forEach { _ ->
                areaList.add(mutableListOf())
            }
            areaItems.add(areaList)
        }
    }

    private var pvOptions: OptionsPickerView<Any>? = null
            /**
     * 条件选择器初始化
     */
    fun showOptionPicker(
        activity: Activity?,
        title: String = "",
        callback: (String,String,String) -> Unit
    ) {

        if (provinceItems.isEmpty() || cityItems.isEmpty()) {
            initJsonData()
            return
        }

        /**
         *
         * 注意 ：如果是三级联动的数据(省市区等)，请参照 JsonDataActivity 类里面的写法。
         *
         */

        pvOptions = OptionsPickerBuilder(activity,
            OnOptionsSelectListener { options1, options2, options3, v -> //返回的分别是三个级别的选中位置
                //做安全控制，防止数组越界
                val p = if(options1 < 0 || options1 >= provinceItems.size) "" else provinceItems[options1].name
                val c = if(options2 < 0 || options2 >= cityItems[options1].size) "" else cityItems[options1][options2].name
                val a = if(options3 < 0 || options3 >= areaItems[options1][options2].size) "" else areaItems[options1][options2][options3].value

                callback.invoke(p,c,a)
            })

            .setTitleText(title)

            .setContentTextSize(20)//设置滚轮文字大小

            .setDividerColor(Color.parseColor("#586671"))//设置分割线的颜色

            .setSelectOptions(0, 0, 0)//默认选中项

            .setBgColor(Color.parseColor("#404E59"))

            .setTitleBgColor(Color.parseColor("#404E59"))
            .setCancelColor(Color.WHITE)
            .setTitleSize(15)
            .setTextColorCenter(Color.WHITE)
            .setSubmitColor(Color.parseColor("#29EBB6"))
            .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
//            .setLabels("省", "市", "区")
            .setOptionsSelectChangeListener { options1, options2, options3 ->
                //                val str = "options1: $options1\noptions2: $options2\noptions3: $options3"
                Log.d("CityDataWorker", "onOptionsSelectChanged: $options1, $options2, $options3")
                if (options1 >= 0 && options2 >= 0 && areaItems[options1][options2].isEmpty()) {
                    updateAreaData(options1, options2, options3)
                }
            }
            .setBackgroundId(0x50000000) //设置外部遮罩颜色
            .build()

        pvOptions!!.show()
        pvOptions!!.setSelectOptions(0, 0, 0)
        pvOptions!!.setPicker(provinceItems.toList(), cityItems.toList(), areaItems.toList())//二级选择器
        updateAreaData(0, 0, 0)
    }

    private fun updateAreaData(options1: Int, options2: Int, options3: Int){
        val cityId = cityItems[options1]?.get(options2)?.id
        if (cityId.isNotEmpty()) {
            getAreaData(cityId) { areaList ->
                if (areaList.isNotEmpty()) {
                    areaItems[options1][options2] = areaList.toMutableList()
                    pvOptions!!.setPicker(provinceItems.toList(), cityItems.toList(), areaItems.toList())
                    pvOptions!!.setSelectOptions(options1, options2, options3)
                }
            }
        }
    }

    private fun getAreaData(cityId: String, callback: (List<AreaBean>) -> Unit) {
        http {
            url = "/apiv6/address/getareainfo"
            params["pid"] = cityId
            onSuccess { res ->
                val data = JSONObject(res).getJSONObject("data")
                val areaList = mutableListOf<AreaBean>()
                data.keys().forEach {
                    val area = AreaBean(it, data.getString(it))
                    areaList.add(area)
                }
                callback(areaList)
            }
        }
    }
}