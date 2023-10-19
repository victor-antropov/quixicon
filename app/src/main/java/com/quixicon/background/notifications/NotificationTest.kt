package com.quixicon.background.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.quixicon.R
import com.quixicon.background.entities.BroadcastExtraKey
import com.quixicon.background.entities.NotificationAction
import com.quixicon.background.receivers.NotificationActionReceiver

class NotificationTest(
    val context: Context,
    val title: String,
    val message: String,
    val offset: Int,
    val size: Int,
    val isAnswer: Boolean,
    val isFinal: Boolean = false,
    val cardId: Long?
) {

    val pendingFlags =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

    private val wrongIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationAction.WRONG
            putExtra(BroadcastExtraKey.OFFSET.name, offset)
            cardId?.let {
                putExtra(BroadcastExtraKey.CARD_ID.name, it)
                putExtra(BroadcastExtraKey.IS_FINAL.name, isFinal)
            }
        },
        pendingFlags
    )

    private val correctIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationAction.CORRECT
            putExtra(BroadcastExtraKey.OFFSET.name, offset)
            cardId?.let {
                putExtra(BroadcastExtraKey.CARD_ID.name, it)
                putExtra(BroadcastExtraKey.IS_FINAL.name, isFinal)
            }
        },
        pendingFlags
    )

    private val showIntent: PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationAction.SHOW
            putExtra(BroadcastExtraKey.OFFSET.name, offset)
        },
        pendingFlags
    )

    private val btnShowVisibility = if (isAnswer) View.GONE else View.VISIBLE

    private val collapsedView =
        RemoteViews(context.packageName, R.layout.notification_test_collapsed).apply {
            setOnClickPendingIntent(R.id.btn_correct, correctIntent)
            setOnClickPendingIntent(R.id.btn_wrong, wrongIntent)
            setOnClickPendingIntent(R.id.btn_show, showIntent)
            setTextViewText(R.id.tv_title, title)
            setTextViewText(R.id.tv_message, message)
            setTextViewText(
                R.id.tv_position,
                context.getString(R.string.notification_position, offset + 1, size)
            )
            setViewVisibility(R.id.btn_show, btnShowVisibility)
        }

    private val expandedView =
        RemoteViews(context.packageName, R.layout.notification_test_expanded).apply {
            setOnClickPendingIntent(R.id.btn_correct, correctIntent)
            setOnClickPendingIntent(R.id.btn_wrong, wrongIntent)
            setOnClickPendingIntent(R.id.btn_show, showIntent)
            setTextViewText(R.id.tv_title, title)
            setTextViewText(R.id.tv_message, message)
            setTextViewText(
                R.id.tv_position,
                context.getString(R.string.notification_position, offset + 1, size)
            )
            setViewVisibility(R.id.btn_show, btnShowVisibility)
        }

    private val collapsedSimpleView =
        RemoteViews(context.packageName, R.layout.notification_test_simple_collapsed).apply {
            setOnClickPendingIntent(R.id.btn_correct, correctIntent)
            setOnClickPendingIntent(R.id.btn_wrong, wrongIntent)
            setOnClickPendingIntent(R.id.btn_show, showIntent)
            setTextViewText(R.id.tv_title, title)
            setTextViewText(R.id.tv_message, message)
            setViewVisibility(R.id.iv_show, btnShowVisibility)
        }

    private val expandedSimpleView =
        RemoteViews(context.packageName, R.layout.notification_test_simple_expanded).apply {
            setOnClickPendingIntent(R.id.btn_correct, correctIntent)
            setOnClickPendingIntent(R.id.btn_wrong, wrongIntent)
            setOnClickPendingIntent(R.id.btn_show, showIntent)
            setTextViewText(R.id.tv_title, title)
            setTextViewText(R.id.tv_message, message)
            setViewVisibility(R.id.iv_show, btnShowVisibility)
        }

    val channelId = context.getString(R.string.default_notification_channel_id)
    private val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    fun show() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        notificationManager.notify(0 /* ID of notification */, builder.build())
    }

    private fun builderCustomNotification(): NotificationCompat.Builder {
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setSmallIcon(R.drawable.ic_quixicon_logo_app_simple)
            .setContentTitle(title)
            .setContentText(message)
            .setSound(defaultSoundUri)

        if (isAnswer && isFinal) notificationBuilder.setAutoCancel(true)

        return notificationBuilder
    }

    private fun builderSimpleCustomNotification(): NotificationCompat.Builder {
        val position = context.getString(R.string.notification_position, offset + 1, size)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setCustomContentView(collapsedSimpleView)
            .setCustomBigContentView(expandedSimpleView)
            .setSmallIcon(R.drawable.ic_quixicon_logo_app_simple)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(position)
            .setShowWhen(false)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setSound(defaultSoundUri)

        if (isAnswer && isFinal) notificationBuilder.setAutoCancel(true)

        return notificationBuilder
    }

    private fun builderSimpleNotification(): NotificationCompat.Builder {
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_quixicon_logo_app_simple)
            .setContentTitle(title)
            .setContentText(message)
            .setSound(defaultSoundUri)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .addAction(
                R.drawable.ic_close_red_24dp,
                context.getString(R.string.notification_wrong),
                wrongIntent
            )
            .addAction(
                R.drawable.ic_check_green_24dp,
                context.getString(R.string.notification_correct),
                correctIntent
            )

        if (!isAnswer) {
            notificationBuilder
                .addAction(
                    R.drawable.ic_visibility_24dp,
                    context.getString(R.string.test_show_answer),
                    showIntent
                )
        } else if (isFinal) notificationBuilder.setAutoCancel(true)

        return notificationBuilder
    }
}
