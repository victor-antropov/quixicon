package com.quixicon.background.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quixicon.background.entities.BroadcastExtraKey
import com.quixicon.background.entities.NotificationAction
import com.quixicon.background.helpers.WorkerHelper
import com.quixicon.core.metrics.Metrics
import com.quixicon.core.metrics.entities.notification.NotificationEvent

class NotificationActionReceiver : BroadcastReceiver() {

    lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        if (intent.action == NotificationAction.SHOW) {
            val extra = intent.getIntExtra(BroadcastExtraKey.OFFSET.name, 0)
            WorkerHelper.runGetFlahcardWorker(context, extra, true)
            Metrics.sendEvent(context, NotificationEvent.SHOW)
        }

        if (intent.action == NotificationAction.CORRECT || intent.action == NotificationAction.WRONG) {
            val isCorrect = intent.action == NotificationAction.CORRECT
            val cardId = intent.getLongExtra(BroadcastExtraKey.CARD_ID.name, 0)
            WorkerHelper.runUpdateKnowledgeWorker(context, cardId, isCorrect)

            val extra = intent.getIntExtra(BroadcastExtraKey.OFFSET.name, 0)
            WorkerHelper.runGetFlahcardWorker(context, extra + 1, false)

            // check for close
            val isFinal = intent.getBooleanExtra(BroadcastExtraKey.IS_FINAL.name, false)
            if (isFinal) {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(0)
            }

            if (isCorrect) {
                Metrics.sendEvent(context, NotificationEvent.CORRECT)
            } else {
                Metrics.sendEvent(context, NotificationEvent.WRONG)
            }
        }
    }
}
