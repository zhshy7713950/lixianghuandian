package wongxd.common.permission

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import wongxd.base.AppManager
import wongxd.common.dissmissProgressDialog


//
//
// 危险权限
//group:android.permission-group.CONTACTS
//permission:android.permission.WRITE_CONTACTS
//permission:android.permission.GET_ACCOUNTS
//permission:android.permission.READ_CONTACTS
//
//group:android.permission-group.PHONE
//permission:android.permission.READ_CALL_LOG
//permission:android.permission.READ_PHONE_STATE
//permission:android.permission.CALL_PHONE
//permission:android.permission.WRITE_CALL_LOG
//permission:android.permission.USE_SIP
//permission:android.permission.PROCESS_OUTGOING_CALLS
//permission:com.android.voicemail.permission.ADD_VOICEMAIL
//
//group:android.permission-group.CALENDAR
//permission:android.permission.READ_CALENDAR
//permission:android.permission.WRITE_CALENDAR
//
//group:android.permission-group.CAMERA
//permission:android.permission.CAMERA
//
//group:android.permission-group.SENSORS
//permission:android.permission.BODY_SENSORS
//
//group:android.permission-group.LOCATION
//permission:android.permission.ACCESS_FINE_LOCATION
//permission:android.permission.ACCESS_COARSE_LOCATION
//
//group:android.permission-group.STORAGE
//permission:android.permission.READ_EXTERNAL_STORAGE
//permission:android.permission.WRITE_EXTERNAL_STORAGE
//
//group:android.permission-group.MICROPHONE
//permission:android.permission.RECORD_AUDIO
//
//group:android.permission-group.SMS
//permission:android.permission.READ_SMS
//permission:android.permission.RECEIVE_WAP_PUSH
//permission:android.permission.RECEIVE_MMS
//permission:android.permission.RECEIVE_SMS
//permission:android.permission.SEND_SMS
//permission:android.permission.READ_CELL_BROADCASTS

/**
 * Created by wongxd on 2018/12/25.
 * https://github.com/wongxd
 * wxd1@live.com
 */
enum class PermissionType(val permission: String, val permissionName: String) {
    READ_CONTACTS(Manifest.permission.READ_CONTACTS, "读取联系人"),
    WRITE_CONTACTS(Manifest.permission.WRITE_CONTACTS, "写入联系人"),
    GET_ACCOUNTS(Manifest.permission.GET_ACCOUNTS, "读取账户"),


    READ_CALL_LOG(Manifest.permission.READ_CALL_LOG, "读取通话记录"),
    READ_PHONE_STATE(Manifest.permission.READ_PHONE_STATE, "读取手机状态"),
    CALL_PHONE(Manifest.permission.CALL_PHONE, "拨打电话"),
    WRITE_CALL_LOG(Manifest.permission.WRITE_CALL_LOG, "写入通话记录"),
    USE_SIP(Manifest.permission.USE_SIP, "获得用户SIP"),
    PROCESS_OUTGOING_CALLS(Manifest.permission.PROCESS_OUTGOING_CALLS, "处理呼出电话"),
    ADD_VOICEMAIL(Manifest.permission.ADD_VOICEMAIL, "添加语音邮件"),


    READ_CALENDAR(Manifest.permission.READ_CALENDAR, "读取日历"),
    WRITE_CALENDAR(Manifest.permission.WRITE_CALENDAR, "写入日历"),


    CAMERA(Manifest.permission.CAMERA, "拍照"),


    @RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
    BODY_SENSORS(Manifest.permission.BODY_SENSORS, "设备传感器"),


    FINE_LOCATION(Manifest.permission.ACCESS_FINE_LOCATION, "获取精确位置"),
    COARSE_LOCATION(Manifest.permission.ACCESS_COARSE_LOCATION, "获取大致位置"),


    READ_EXTERNAL_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE, "读取存储卡"),
    WRITE_EXTERNAL_STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE, "写入存储卡"),


    RECORD_AUDIO(Manifest.permission.RECORD_AUDIO, "录音"),


    READ_SMS(Manifest.permission.READ_SMS, "读取短信"),
    RECEIVE_WAP_PUSH(Manifest.permission.RECEIVE_WAP_PUSH, "接收 WAP_PUSH"),
    RECEIVE_MMS(Manifest.permission.RECEIVE_MMS, "接收MMS"),
    RECEIVE_SMS(Manifest.permission.RECEIVE_SMS, "接收SMS"),
    SEND_SMS(Manifest.permission.SEND_SMS, "发送SMS"),


    ACCESS_WIFI_STATE(Manifest.permission.ACCESS_WIFI_STATE, "访问WiFi网络信息"),
    ACCESS_NETWORK_STATE(Manifest.permission.ACCESS_NETWORK_STATE, "获取网络状态");
}

