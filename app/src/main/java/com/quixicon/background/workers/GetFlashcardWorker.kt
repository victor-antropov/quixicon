package com.quixicon.background.workers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quixicon.R
import com.quixicon.background.entities.BroadcastExtraKey
import com.quixicon.background.notifications.NotificationSettings
import com.quixicon.background.receivers.GetFlashcardReceiver
import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.enums.NotificationSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import javax.inject.Inject

class GetFlashcardWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    @Inject
    lateinit var databaseBoundary: DatabaseBoundary

    @Inject
    lateinit var preferencesBoundary: PreferencesBoundary

    @SuppressLint("CheckResult")
    override suspend fun doWork(): Result {
        val offset = inputData.getInt(BroadcastExtraKey.OFFSET.name, 0)
        val isAnswer = inputData.getBoolean(BroadcastExtraKey.IS_ANSWER.name, false)
        val isCheckDate = inputData.getBoolean(BroadcastExtraKey.CHECK_DATE.name, false)

        return withContext(Dispatchers.IO) {
            val config = preferencesBoundary.getConfig()

            var isActive = config.useNotifications
            val source = config.notificationSource

            val collectionId = if (source == NotificationSource.RECENT) {
                config.testCollectionId ?: 0
            } else {
                config.notificationTestCollectionId ?: 0
            }

            // Check recent collection date:
            val recentCollection = databaseBoundary.getRecentCollection().blockingGet()
            val currentTime = OffsetDateTime.now(ZoneOffset.UTC)

            recentCollection.timestamp?.let {
                val hours = applicationContext.resources.getInteger(R.integer.notification_delay_in_hours)
                if (isCheckDate && offset == 0 && !isAnswer && it.plusHours(hours.toLong()) > currentTime) isActive = false
            }

            if (isActive && collectionId > 0) {
                val cards = databaseBoundary.selectCardsForShortTest(collectionId, 0, 5).blockingGet()
                var card: Card? = null
                var isFinal = false
                val limit = 10

                if (offset < cards.size && offset < limit) {
                    card = cards[offset]

                    if (offset == 1 && preferencesBoundary.getNotificationHint()) {
                        preferencesBoundary.setNotificationHint(false)
                        NotificationSettings(applicationContext, applicationContext.getString(R.string.notification_info_title), applicationContext.getString(R.string.notification_info_message)).apply {
                            show()
                        }
                    }
                }

                if (offset >= limit - 1 || offset == cards.size - 1) isFinal = true

                if (card != null) {
                    val intent = Intent(applicationContext, GetFlashcardReceiver::class.java).apply {
                        putExtra(BroadcastExtraKey.NAME.name, card.name)
                        putExtra(BroadcastExtraKey.TRANSLATION.name, card.translation)
                        putExtra(BroadcastExtraKey.OFFSET.name, offset)
                        putExtra(BroadcastExtraKey.IS_ANSWER.name, isAnswer)
                        putExtra(BroadcastExtraKey.IS_FINAL.name, isFinal)
                        putExtra(BroadcastExtraKey.CARD_ID.name, card.id)
                        putExtra(BroadcastExtraKey.SIZE.name, cards.size)
                    }
                    applicationContext.sendBroadcast(intent)
                }
            }

            return@withContext Result.success()
        }
    }
}
