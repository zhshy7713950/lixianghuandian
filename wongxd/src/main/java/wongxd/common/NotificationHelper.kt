package wongxd.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat


/**
 * Created by wongxd on 2018/11/20.
 * https://github.com/wongxd
 * wxd1@live.com
 */
object NotificationHelper {


    /*
    *
    *   PendingIntent 主要可以通过以下三种方式获取：
    *   //获取一个用于启动 Activity 的 PendingIntent 对象
    *   public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags);
    *
    *   //获取一个用于启动 Service 的 PendingIntent 对象
    *   public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags);
    *
    *   //获取一个用于向 BroadcastReceiver 广播的 PendingIntent 对象
    *   public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags)
    *
    *   FLAG_CANCEL_CURRENT:如果当前系统中已经存在一个相同的 PendingIntent 对象，那么就将先将已有的 PendingIntent 取消，然后重新生成一个 PendingIntent 对象。
    *   FLAG_NO_CREATE:如果当前系统中不存在相同的 PendingIntent 对象，系统将不会创建该 PendingIntent 对象而是直接返回 null 。
    *   FLAG_ONE_SHOT:该 PendingIntent 只作用一次。
    *   FLAG_UPDATE_CURRENT:如果系统中已存在该 PendingIntent 对象，那么系统将保留该 PendingIntent 对象，但是会使用新的 Intent 来更新之前 PendingIntent 中的 Intent 对象数据，例如更新 Intent 中的 Extras 。
    *
    * */

    val mContext: Context by lazy { getCurrentAty() }

    //获取NotificationManager实例
    val mNotifyManager: NotificationManager by lazy { mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }


    data class NotificationInstanceBean(val channelId: Int, val channelTag: String)


    /**
     * 发送一条notification
     *
     * @param channelId
     * @param channelTag
     * @param title
     * @param content
     * @param pendingIntent 给 Notification 设置 Action
     */
    fun show(
        channelId: Int,
        channelTag: String, icon: Int, pendingIntent: PendingIntent? = null, title: String,
        content: String
    ): NotificationInstanceBean {
        return sendNotification(
            channelId = channelId,
            channelTag = channelTag,
            smallIcon = icon,
            pendingIntent = pendingIntent,
            title = title,
            content = content
        )

    }

    /**
     * 发送一条notification
     *
     * @param channelId
     * @param channelTag
     * @param largeIcon
     * @param smallIcon
     * @param title
     * @param content
     * @param isAutoCancel  点击通知后自动清除
     * @param sendWhen      设置通知时间，默认为系统发出通知的时间，通常不用设置
     * @param pendingIntent 给 Notification 设置 Action
     * @param onGoing       表示该通知通知放置在正在运行,不能被手动清除,但能通过 cancel() 方法清除
     */
    fun sendNotification(
        channelId: Int,
        channelTag: String,
        largeIcon: Bitmap? = null,
        smallIcon: Int,
        title: String,
        content: String,
        isAutoCancel: Boolean = true,
        sendWhen: Long? = null,
        pendingIntent: PendingIntent? = null,
        onGoing: Boolean = false
    ): NotificationInstanceBean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //on oreo and newer, create a notification channel
            val channel =
                NotificationChannel(channelTag, channelTag, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            channel.enableLights(true)
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            mNotifyManager.createNotificationChannel(channel)
        }

        //实例化NotificationCompat.Builde并设置相关属性
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(mContext, channelTag)
                .setShowWhen(false)
                .setOngoing(true)


        largeIcon?.let {
            builder.setLargeIcon(largeIcon)
        }

        //设置小图标
        builder.setSmallIcon(smallIcon)
        //设置通知标题
        builder.setContentTitle(title)
        //设置通知内容
        builder.setContentText(content)
        //点击通知后自动清除
        builder.setAutoCancel(isAutoCancel)
        //设置通知时间，默认为系统发出通知的时间，通常不用设置
        sendWhen?.let {
            builder.setWhen(it)
        }

        //通过builder.build()方法生成Notification对象,并发送通知,id=channelId
        pendingIntent?.let {
            builder.setContentIntent(pendingIntent)
        }
        //调用系统默认响铃,设置此属性后setSound()会无效
        builder.setDefaults(Notification.DEFAULT_SOUND)
        //用系统自带的铃声效果
        //.setSound(Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6"))
        //调用自己提供的铃声，位于 /res/values/raw 目录下
        //.setSound(Uri.parse("android.resource://com.littlejie.notification/" + R.raw.sound))


        //设置 Notification 的 flags = FLAG_NO_CLEAR
        //FLAG_ONGOING_EVENT 表示该通知通知放置在正在运行,不能被手动清除,但能通过 cancel() 方法清除
        //等价于 builder.setOngoing(true);
        //notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        builder.setOngoing(onGoing)


        //展示有震动效果的通知,需要在AndroidManifest.xml中申请震动权限
        //<uses-permission android:name="android.permission.VIBRATE" />
        //补充:测试震动的时候,手机的模式一定要调成铃声+震动模式,否则你是感受不到震动的
        //震动也有两种设置方法,与设置铃声一样,在此不再赘述

        //使用系统默认的震动参数,会与自定义的冲突
        //.setDefaults(Notification.DEFAULT_VIBRATE)
        //自定义震动效果
        //val vibrate = longArrayOf(0, 500, 1000, 1500)
        //.setVibrate(vibrate)
        builder.setDefaults(Notification.DEFAULT_VIBRATE)


        //ledARGB 表示灯光颜色、 ledOnMS 亮持续时间、ledOffMS 暗的时间
        builder.setLights(0xFF0000, 3000, 3000)


        mNotifyManager.notify(channelId, builder.build())

        return NotificationInstanceBean(channelId, channelTag)
    }


    fun cancelNotification(notificationInstanceBean: NotificationInstanceBean) {
        mNotifyManager.cancel(
            notificationInstanceBean.channelTag,
            notificationInstanceBean.channelId
        )
    }


    fun cancelAll() {
        mNotifyManager.cancelAll()
    }
}