fun getPermissionsWithTips(aty: FragmentActivity?,
                           vararg per: PermissionType,
                           contentText: String,
                           result: (Boolean, List<PermissionActivityResult.Permission>) -> Unit = { isAllGranted, perList -> },
                           allGranted: () -> Unit = {},
                           isGoSetting: Boolean = false){
    var isAllGranted = false
    per.forEach {
        isAllGranted = aty?.checkSelfPermission(it.permission) == PackageManager.PERMISSION_GRANTED
    }
    if(isAllGranted){
        allGranted.invoke()
        return
    }
    val dlg: SweetAlertDialog = SweetAlertDialog(aty, SweetAlertDialog.NORMAL_TYPE)
        .also {
            it.titleText = "温馨提示"
            it.contentText = contentText
            it.confirmText = "允许权限"
            it.cancelText = "暂不授权"
        }
    with(dlg){
        setConfirmClickListener {
            getPermissions(aty, *per, result = result,allGranted = allGranted,isGoSetting = isGoSetting)
            it.dismiss()
        }
        setCancelClickListener {
            it.dismiss()
        }
        setCancelable(true)
        show()
    }
}

/**
 * 获取权限
 */
fun getPermissions(
    aty: FragmentActivity?,
    vararg per: PermissionType,
    result: (Boolean, List<PermissionActivityResult.Permission>) -> Unit = { isAllGranted, perList -> },
    granterResult: (List<PermissionActivityResult.Permission>) -> Unit = { perList -> },
    notGranterResult: (List<PermissionActivityResult.Permission>) -> Unit = { perList -> },
    allGranted: () -> Unit = {},
    isGoSetting: Boolean = true

) {
    if (aty == null) return

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        allGranted.invoke()
        return
    }

    val permissionReq = PermissionActivityResult.getOne(aty.supportFragmentManager)

    permissionReq.reqPermission(per.map { it.permission }.toTypedArray()) { isAllGranted, perList ->
        if (isAllGranted) {
            allGranted.invoke()
            return@reqPermission
        }

        result.invoke(isAllGranted, perList)
        granterResult.invoke(perList.filter { it.granted })
        notGranterResult.invoke(perList.filter { !it.granted })
        if (isGoSetting) {
            goSettingWithSweetAlertDialog(aty, perList.filter { !it.granted })
        }
    }

}

fun getPermissions(perlist: List<PermissionType>, allGranted: () -> Unit) {
    getPermissions(
        AppManager.getAppManager().currentActivity() as FragmentActivity,
        *perlist.toTypedArray(),
        allGranted = allGranted
    )
}

private fun goSetting(ctx: Context) {
    val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
    intent.data = Uri.parse("package:${ctx.applicationContext.packageName}")
    ctx.startActivity(intent)
}

private fun goSettingWithSweetAlertDialog(aty: Activity?, pers: List<PermissionActivityResult.Permission>) {

    val dlg: SweetAlertDialog = SweetAlertDialog(aty, SweetAlertDialog.WARNING_TYPE)
        .also {
            it.titleText = "有如下权限被禁止"
            val sb = StringBuilder()
            pers.forEach { per -> sb.append("${per.name}\n") }
            sb.append("(将会导致应用不能正常运行)")
            it.contentText = sb.toString()
            it.confirmText = "前往设置给予权限"

        }
    dlg.setConfirmClickListener {
        aty?.let { goSetting(it)
        }
        dlg.dismiss()
    }
    dlg.setCancelable(true)
    dlg.show()

}


//###########################具体实现###########################################

class PermissionActivityResult {

    private val TAG = "PermissionActivityResult"
    private var mPermissionActivityResultFragment: PermissionActivityResultFragment? = null


    companion object {

        fun getOne(fm: FragmentManager): PermissionActivityResult {
            val one = PermissionActivityResult()
            one.mPermissionActivityResultFragment = one.getOnResultFragment(fm)
            return one
        }

    }


    fun reqPermission(permissions: Array<String>, eachResult: PermissionResultCallback) {
        requestPermissionsFromFragment(permissions, eachResult)
    }


    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionsFromFragment(permissions: Array<String>, lis: PermissionResultCallback) {
        mPermissionActivityResultFragment?.requestPermissions(permissions, lis)
    }

