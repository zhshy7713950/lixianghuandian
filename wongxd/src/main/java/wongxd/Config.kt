package wongxd

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import wongxd.base.aCache.AcacheUtil
import wongxd.utils.utilcode.util.SPUtils


/**
 * Created by wongxd on 2018/10/18.
 * https://github.com/wongxd
 * wxd1@live.com
 */
class Config private constructor() {

    companion object {
        private val sInstances by lazy { Config() }
        private var appInstance: Application? = null
        fun init(app: Application) {
            appInstance = app
        }

        fun getDefault() = sInstances
    }

    init {
        if (appInstance == null) {
            throw RuntimeException("你必须 在 Application 先调用 Config.init(this) ")
        }
    }

    private val packageInfo by
    lazy { appInstance!!.getPackageManager().getPackageInfo(appInstance?.getPackageName(), 0) }

    val versionCode: Int by lazy { packageInfo.versionCode }

    val versionName: String  by lazy { packageInfo.versionName }

    val packageName: String by lazy { packageInfo.packageName }

    val applicationInfo: ApplicationInfo by
    lazy {
        appInstance!!.getPackageManager()
            .getApplicationInfo(appInstance?.getPackageName(), PackageManager.GET_META_DATA)
    }

    //###############配置字符串################

    val spUtils by lazy { SPUtils.getInstance(this.javaClass.simpleName) }

    val stringCacheUtils by lazy { AcacheUtil.getDefault(appInstance!!.applicationContext, AcacheUtil.StringCache) }

    val objCacheUtils by lazy { AcacheUtil.getDefault(appInstance!!.applicationContext, AcacheUtil.ObjectCache) }


    //###############配置字符串################


    private var tempToken: String? = null
    var token: String
        get() {
            val temp = tempToken ?: spUtils.getString("token", "")
            tempToken = temp
            Http.token = temp
            return temp
        }
        set(value) {
            Http.token = value
            tempToken = value
            spUtils.put("token", value)
        }

}