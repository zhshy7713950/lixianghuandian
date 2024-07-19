package com.ruimeng.things.me

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.google.gson.Gson
import com.ruimeng.things.App
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.JsonBean
import com.ruimeng.things.shop.PostGlideEngine
import com.ruimeng.things.zipImg
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.internal.entity.CaptureStrategy
import kotlinx.android.synthetic.main.fgt_apply_agent.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.NotificationHelper
import wongxd.common.loadImg
import wongxd.http
import wongxd.utils.SystemUtils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader


/**
 * Created by wongxd on 2018/11/28.
 */
class FgtApplyAgent : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_apply_agent

    val REQUEST_IMAGE = 1002

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "申请代理商")

        doAsync {
            initJsonData()

            uiThread { _ ->

                ll_check_province_apply_agent.setOnClickListener { view ->

                    initOptionPicker("", 0, options1Items.map { it.name })
                }


                ll_check_city_apply_agent.setOnClickListener { _ ->

                    if (provinceIndex != -1)
                        initOptionPicker("城市选择", 1, options1Items[provinceIndex].cityList.map { it.name })
                    else
                        EasyToast.DEFAULT.show("请先选择省份")
                }

                ll_check_area_apply_agent.setOnClickListener { _ ->
                    if (provinceIndex != -1 && cityIndex != -1)
                        initOptionPicker("区/县选择", 2, options1Items[provinceIndex].cityList[cityIndex].area)
                    else
                        EasyToast.DEFAULT.show("请先选择城市")
                }


            }
        }


        fl_upload_business_license_apply_agent.setOnClickListener { getPic() }

        btn_submit_apply_agent.setOnClickListener {

            if (provinceIndex == -1 || cityIndex == -1 || areaIndex == -1) {
                EasyToast.DEFAULT.show("请先选择 省  市  区")
                return@setOnClickListener
            }

            val agentName = et_agent_name_apply_agent.text.toString()
            val name = et_name_apply_agent.text.toString()
            val phone = et_phone_apply_agent.text.toString()
            val idcard = et_idcard_apply_agent.text.toString()
            val detailAdress = et_detail_adress_apply_agent.text.toString()

            if (SystemUtils.isHadEmptyText(agentName, name, phone, idcard, detailAdress)) {
                EasyToast.DEFAULT.show("请完善本页数据")
                return@setOnClickListener
            }

            if (imgUrl.isBlank()) {
                EasyToast.DEFAULT.show("请上传营业执照")
                return@setOnClickListener
            }


            http {

                url = Path.APPLY_AGENT

                params["province"] = tv_check_province_apply_agent.text.toString()
                params["city"] = tv_check_city_apply_agent.text.toString()
                params["area"] = tv_check_area_apply_agent.text.toString()

                params["enterprise_name"] = agentName
                params["name"] = name
                params["mobile"] = phone
                params["idcard"] = idcard
                params["address"] = detailAdress

                params["img"] = imgUrl

                onSuccessWithMsg { res, msg ->

                    EasyToast.DEFAULT.show(msg)
                    pop()
                }
            }

        }
    }


    private fun getPic() {
        Matisse.from(this)
            .choose(MimeType.ofAll())
            .capture(true)
            .captureStrategy(
                CaptureStrategy(
                    true,
                    activity?.packageName + ".fileprovider"
                )
            )
            .countable(true)
            .maxSelectable(1)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .thumbnailScale(0.85f)
            .imageEngine(PostGlideEngine())
            .forResult(REQUEST_IMAGE)
    }


    private var imgUrl = ""

    fun uploadImg(imgPath: String) {

        val dlg = ProgressDialog(activity)
        dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dlg.max = 100
        dlg.setTitle("上传营业执照")
        dlg.show()

        http {
            url = Path.UPLOAD_IMG

            imgs["file"] = File(imgPath)

            onUploadFile { progress, total, index ->
                //                wongxd.utils.utilcode.util.LogUtils.i(progress, total, index)
                dlg.progress = progress
            }

            onFinish {
                dlg.dismiss()
            }

            onSuccessWithMsg { res, msg ->
                //                {"errcode":200,"errmsg":"","data":{"string":"http:\/\/cdn.tk.image.xianlubang.com\/201811261643545863.png"}}
                val json = JSONObject(res)
                val data = json.optJSONObject("data")
                val imgUrl = data.optString("string")

                setImg(imgUrl)
            }
        }
    }

    private fun setImg(imgUrl: String) {
        if (imgUrl.isNotBlank()) {
            this.imgUrl = imgUrl
            iv_business_license_apply_agent.visibility = View.VISIBLE
            iv_business_license_apply_agent.loadImg(imgUrl)
        } else {
            iv_business_license_apply_agent.visibility = View.GONE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null) {//从相册选择完图片

            val arrylist = ArrayList<String>()
            Matisse.obtainResult(data).forEach { uri ->
                //                Logger.e(uri.toString())
                arrylist.add(
                    PostGlideEngine.getAbsoluteImagePath(NotificationHelper.mContext, uri).replace(
                        "/my_images/",
                        "/storage/emulated/0/"
                    )
                )
            }

            if (arrylist.isNotEmpty()) {
                zipImg(App.getMainAty(),arrylist[0]) {
                    uploadImg(it)
                }
            }
        }
    }

    private var provinceIndex = -1
    private var cityIndex = -1
    private var areaIndex = -1


    private val options1Items: MutableList<JsonBean> = mutableListOf()