    /**
     * Returns true if the permission is already granted.
     *
     *
     * Always true if SDK < 23.
     */
    fun isGranted(permission: String): Boolean {
        return !isMarshmallow() || mPermissionActivityResultFragment?.isGranted(permission)!!
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     *
     *
     * Always false if SDK < 23.
     */
    fun isRevoked(permission: String): Boolean {
        return isMarshmallow() && mPermissionActivityResultFragment?.isRevoked(permission)!!
    }

    internal fun isMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }


    fun setLogging(logging: Boolean) {
        mPermissionActivityResultFragment?.setLogging(logging)
    }

    private fun getOnResultFragment(fragmentManager: FragmentManager): PermissionActivityResultFragment {
        var simpleOnResultFragment: PermissionActivityResultFragment? =
            findSimpleOnResultFragment(fragmentManager)
        if (simpleOnResultFragment == null) {
            simpleOnResultFragment = PermissionActivityResultFragment()
            fragmentManager
                .beginTransaction()
                .add(simpleOnResultFragment, TAG)
                .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }
        return simpleOnResultFragment
    }

    private fun findSimpleOnResultFragment(fragmentManager: FragmentManager): PermissionActivityResultFragment? {
        return fragmentManager.findFragmentByTag(TAG) as PermissionActivityResultFragment?
    }


    class Permission(
        val perStr: String,
        val name: String,
        val granted: Boolean,
        val shouldShowRequestPermissionRationale: Boolean = false
    ) {

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false

            val that = o as Permission?

            if (granted != that?.granted) return false
            return if (shouldShowRequestPermissionRationale != that.shouldShowRequestPermissionRationale) false else name == that.name
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + if (granted) 1 else 0
            result = 31 * result + if (shouldShowRequestPermissionRationale) 1 else 0
            return result
        }

        override fun toString(): String {
            return "Permission{" +
                    "name='" + name + '\''.toString() +
                    ", granted=" + granted +
                    ", shouldShowRequestPermissionRationale=" + shouldShowRequestPermissionRationale +
                    '}'.toString()
        }

    }


    /**
     *
     * @Description: 真正调用 startActivity 和处理 onActivityResult 的类。
     */
    class PermissionActivityResultFragment : Fragment() {

        private val TAG = "Permission"
        private val PERMISSIONS_REQUEST_CODE = 42
        private var mLogging: Boolean = true

        private var listener: PermissionResultCallback? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            retainInstance = true
        }


        @TargetApi(Build.VERSION_CODES.M)
        internal fun requestPermissions(permissions: Array<String>, lis: PermissionResultCallback) {
            listener = lis
            log("请求权限  " + TextUtils.join(",", permissions))
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            if (requestCode != PERMISSIONS_REQUEST_CODE) return

            val jsutGrantSize = grantResults.filter { it == PackageManager.PERMISSION_GRANTED }.size
            val shouldShowRequestPermissionRationale = BooleanArray(permissions.size)

            log("获取权限结果  permissions  " + permissions.size + "  grantResults  " + jsutGrantSize)

            for (i in permissions.indices) {
                shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(permissions[i])
            }

            val isAllGranted = permissions.size == grantResults.filter { it == PackageManager.PERMISSION_GRANTED }.size
            val permissionList: MutableList<Permission> = mutableListOf()

            log("需要重新请求的权限个数  " + shouldShowRequestPermissionRationale.filter { it }.size)

            listener?.let { lis ->

                permissions.forEachIndexed { index, per ->
                    val granted = grantResults[index] == PackageManager.PERMISSION_GRANTED
                    val rationale = shouldShowRequestPermissionRationale[index]
                    permissionList.add(Permission(per, getNameByPermissionStr(per), granted, rationale))
                }

                lis.invoke(isAllGranted, permissionList)
            }

        }

        private fun getNameByPermissionStr(perStr: String): String {
            return try {
                PermissionType.values().first { it.permission == perStr }.permissionName
            } catch (e: Exception) {
                e.printStackTrace()
                "Unknown Permission"
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        internal fun isGranted(permission: String): Boolean {
            return activity?.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }

        @TargetApi(Build.VERSION_CODES.M)
        internal fun isRevoked(permission: String): Boolean? {
            return activity?.packageManager?.isPermissionRevokedByPolicy(permission, activity?.packageName!!)
        }


        fun setLogging(logging: Boolean) {
            mLogging = logging
        }

        internal fun log(message: String) {
            if (mLogging) {
                Log.e(TAG, message)
            }
        }
    }

}

/**
 *
 * 是否获得全部权限
 *
 * perList
 *
 */
private typealias  PermissionResultCallback = (Boolean, List<PermissionActivityResult.Permission>) -> Unit



