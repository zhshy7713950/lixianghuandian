package com.ruimeng.things.net_station.net_city_data

import android.app.Activity
import android.graphics.Color
import android.view.View
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.ruimeng.things.PathV3
import wongxd.Config
import wongxd.common.toPOJO
import wongxd.http

/**
 * Created by wongxd on 2020/1/4.
 */
object CityDataWorker {


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


    val provinceItems: MutableList<NetCityJsonBean.Data> = mutableListOf()

    val cityItems: MutableList<MutableList<NetCityJsonBean.Data.Child>> = mutableListOf()

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
        provinceItems.forEach { p ->
            cityItems.add(p.child.toMutableList())
        }


    }


    /**
     * 条件选择器初始化
     */
    fun showOptionPicker(
        activity: Activity?,
        title: String = "",
        callback: (NetCityJsonBean.Data, NetCityJsonBean.Data.Child) -> Unit
    ) {

        if (CityDataWorker.provinceItems.isEmpty() || CityDataWorker.cityItems.isEmpty()) {
            initJsonData()
            return
        }

        /**
         *
         * 注意 ：如果是三级联动的数据(省市区等)，请参照 JsonDataActivity 类里面的写法。
         *
         */

        val pvOptions = OptionsPickerBuilder(activity, object : OnOptionsSelectListener {

            override fun onOptionsSelect(options1: Int, options2: Int, options3: Int, v: View?) {

                //返回的分别是三个级别的选中位置
//
                val p = provinceItems[options1]
                val c = cityItems[options1][options2]

                callback.invoke(p, c)
            }

        })

            .setTitleText(title)

            .setContentTextSize(20)//设置滚轮文字大小

            .setDividerColor(Color.parseColor("#586671"))//设置分割线的颜色

            .setSelectOptions(0, 1)//默认选中项

            .setBgColor(Color.parseColor("#404E59"))

            .setTitleBgColor(Color.parseColor("#404E59"))
            .setCancelColor(Color.WHITE)
            .setTitleSize(15)
            .setTextColorCenter(Color.WHITE)

            .setSubmitColor(Color.parseColor("#29EBB6"))


            .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。

            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。

//            .setLabels("省", "市", "区")

            .setBackgroundId(0x50000000) //设置外部遮罩颜色

            .setOptionsSelectChangeListener { options1, options2, options3 ->
                //                val str = "options1: $options1\noptions2: $options2\noptions3: $options3"

            }

            .build<Any>()


        pvOptions.show()
        pvOptions.setSelectOptions(0, 0)

        pvOptions.setPicker(provinceItems.toList(), cityItems.toList())//二级选择器


    }


    fun getProvinceAndCityInfoByName(
        provinceName: String,
        cityName: String
    ): Pair<NetCityJsonBean.Data?, NetCityJsonBean.Data.Child?> {
        if (provinceItems.isEmpty() || cityItems.isEmpty()) {
            initJsonData()
            return null to null
        }

        val p =
            provinceItems.filter {
                it.name.contains(provinceName) || provinceName.contains(it.name)
            }.first()


        val c = p.child.filter {
            it.name.contains(cityName) || cityName.contains(it.name)
        }.first()

        return p to c
    }
}