//    private val options2Items: MutableList<MutableList<String>> = mutableListOf()
//
//    private val options3Items: MutableList<MutableList<MutableList<String>>> = mutableListOf()


    private fun getJson(context: Context, fileName: String): String {


        val stringBuilder = StringBuilder()

        try {

            val assetManager = context.getAssets()

            val bf = BufferedReader(
                InputStreamReader(

                    assetManager.open(fileName)
                )
            )

            var line: String = ""

            while ((bf.readLine())?.also { line = it } != null) {

                stringBuilder.append(line)

            }

        } catch (e: IOException) {

            e.printStackTrace()

        }

        return stringBuilder.toString()

    }

    private fun parseData(result: String): MutableList<JsonBean> {//Gson 解析

        val detail = mutableListOf<JsonBean>()

        try {

            val data = JSONArray(result)

            val gson = Gson()

            for (i in 0 until data.length()) {

                val entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean::class.java)

                detail.add(entity)

            }

        } catch (e: Exception) {

            e.printStackTrace()

        }

        return detail

    }

    private fun initJsonData() {//解析数据


        /**
         *
         * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
         *
         * 关键逻辑在于循环体
         *
         *
         *
         */

        val JsonData = getJson(activity!!, "city.json")//获取assets目录下的json文件数据


        val jsonBean = parseData(JsonData)//用Gson 转成实体


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

        options1Items.clear()
        options1Items.addAll(jsonBean)


//        for (i in 0 until jsonBean.size) {//遍历省份
//
//            val CityList = mutableListOf<String>()//该省的城市列表（第二级）
//
//            val Province_AreaList = mutableListOf<MutableList<String>>()//该省的所有地区列表（第三极）
//
//
//
//            for (c in 0 until jsonBean.get(i).getCityList().size) {//遍历该省份的所有城市
//
//                val CityName = jsonBean.get(i).getCityList().get(c).getName()
//
//                CityList.add(CityName)//添加城市
//
//                val City_AreaList = mutableListOf<String>()//该城市的所有地区列表
//
//
//                //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
//
//                if (jsonBean.get(i).getCityList().get(c).getArea() == null
//
//                    || jsonBean.get(i).getCityList().get(c).getArea().size == 0
//                ) {
//
//                    City_AreaList.add("")
//
//                } else {
//
//                    City_AreaList.addAll(jsonBean.get(i).getCityList().get(c).getArea())
//
//                }
//
//                Province_AreaList.add(City_AreaList)//添加该省所有地区数据
//
//            }
//
//
//            /**
//             *
//             * 添加城市数据
//             *
//             */
//
//            options2Items.add(CityList)
//
//
//            /**
//             *
//             * 添加地区数据
//             *
//             */
//
//            options3Items.add(Province_AreaList)
//
//        }


    }

    private fun initOptionPicker(title: String = "省份选择", changeIndex: Int, data: List<String>) {//条件选择器初始化


        /**
         *
         * 注意 ：如果是三级联动的数据(省市区等)，请参照 JsonDataActivity 类里面的写法。
         *
         */


        val pvOptions = OptionsPickerBuilder(activity, object : OnOptionsSelectListener {

            override fun onOptionsSelect(options1: Int, options2: Int, options3: Int, v: View?) {

                //返回的分别是三个级别的选中位置
//
//                val p = options1Items.get(options1).getPickerViewText()
//                val c = options2Items.get(options1).get(options2)
//                val a = options3Items.get(options1).get(options2).get(options3)

                when (changeIndex) {
                    0 -> {
                        tv_check_province_apply_agent.text = data[options1]
                        provinceIndex = options1
                    }
                    1 -> {
                        tv_check_city_apply_agent.text = data[options1]
                        cityIndex = options1
                    }
                    else -> {
                        tv_check_area_apply_agent.text = data[options1]
                        areaIndex = options1
                    }
                }
            }

        })

            .setTitleText(title)

            .setContentTextSize(20)//设置滚轮文字大小

            .setDividerColor(Color.LTGRAY)//设置分割线的颜色

            .setSelectOptions(0, 1)//默认选中项

            .setBgColor(Color.BLACK)

            .setTitleBgColor(Color.DKGRAY)

            .setTitleColor(Color.LTGRAY)

            .setCancelColor(Color.YELLOW)

            .setSubmitColor(Color.YELLOW)

            .setTextColorCenter(Color.LTGRAY)

            .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。

            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。

//            .setLabels("省", "市", "区")

            .setBackgroundId(0x00000000) //设置外部遮罩颜色

            .setOptionsSelectChangeListener { options1, options2, options3 ->
                //                val str = "options1: $options1\noptions2: $options2\noptions3: $options3"

            }

            .build<Any>()

        pvOptions.setPicker(data)

        pvOptions.show()

        //        pvOptions.setSelectOptions(1,1);

//        pvOptions.setPicker(options1Items.toList())//一级选择器

//        pvOptions.setPicker(options1Items, options2Items)//二级选择器

        /*pvOptions.setPicker(options1Items, options2Items,options3Items);//三级选择器*/

    }


}