package com.ruimeng.things;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 * Created by wongxd on 2019/9/23.
 */
public class NotificationUtils {

    public static void NotificationShow(Context ctx, String title, String content, PendingIntent pendingIntent) {
        //1.获取通知管理器类
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "1");
        /**
         * 兼容Android版本8.0系统
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //第三个参数表示通知的重要程度，默认则只在通知栏闪烁一下
            NotificationChannel channel = new NotificationChannel("NotificationID", "NotificationName", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);         // 开启指示灯，如果设备有的话
            channel.setLightColor(Color.RED);   // 设置指示灯颜色
            channel.setShowBadge(true);         // 检测是否显示角标
            // 注册通道
            if (null != notificationManager)
                notificationManager.createNotificationChannel(channel);
            builder.setChannelId("NotificationID");
        }
        //2.构建通知类
        builder.setSmallIcon(R.drawable.ic_launcher);//设置图标
        builder.setContentTitle(title);//标题
        builder.setContentText(content);//内容
        builder.setWhen(System.currentTimeMillis());    //时间
        builder.setContentIntent(pendingIntent);
        builder.setDefaults(Notification.DEFAULT_ALL);
        //SetDefaults 这个方法可选值如下：
        // Notification.DEFAULT_VIBRATE ：震动提示,Notification.DEFAULT_SOUND：提示音,Notification.DEFAULT_LIGHTS：三色灯,Notification.DEFAULT_ALL：以上三种全部
        //3.获取通知
        Notification notification = builder.build();
        //4.发送通知
        if (null != notificationManager)
            notificationManager.notify(1, notification);
    }

    public static void notificationVanish(Context ctx) {
        //取消通知
        NotificationManager service = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != service)
            service.cancel(1);
    }
}
