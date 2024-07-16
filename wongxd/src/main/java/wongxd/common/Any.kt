package wongxd.common

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import wongxd.base.AppManager
import wongxd.utils.utilcode.util.Utils
import java.text.SimpleDateFormat
import java.util.Date


/**
 * Created by wongxd on 2018/06/15.
 *            https://github.com/wongxd
 *            wxd1@live.com
 *
 */

private fun doDp2Px(dpValue: Float): Float {
    return (0.5f + dpValue * Resources.getSystem().displayMetrics.density)
}

private fun doPx2Dp(dpValue: Float): Float {
    return (0.5f + dpValue / Resources.getSystem().displayMetrics.density)
}

private fun doSp2Px(spValue: Float): Float {
    return (0.5f + spValue * Resources.getSystem().displayMetrics.scaledDensity)
}

fun Int.px2Dp(): Float = doPx2Dp(this.toFloat())
fun Int.sp2px(): Float = doSp2Px(this.toFloat())

fun Float.dp2px(): Float = doDp2Px(this)

fun Int.dp2px(): Float = doDp2Px(this.toFloat())

val Float.dp2px: Float
    get() = dp2px()

val Int.dp2px: Float
    get() = dp2px()

fun getCurrentAppAty(): Activity = AppManager.getAppManager().currentActivity()

fun getCurrentAty(): AppCompatActivity =
    AppManager.getAppManager().currentActivity() as AppCompatActivity

fun getSweetDialog(
    appCompatActivity: AppCompatActivity,
    type: Int,
    msg: String,
    cancelable: Boolean = true,
    confirmLis: () -> Unit = {}
): SweetAlertDialog {
    val dlg = SweetAlertDialog(appCompatActivity, type)
    val thisCancelable = if (type == SweetAlertDialog.ERROR_TYPE) false else cancelable
    dlg.titleText = msg
    dlg.confirmText = "确定"
    dlg.setCancelable(thisCancelable)
    dlg.setCanceledOnTouchOutside(thisCancelable)
    dlg.setConfirmClickListener {
        it.dismissWithAnimation()
        confirmLis.invoke()
    }

    return dlg
}

fun getSweetDialog(
    type: Int,
    msg: String,
    cancelable: Boolean = true,
    confirmLis: () -> Unit = {}
): SweetAlertDialog {
    val dlg = SweetAlertDialog(getCurrentAty(), type)
    val thisCancelable = if (type == SweetAlertDialog.ERROR_TYPE) false else cancelable
    dlg.titleText = msg
    dlg.confirmText = "确定"
    dlg.setCancelable(thisCancelable)
    dlg.setCanceledOnTouchOutside(thisCancelable)
    dlg.setConfirmClickListener {
        it.dismissWithAnimation()
        confirmLis.invoke()
    }

    return dlg
}

private var progressDialog: ProgressDialog? = null

fun showProgressDialog(msg: String = "请求中。。。") {
    try {
        progressDialog?.dismiss()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    progressDialog = ProgressDialog(getCurrentAty())
    progressDialog?.setTitle(msg)
    progressDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)

    progressDialog?.show()
}


fun dissmissProgressDialog() {
    try {
        progressDialog?.dismiss()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    progressDialog = null
}

inline fun <reified T : ViewModel> createVM(fgt: Fragment): T {
    return ViewModelProviders.of(fgt).get(T::class.java)
}

inline fun <reified T : ViewModel> createVM(aty: AppCompatActivity): T {
    return ViewModelProviders.of(aty).get(T::class.java)
}


fun Long.sec2hour(s: Long = this): String {

    fun unitFormat(i: Long): String {
        return if (i in 0..9)
            "0" + i.toString()
        else
            "" + i
    }

    var timeStr: String = ""
    var hour = 0L
    var minute = 0L
    var second = 0L
    if (s <= 0)
        return "00:00"
    else {
        minute = s / 60
        if (minute < 60) {
            second = s % 60
            timeStr = unitFormat(minute) + ":" + unitFormat(second)
        } else {
            hour = minute / 60
            if (hour > 99)
                return "99:59:59"
            minute = minute % 60
            second = s - hour * 3600 - minute * 60
            timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second)
        }
    }
    return timeStr

}


