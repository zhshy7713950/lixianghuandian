package wongxd

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.multidex.MultiDex
import com.scwang.smartrefresh.header.WaterDropHeader
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.uuzuche.lib_zxing.activity.ZXingLibrary
import com.wongxd.R
import wongxd.base.AppManager
import wongxd.utils.utilcode.util.Utils


/**
 * Created by wongxd on 2018/11/9.
 */
open class Wongxd : Application() {

    companion object {


        private var initApp: Wongxd? = null

        val instance: Wongxd by lazy {
            initApp ?: throw RuntimeException("请确保 application 继承自  Wongxd ")
        }

        //static 代码段可以防止内存泄露
        init {
            //设置全局的Header构建器
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
                layout.setPrimaryColorsId(R.color.gray_9, android.R.color.white)//全局设置主题颜色
                WaterDropHeader(context)
            }
            //设置全局的Footer构建器
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                //指定为经典Footer，默认是 BallPulseFooter
                ClassicsFooter(context).setDrawableSize(20f)
            }
        }
    }


    override
    fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }



    override
    fun onCreate() {
        super.onCreate()

        initApp = this

        Utils.init(this)
        Config.init(this)
        //几行代码快速集成Zxing
        ZXingLibrary.initDisplayOpinion(this)








      registerActivityLifecycleCallbacks(object :ActivityLifecycleCallbacks{
          override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
              AppManager.getAppManager().addActivity(activity)
          }

          override fun onActivityStarted(activity: Activity) {
          }

          override fun onActivityResumed(activity: Activity) {
          }

          override fun onActivityPaused(activity: Activity) {
          }

          override fun onActivityStopped(activity: Activity) {
          }

          override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
          }

          override fun onActivityDestroyed(activity: Activity) {
              AppManager.getAppManager().removeActivity(activity)
          }
      })

    }


}