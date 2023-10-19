package com.quixicon.background.workers

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quixicon.background.entities.BroadcastExtraKey
import com.quixicon.domain.boundaries.DatabaseBoundary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateKnowledgeWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    @Inject
    lateinit var databaseBoundary: DatabaseBoundary

    @SuppressLint("CheckResult")
    override suspend fun doWork(): Result {
        val cardId = inputData.getLong(BroadcastExtraKey.CARD_ID.name, 0)
        val isKnowledgeUp = inputData.getBoolean(BroadcastExtraKey.KNOWLEDGE_UP.name, false)

        return withContext(Dispatchers.IO) {
            if (cardId > 0) {
                val card = databaseBoundary.getCard(cardId).blockingGet()
                var value = card.knowledge ?: 0
                if (isKnowledgeUp) value += 10 else value -= 20
                if (value > 100) value = 100
                if (value < 0) value = 0
                databaseBoundary.updateCardKnowledge(cardId, value).blockingGet()
            }
            return@withContext Result.success()
        }
    }
}