fun <A, B> bothNotNull(a: A?, b: B?, then: (a: A, b: B) -> Unit) {
    a?.let { aa ->
        b?.let { bb ->
            then.invoke(aa, bb)
        }
    }
}


val gson by lazy { Gson() }

/**
 * 把 json 字符串 序列化为 java对象
 */
inline fun <reified P : Any> String.toPOJO(): P {
    return gson.fromJson(this, P::class.java)
}

inline fun <reified A : Activity> Fragment.startAty(vararg params: Pair<String, String>) {
    this.activity?.startAty<A>(*params)
}

inline fun <reified A : Activity> Activity.startAty(vararg params: Pair<String, String>) {
    val intent = Intent(this, A::class.java)

    params.forEach {
        intent.putExtra(it.first, it.second)
    }

    this.startActivity(intent)
}

inline fun <reified A : Activity> Fragment.startAty(bundle: Bundle?) {
    this.activity?.startAty<A>(bundle)
}

inline fun <reified A : Activity> Activity.startAty(bundle: Bundle?) {
    val intent = Intent(this, A::class.java)
    bundle?.let { intent.putExtras(bundle) }
    this.startActivity(intent)
}


fun ImageView.loadImg(path: Any) {

//    val optins: RequestOptions = RequestOptions()
//            .centerCrop()

    Glide.with(this.context).load(path)
//            .apply(optins)
//        .centerCrop()
        .into(this)

}


fun ImageView.loadBigImg(path: Any) {


//    val optins: RequestOptions = RequestOptions()
//            .centerCrop()
//            .override(Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)

    Glide.with(this.context).load(path)
//            .apply(optins)
//        .centerCrop()
//        .override(Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)
        .into(this)

}


/**
 * https://www.china-7.net/view-486463.html
 */
