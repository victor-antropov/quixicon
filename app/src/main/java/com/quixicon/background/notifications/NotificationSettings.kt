package com.quixicon.background.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.quixicon.R
import com.quixicon.presentation.activities.settings.views.SettingsActivity

class NotificationSettings(
    val context: Context,
    val title: String,
    val message: String
) {
    val channelId = context.getString(R.string.default_notification_channel_id)
    private val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val appIntent: PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        },
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )

    private val collapsedSimpleView = RemoteViews(context.packageName, R.layout.notification_info_simple)

    private val collapsedView = RemoteViews(context.packageName, R.layout.notification_info)

    private fun builderCustomNotification(): NotificationCompat.Builder {
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(collapsedView)
            .setSmallIcon(R.drawable.ic_quixicon_logo_app_simple)
            .setContentTitle(title)
            .setContentText(message)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setContentIntent(appIntent)

        return notificationBuilder
    }

    private fun builderSimpleCustomNotification(): NotificationCompat.Builder {
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setCustomContentView(collapsedSimpleView)
            .setCustomBigContentView(collapsedSimpleView)
            .setSmallIcon(R.drawable.ic_quixicon_logo_app_simple)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(context.getString(R.string.notification_info_about))
            .setShowWhen(false)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSound(defaultSoundUri)
            .setAutoCancel(true)
            .setContentIntent(appIntent)

        return notificationBuilder
    }

    fun show() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.setSound(null, null)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builderSimpleCustomNotification()
        } else {
            builderCustomNotification()
        }

        notificationManager.notify(1 /* ID of notification */, builder.build())
    }
}
