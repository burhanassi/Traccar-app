package com.logestechs.driver.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.logestechs.driver.R
import com.logestechs.driver.utils.AppConstants
import com.logestechs.driver.utils.IntentExtrasKeys


class LogesTechsFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data
            var title: String? = ""
            var body: String? = ""
            var barcode: String? = ""
            if (data.containsKey("title")) {
                title = data["title"]
            }
            if (data.containsKey("body")) {
                body = data["body"]
            }

            if (data.containsKey("body")) {
                body = data["body"]
            }

            if (data.containsKey("packageBarcode")) {
                barcode = data["packageBarcode"]
            }

            sendNotification(
                title,
                body,
                barcode
            )
        }
    }

    // [END receive_message]
    private fun sendNotification(
        messageTitle: String?,
        messageBody: String?,
        packageBarcode: String?
    ) {

        val broadcastIntent = Intent()
        broadcastIntent.action = AppConstants.BROADCAST_CREDENTIAL
        broadcastIntent.putExtra(IntentExtrasKeys.EXTRA_RECEIVED_NOTIFICATION.name, 1)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                getString(R.string.NOTIFICATION_CHANNEL_ID),
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(mChannel)
        }

//        var intent: Intent? = null
//
//        intent = Intent(this, DashboardActivity::class.java)
//        intent.putExtra(BundleKeys.NOTIFICATION_TRACK_PKG_BARCODE_KEY.name, packageBarcode)
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, getString(R.string.NOTIFICATION_CHANNEL_ID))
                .setSmallIcon(R.drawable.ic_customer_logo)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.ic_customer_logo);
        } else {
            notificationBuilder.setSmallIcon(R.drawable.ic_customer_logo);
        }
        notificationManager.notify(
            System.currentTimeMillis().toInt() /* ID of notification */,
            notificationBuilder.build()
        )
    }

    override fun onNewToken(token: String) {
//        Log.d(TAG, "FirebaseMessagingService:onNewToken: " + token);

//        TODO Comment
//         More Info : Follow This :
//        https://firebase.google.com/docs/cloud-messaging/android/client
    }
}