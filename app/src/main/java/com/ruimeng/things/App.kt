package com.ruimeng.things

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.ruimeng.things.shop.TkHttp
import com.ruimeng.things.shop.tkLogin
import wongxd.Http
import wongxd.Wongxd
import wongxd.base.AppManager
import wongxd.base.crash.CaocConfig
import wongxd.common.EasyToast
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * Created by wongxd on 2018/11/9.
 */
class App : Wongxd() {

    companion object {
        fun getMainAty(): AppCompatActivity {
            return AppManager.getActivity(AtyMain::class.java) as AppCompatActivity
        }

        var lat = 0.0
        var lng = 0.0
        var province = ""
        var city = ""
    }

    override fun onCreate() {
        super.onCreate()

        CaocConfig.Builder()
            .apply()

        initCloudChannel(this)

        TkHttp.appId = "822"
        TkHttp.appKey = "eb8ec6b3e7a3afbc54962db73e86bdec"
        TkHttp.TOKEN_LOST_FUN = { msg ->
            val userInfo = UserInfoLiveData.getFromString()

            if (userInfo.unionid.isNotBlank()) {
                tkLogin(userInfo.username, userInfo.unionid)
            }
        }


        Http.appId = "1000"
        Http.appKey = "23befeadc6017e5ff4e18e3fafb10a5e"
        Http.host = getString(R.string.home_url)
        Http.TOKEN_LOST_FUN = { msg ->
            EasyToast.DEFAULT.show(msg)
            val aty = AppManager.getAppManager().currentActivity()
            val i = Intent(aty, AtyLogin::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            aty.startActivity(i)
        }
    }


    /**
     * 初始化云推送通道
     * @param applicationContext
     */
    private fun initCloudChannel(applicationContext: Context) {
        try{
            PushServiceFactory.init(applicationContext)
            val pushService = PushServiceFactory.getCloudPushService()
            pushService.register(applicationContext, object : CommonCallback {
                override fun onSuccess(p0: String?) {
                    Log.d("w-", "init cloudchannel success   $p0")
                }

                override fun onFailed(p0: String?, p1: String?) {
                    Log.d(
                        "w-", "init cloudchannel failed -- errorcode:$p0 -- errorMessage:$p1"
                    )
                }

            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    /**
     * 忽略https的证书校验
     * 避免Glide加载https图片报错：
     * javax.net.ssl.SSLHandshakeException: java.security.cert.CertPathValidatorException: Trust anchor for certification path not found.
     */
    fun handleSSLHandshake() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return emptyArray()
                }

                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}

                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            })

            val sc = SSLContext.getInstance("TLS")
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { hostname, session -> true }
        } catch (ignored: Exception) {
        }

    }

}
