package com.ruimeng.things

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import android.text.TextUtils
import android.util.Log
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ruimeng.things.bean.ConfigBean
import com.ruimeng.things.home.AtyScanQrcode
import com.ruimeng.things.home.FgtChangeRentBattery
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.FgtPackageBind
import com.ruimeng.things.shop.tkLogin
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.vector.update_app.UpdateAppBean
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import wongxd.Config
import wongxd.UpdateAppHttpUtil
import wongxd.base.BaseBackActivity
import wongxd.common.EasyToast
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import wongxd.updateApp.check
import wongxd.updateApp.updateApp

class AtyMain : BaseBackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        InfoViewModel.getDefault()

        UserInfoLiveData.refresh()

        setContentView(R.layout.aty_main)

        setSwipeBackEnable(false)

        loadRootFragment(R.id.fl_aty_main, FgtMain())

        getPermissions(
            this,
            PermissionType.WRITE_EXTERNAL_STORAGE,
            PermissionType.READ_EXTERNAL_STORAGE,
            allGranted = { configUpgrade() })

        getAppConfig()


        InfoViewModel.getDefault().userInfo.simpleObserver(this) { userInfo ->
            tkLogin(userInfo.username, userInfo.unionid)
        }


        dealNotification()

        val pushService = PushServiceFactory.getCloudPushService()
        bindPush(pushService.deviceId, pushService.deviceId)


    }

    private fun bindPush(sdk_userid: String, sdk_channelid: String) {

        http {
            url = Path.BIND_PUSH
            params["sdk_userid"] = sdk_userid
            params["sdk_channelid"] = sdk_channelid
            params["os"] = "android"

            onSuccess {

            }
        }
    }


    private fun configUpgrade() {
        //下载路径
        val path = Environment.getExternalStorageDirectory().absolutePath
        //自定义参数
        val params = HashMap<String, String>()

        params["package_name"] = Config.getDefault().packageName
        params["os"] = "android"
        params["ver"] = Config.getDefault().versionCode.toString()

        val mUpdateUrl = Path.CHECK_UPGRADE


        updateApp(mUpdateUrl, UpdateAppHttpUtil())
        //自定义配置
        {
            //以下设置，都是可选
            //设置请求方式，默认get
            isPost = true
            //添加自定义参数，默认version=1.0.0（app的versionName）；apkKey=唯一表示（在AndroidManifest.xml配置）
            setParams(params)
            //设置点击升级后，消失对话框，默认点击升级后，对话框显示下载进度
//            hideDialogOnDownloading()
            //设置头部，不设置显示默认的图片，设置图片后自动识别主色调，然后为按钮，进度条设置颜色
            topPic = R.drawable.top_8
            //为按钮，进度条设置颜色，默认从顶部图片自动识别。
            //setThemeColor(ColorUtil.getRandomColor())
            themeColor = resources.getColor(R.color.app_red)
            //设置apk下砸路径，默认是在下载到sd卡下/Download/1.0.0/test.apk
            targetPath = path
            //设置appKey，默认从AndroidManifest.xml获取，如果，使用自定义参数，则此项无效
            //setAppKey("ab55ce55Ac4bcP408cPb8c1Aaeac179c5f6f")

        }
            .check {
                onBefore {

                }
                //自定义解析
                parseJson {
                    val json = JSONObject(it)

                    if (json.optInt("errcode") != 200) {
                        UpdateAppBean()
                    } else {

//                        AccessibilityUtil.checkSetting(
//                            applicationContext,
//                            AutoInstallService::class.java
//                        )
//                        InstallUtil.checkSetting(applicationContext)

                        val data = json.optJSONObject("data")

                        val update_force = data.optInt("update_force")
                        val update_msg = data.optString("update_msg")
                        val update_url = data.optString("update_url")

                        UpdateAppBean()
                            //（必须）是否更新Yes,No
                            .setUpdate("Yes")
                            //（必须）新版本号，
                            .setNewVersion("")
                            //（必须）下载地址
                            .setApkFileUrl(update_url)
                            //（必须）更新内容
                            .setUpdateLog(update_msg)
                            //大小，不设置不显示大小，可以不设置
//                        .setTargetSize("")
                            //是否强制更新，可以不设置
                            .setConstraint(update_force == 1)
                        //设置md5，可以不设置
//                        .setNewMd5("")

                    }
                }
                noNewApp {
                    //                    toast("没有新版本")
                }
                onAfter {

                }
            }

    }

    private fun getAppConfig() {
        http {
            url = Path.GET_CONFIG

            onSuccess {
                val result = it.toPOJO<ConfigBean>()
                if (!TextUtils.isEmpty(result.data.led)) {
                    Config.getDefault().spUtils.put(
                        "ledString",
                        result.data.led
                    )
                }
            }


        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("w-", "onNewIntent:${intent?.toString()}")
        NotificationUtils.notificationVanish(this)
        intent?.let {
            dealJumpLogic(it)
        }
    }

    private fun dealJumpLogic(intent: Intent) {
        val b = intent.extras
        Log.d("w-", "dealJumpLogic:${b?.toString()}")
        val title = b?.getString("pushTitle") ?: ""
        Log.d("w-", "dealJumpLogic:${title}")
        if (title.isBlank()) return
        val content = b?.getString("pushContent") ?: ""


        NormalDialog(this)
            .apply {
                style(NormalDialog.STYLE_TWO)
                btnNum(1)
                title(title)
                content(content)
                btnText("确认")
                setOnBtnClickL(OnBtnClickL {
                    dismiss()
                })

            }.show()

    }

    /**
     * 打开消息通知权限
     */
    private fun dealNotification() {
        val notification = NotificationManagerCompat.from(this)
        val isEnabled = notification.areNotificationsEnabled()

        if (!isEnabled) {
            //未打开通知
            AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("请在“通知”中打开通知权限")
                .setNegativeButton(
                    "取消"
                ) { dialog, which -> dialog.dismiss() }
                .setPositiveButton(
                    "去设置"
                ) { dialog, which ->
                    val intent = Intent()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra(
                            "android.provider.extra.APP_PACKAGE",
                            applicationContext.packageName
                        )
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //5.0
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra("app_package", applicationContext.packageName)
                        intent.putExtra("app_uid", applicationContext.applicationInfo.uid)
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {  //4.4
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.data = Uri.parse("package:" + applicationContext.packageName)
                    } else if (Build.VERSION.SDK_INT >= 15) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                        intent.data =
                            Uri.fromParts("package", applicationContext.packageName, null)
                    }
                    startActivity(intent)

                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }


    /**
     * 播放系统提示音且震动
     */
    private fun notifyUser() {

        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(getApplicationContext(), notification)
        r.play()

        // 最后别忘了加上<uses-permission android:name="android.permission.VIBRATE"/>
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /**
         * 处理二维码扫描结果
         */
        if (requestCode == FgtHome.REQUEST_ZXING_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                val bundle = data.extras ?: return

                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    val result = bundle.getString(CodeUtils.RESULT_STRING) ?: ""
                    val prefix = bundle.getString(AtyScanQrcode.RESULT_PREFIX, "")
                    if (prefix == FgtPackageBind.PACKAGE_BIND_SCAN_PREFIX) {
                        EventBus.getDefault()
                            .post(FgtPackageBind.FgtPackageBindScanResultEvent(result))
                    } else if (prefix == AtyScanQrcode.TYPE_CHANGE) {
                        val oldContractId =
                            bundle.getString(AtyScanQrcode.RESULT_OLD_CONTRACT_ID, "")
                        if (result.isBlank()) {
                            EasyToast.DEFAULT.show("请输入要更换的设备的编号")
                        } else {
                            start(FgtChangeRentBattery.newInstance(oldContractId, result))
                        }

                    } else {
                        FgtHome.dealScanResult(result)
                    }
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    EasyToast.DEFAULT.show("未能正确识别设备码")
                }
            }
        }
    }

    companion object {


        fun checkUpgrade() {
            http {
                url = Path.CHECK_UPGRADE
                params["package_name"] = Config.getDefault().packageName
                params["os"] = "android"
                params["ver"] = Config.getDefault().versionCode.toString()

                onSuccessWithMsg { res, msg ->

                    //当errcode=200时
                    //
                    //update_force int 1强制更新0不强制更新
                    //update_msg string 更新提示文字
                    //update_url  string 更新包URL地址
                    val json = JSONObject(res)
                    val data = json.optJSONObject("data")

                    val update_force = data.optInt("update_force")
                    val update_msg = data.optString("update_msg")
                    val update_url = data.optString("update_url")


//                    val dlg = AlertDialog.Builder(getCurrentAty())
//                        .setTitle(update_msg)
//                        .setCancelable(update_force != 1)
//
//
//                    if (update_force != 1) {
//                        dlg.setNegativeButton("取消") { dialog, which -> dialog.dismiss() }
//                    }
//
//                    dlg.setPositiveButton("确定") { dialog, which ->
//
//                        openInSysBrowser(getCurrentAty(), update_url)
//
//                        AppManager.getAppManager().AppExit()
//                    }
//
//
//                    dlg.create().show()
                }
            }
        }
    }


}
