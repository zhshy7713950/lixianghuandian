package com.ruimeng.things.me

import android.os.Bundle
import com.ruimeng.things.AtyLogin
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.UserInfoLiveData
import kotlinx.android.synthetic.main.fgt_setting.*
import wongxd.AtyWeb
import wongxd.Config
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.http
import wongxd.utils.SystemUtils
import wongxd.utils.utilcode.util.CacheUtils

/**
 * Created by wongxd on 2018/11/14.
 */
class FgtSetting : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_setting

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "设置")

        ll_clean_cache_setting.setOnClickListener {
            CacheUtils.getInstance().clear()
            it.postDelayed({
                EasyToast.DEFAULT.show("缓存清除成功")
            }, 800)
        }


        ll_help_center_setting.setOnClickListener {
            http {
                method = "get"
                url = Path.HELP

                onResponse {
                    AtyWeb.start("帮助中心", it)
                }
            }
        }


        ll_about_us_setting.setOnClickListener {
            http {
                method = "get"
                url = Path.ABOUT_ME

                onResponse {
                    AtyWeb.start("关于我们", it)
                }
            }
        }


        btn_logout_setting.setOnClickListener {
            Config.getDefault().token = ""
            Config.getDefault().stringCacheUtils.remove(UserInfoLiveData.STORE_KEY)
            SystemUtils.cleanTask2Activity(activity, AtyLogin::class.java)
        }

        //1,获取包 管理器
        val packageManager = activity?.packageManager
        //2,通过上下文获取包名
        val packageName = activity?.packageName
        //3,获取包的信息
        val packageInfo = packageManager?.getPackageInfo(packageName, 0)
        //4,获取版本号
        val versionCode = packageInfo?.versionCode
        //5,获取版本名
        val versionName = packageInfo?.versionName ?: "未知版本"

        tv_version_setting.text = "当前版本:v$versionName($versionCode)"
    }
}