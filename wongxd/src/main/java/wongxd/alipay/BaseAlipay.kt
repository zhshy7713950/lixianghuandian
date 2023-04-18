package wongxd.alipay

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.alipay.sdk.app.PayTask
import wongxd.common.MainLooper
import wongxd.common.getCurrentAty
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions

/**
 * Created by wongxd on 2018/11/30.
 */
object BaseAlipay {

    /**
     * 获取支付宝支付的相关权限
     * @param get 获得权限后要执行的代码
     */
    fun getAlipayPermissions(get: () -> Unit) {
        getPermissions(
            getCurrentAty(),
            PermissionType.READ_PHONE_STATE,
            PermissionType.WRITE_EXTERNAL_STORAGE,
            allGranted = {
                get.invoke()
            })
    }


    /**
     * 尝试支付
     *
     * @param block 当支付结束后 的回调 (resultInfo,resultStatus,isLocalSuccessed)
     */
    fun tryPay(payStr: String, block: (String, String, Boolean) -> Unit) {

        getAlipayPermissions {
            Thread(Runnable {

                val alipay = PayTask(getCurrentAty())
                val result = alipay.payV2(payStr, true)
                MainLooper.runOnUiThread {

                    val payResult = PayResult(result)
                    /**
                    对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    val resultInfo = payResult.result// 同步返回需要验证的信息
                    val resultStatus = payResult.resultStatus
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        block.invoke(resultInfo, resultStatus, true)
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        block.invoke(resultInfo, resultStatus, false)
                    }

                }
            }).start()
        }

    }


    /**
     * 尝试H5 转到 原生支付
     *
     * @param url
     *
     */
    fun tryH5Pay(url: String) {

        getAlipayPermissions {

            val intent = Intent(getCurrentAty(), AlipayH5Activity::class.java)
            val extras = Bundle()

            /*
     * URL 是要测试的网站，在 Demo App 中会使用 H5PayDemoActivity 内的 WebView 打开。
     *
     * 可以填写任一支持支付宝支付的网站（如淘宝或一号店），在网站中下订单并唤起支付宝；
     * 或者直接填写由支付宝文档提供的“网站 Demo”生成的订单地址
     * （如 https://mclient.alipay.com/h5Continue.htm?h5_route_token=303ff0894cd4dccf591b089761dexxxx）
     * 进行测试。
     *
     * H5PayDemoActivity 中的 MyWebViewClient.shouldOverrideUrlLoading() 实现了拦截 URL 唤起支付宝，
     * 可以参考它实现自定义的 URL 拦截逻辑。
     */
            extras.putString("url", url)
            intent.putExtras(extras)
            getCurrentAty().startActivity(intent)

        }


    }
}