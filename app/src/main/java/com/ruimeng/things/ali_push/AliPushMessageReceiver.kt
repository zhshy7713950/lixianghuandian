package com.ruimeng.things.ali_push
class AliPushMessageReceiver{

}
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.util.Log
//import com.alibaba.sdk.android.push.MessageReceiver
//import com.alibaba.sdk.android.push.notification.CPushMessage
//import com.ruimeng.things.AtySplash
//import com.ruimeng.things.NotificationUtils
//
//class AliPushMessageReceiver : MessageReceiver() {
//
//    public override fun onNotification(
//        context: Context?,
//        title: String?,
//        summary: String?,
//        extraMap: Map<String, String>?
//    ) {
//        Log.e(
//            "AliPushMessageReceiver",
//            "Receive notification, title: $title, summary: $summary, extraMap: $extraMap"
//        )
//    }
//
//    public override fun onMessage(context: Context?, cPushMessage: CPushMessage) {
//        Log.e(
//            "AliPushMessageReceiver",
//            "onMessage, messageId: " + cPushMessage.messageId + ", title: " + cPushMessage.title + ", content:" + cPushMessage.content
//        )
//        NotificationUtils.NotificationShow(
//            context,
//            cPushMessage.title,
//            cPushMessage.content,
//            PendingIntent.getActivity(
//                context,
//                System.currentTimeMillis().toInt(), Intent(context, AtySplash::class.java).apply {
//                    putExtras(Bundle().apply {
//                        putString("pushTitle", cPushMessage.title)
//                        putString("pushContent", cPushMessage.content)
//                        putString("pushJson", cPushMessage.traceInfo?.toString())
//                    })
//                }, PendingIntent.FLAG_ONE_SHOT
//            )
//        )
//    }
//
//    public override fun onNotificationOpened(
//        context: Context?,
//        title: String?,
//        summary: String?,
//        extraMap: String?
//    ) {
//        Log.e(
//            "AliPushMessageReceiver",
//            "onNotificationOpened, title: $title, summary: $summary, extraMap:$extraMap"
//        )
//
//        context?.startActivity(Intent(context, AtySplash::class.java).apply {
//            putExtras(Bundle().apply {
//                putString("pushTitle", title)
//                putString("pushContent", summary)
//                putString("pushJson", extraMap?.toString())
//            })
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        })
//    }
//
//    override fun onNotificationClickedWithNoAction(
//        context: Context?,
//        title: String?,
//        summary: String?,
//        extraMap: String?
//    ) {
//        Log.e(
//            "AliPushMessageReceiver",
//            "onNotificationClickedWithNoAction, title: $title, summary: $summary, extraMap:$extraMap"
//        )
//    }
//
//    override fun onNotificationReceivedInApp(
//        context: Context?,
//        title: String?,
//        summary: String?,
//        extraMap: Map<String, String>?,
//        openType: Int,
//        openActivity: String?,
//        openUrl: String?
//    ) {
//        Log.e(
//            "AliPushMessageReceiver",
//            "onNotificationReceivedInApp, title: $title, summary: $summary, extraMap:$extraMap, openType:$openType, openActivity:$openActivity, openUrl:$openUrl"
//        )
//    }
//
//    override fun onNotificationRemoved(context: Context?, messageId: String?) {
//        Log.e("AliPushMessageReceiver", "onNotificationRemoved")
//    }
//
//    companion object {
//        // 消息接收部分的LOG_TAG
//        val REC_TAG = "receiver"
//    }
//}