fun openInApp(url: String, ctx: Context): Boolean {

    if (url.contains("tpopen://") || url.contains("taobao://") || url.contains("taobao.")
        || url.contains("tbopen://")
    ) {
        if (checkPackage(ctx, "com.taobao.taobao")) {
            val trolleyIntent: Intent = Intent()
            trolleyIntent.action = "android.intent.action.VIEW"
            val uri = Uri.parse(
                url.replace("https://", "taobao://")
                    .replace("http://", "taobao://")
                    .replace("tpopen://", "taobao://")
                    .replace("tbopen://", "taobao://")
            )
            trolleyIntent.data = uri
            ctx.startActivity(trolleyIntent)

            return true
        } else {
            Toast.makeText(ctx, "手机未安装淘宝!", Toast.LENGTH_SHORT).show()
        }


    }



    if (url.contains("tmall:") || url.contains("tmall://") || url.contains("tmall.")) {

        if (checkPackage(ctx, "com.tmall.wireless")) {
            val trolleyIntent: Intent = Intent()
            trolleyIntent.action = "android.intent.action.VIEW"
            val uri = Uri.parse(url.replace("https://", "tmall://").replace("http://", "tmall://"))
            trolleyIntent.data = uri
            ctx.startActivity(trolleyIntent)


            return true
        } else {
            Toast.makeText(ctx, "手机未安装天猫!", Toast.LENGTH_SHORT).show()
        }

    }



    if (url.contains("jd:") || url.contains("jingdong://") || url.contains("openapp.jdmobile:") || url.contains(
            "jd."
        )
    ) {

        if (checkPackage(ctx, "com.jingdong.app.mall")) {
            val trolleyIntent: Intent = Intent()
            trolleyIntent.action = "android.intent.action.VIEW"
            //com.jd.lib.icssdk.ui.activity.ActivityWebView
            //http://item.jd.com/26725932622.html
            //openApp.jdMobile://virtual?params={\"category\":\"jump\",\"des\":\"productDetail\",\"skuId\":\"%@\",\"sourceType\":\"homefloor\",\"sourceValue\":\"4384\",\"landPageId\":\"jshop.cx.mobile\"}
            val jdUrl = url.replace("http://item.jd.com/", "").replace("https://item.jd.com/", "")
                .replace(".html", "").replace(".htm", "")
                .replace("https://u.jd.com/", "") //优惠券

            val jdUri = if (url.contains("u.jd.")) {
//                "openapp.jdmobile://virtual?params=%7B%22sourceValue%22:%220_getCoupon_97%22,%22des%22:%22getCoupon%22,%22skuId%22:%22$jdUrl%22,%22category%22:%22jump%22,%22sourceType%22:%22PCUBE_CHANNEL%22%7D"
//                "openapp.jdmobile://virtual?params=%7b%22category%22%3a%22jump%22%2c%22des%22%3a%22getCoupon%22%2c%22skuId%22%3a%22$jdUrl%22%2c%22sourceType%22%3a%22homefloor%22%2c%22sourceValue%22%3a%224384%22%2c%22landPageId%22%3a%22jshop.cx.mobile%22%7d"

                return false
            } else {
                "openapp.jdmobile://virtual?params=%7B%22sourceValue%22:%220_productDetail_97%22,%22des%22:%22productDetail%22,%22skuId%22:%22$jdUrl%22,%22category%22:%22jump%22,%22sourceType%22:%22PCUBE_CHANNEL%22%7D"
//                "openapp.jdmobile://virtual?params={\"category\":\"jump\",\"des\":\"productDetail\",\"skuId\":\"$jdUrl\",\"sourceType\":\"homefloor\",\"sourceValue\":\"4384\",\"landPageId\":\"jshop.cx.mobile\"}"
//                "openapp.jdmobile://virtual?params={\"sourceValue\":\"0_productDetail_97\",\"des\":\"productDetail\",\"skuId\":\"$jdUrl\",\"category\":\"jump\",\"sourceType\":\"PCUBE_CHANNEL\"}"
            }

            val uri = Uri.parse(jdUri)
            trolleyIntent.data = uri
            ctx.startActivity(trolleyIntent)


            return true
        } else {
            Toast.makeText(ctx, "手机未安装京东!", Toast.LENGTH_SHORT).show()
        }


    }



    if (url.contains("pinduoduo:") || url.contains("pinduoduo.") || url.contains("yangkeduo")) {

        val intentUrl = url.replace("https://mobile.yangkeduo.com/", "")

        if (checkPackage(ctx, "com.xunmeng.pinduoduo")) {
            val trolleyIntent: Intent = Intent()
            trolleyIntent.action = "android.intent.action.VIEW"
            val uri = Uri.parse("pinduoduo://com.xunmeng.pinduoduo/" + url)
            trolleyIntent.data = uri
            ctx.startActivity(trolleyIntent)


            return true
        } else {
            Toast.makeText(ctx, "手机未安装拼多多!", Toast.LENGTH_SHORT).show()
            return true
        }

    }


    return false
}

fun openInSysBrowser(ctx: Context, url: String) {
    val intent = Intent()
    intent.action = "android.intent.action.VIEW"
    val content_url = Uri.parse(url)
    intent.data = content_url
    ctx.startActivity(intent)
}

fun checkPackage(context: Context, packageName: String?): Boolean {
    if (packageName == null || "" == packageName) return false
    return try {
        context.packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_ACTIVITIES
        )
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

}

fun I(tag: String = "wongxd", info: String) {  //信息太长,分段打印

    var msg = info

    //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，

    //  把4*1024的MAX字节打印长度改为2001字符数

    val max_str_length = 2001 - tag.length

    //大于4000时

    while (msg.length > max_str_length) {

        Log.i(tag, msg.substring(0, max_str_length))

        msg = msg.substring(max_str_length)

    }

    //剩余部分

    Log.i(tag, msg)

}


/**
 * 时间戳转 "yyyy-MM-dd HH:mm"
 * @param customFmt "yyyy-MM-dd HH:mm" 自定义显示格式
 */
fun Long.getTime(
    isShowHour: Boolean = true,
    splitStr: String = "-",
    customFmt: String = ""
): String {
    val res: String
    val fmt = if (customFmt.isBlank()) {
        if (isShowHour) {
            "yyyy${splitStr}MM${splitStr}dd HH:mm"
        } else {
            "yyyy${splitStr}MM${splitStr}dd"
        }
    } else {
        customFmt
    }

    val simpleDateFormat = SimpleDateFormat(fmt)
    val date = Date(if (this.toString().length == 10) this * 1000 else this)
    res = simpleDateFormat.format(date)
    return res
}

