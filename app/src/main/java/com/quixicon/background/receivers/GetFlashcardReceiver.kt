package com.quixicon.background.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quixicon.R
import com.quixicon.background.entities.BroadcastExtraKey
import com.quixicon.background.notifications.NotificationTest

class GetFlashcardReceiver : BroadcastReceiver() {

    lateinit var context: Context

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        val isFinal = intent.getBooleanExtra(BroadcastExtraKey.IS_FINAL.name, false)
        val name = intent.getStringExtra(BroadcastExtraKey.NAME.name) ?: ""
        val translation = intent.getStringExtra(BroadcastExtraKey.TRANSLATION.name) ?: ""
        val offset = intent.getIntExtra(BroadcastExtraKey.OFFSET.name, 0)
        val isAnswer = intent.getBooleanExtra(BroadcastExtraKey.IS_ANSWER.name, false)
        val cardId = intent.getLongExtra(BroadcastExtraKey.CARD_ID.name, 0)
        val size = intent.getIntExtra(BroadcastExtraKey.SIZE.name, 0)

        val title = name
        val message =
            if (isAnswer) translation else context.getString(R.string.notification_question)

        NotificationTest(
            context,
            title,
            message,
            offset,
            size,
            isAnswer,
            isFinal = isFinal,
            cardId
        ).apply {
            show()
        }
    }
}
