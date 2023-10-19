package com.quixicon.background.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quixicon.background.entities.NotificationAction
import com.quixicon.background.helpers.AlarmHelper
import com.quixicon.background.helpers.WorkerHelper
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.notification.NotificationEvent

class AlarmReceiver : BroadcastReceiver() {

    lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            AlarmHelper.init(context)
        }

        if (intent.action == NotificationAction.ALARM) {
            WorkerHelper.runGetFlahcardWorker(context, 0, false)
            Metrics.sendEvent(context, NotificationEvent.ALARM)
        }
    }
}
