package com.quixicon.background.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.quixicon.R
import com.quixicon.background.entities.NotificationAction
import com.quixicon.background.receivers.AlarmReceiver
import timber.log.Timber
import java.util.*

object AlarmHelper {
    fun init(context: Context) {
        // Get AlarmManager instance
        val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Intent part
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = NotificationAction.ALARM

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, pendingFlags)

        // Alarm time
        val hours = context.resources.getInteger(R.integer.notification_delay_in_hours)
        Timber.e("Start Alarm for $hours hours")

        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, hours)
        }
        val ALARM_DELAY = AlarmManager.INTERVAL_HOUR * hours
        // Set with system Alarm Service
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, ALARM_DELAY, pendingIntent)
    }
}
