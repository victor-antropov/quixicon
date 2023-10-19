package com.quixicon.background.helpers

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.quixicon.background.entities.BroadcastExtraKey
import com.quixicon.background.workers.GetFlashcardWorker
import com.quixicon.background.workers.UpdateKnowledgeWorker
import timber.log.Timber

object WorkerHelper {

    fun runGetFlahcardWorker(context: Context, offset: Int, isAnswer: Boolean, isCheckDate: Boolean = false) {
        /*
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        */

        Timber.e("Start Worker with Offset $offset and answer $isAnswer")

        val data: Data = workDataOf(
            BroadcastExtraKey.OFFSET.name to offset,
            BroadcastExtraKey.IS_ANSWER.name to isAnswer,
            BroadcastExtraKey.CHECK_DATE.name to isCheckDate
        )

        val request =
            OneTimeWorkRequestBuilder<GetFlashcardWorker>()
                // .setConstraints(constraints)
                .setInputData(data)
                .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun runUpdateKnowledgeWorker(context: Context, cardId: Long, isKnowledgeUp: Boolean) {
        Timber.e("Start Knowlege Worker with for cardId $cardId and up direction: $isKnowledgeUp")

        val data: Data = workDataOf(
            BroadcastExtraKey.CARD_ID.name to cardId,
            BroadcastExtraKey.KNOWLEDGE_UP.name to isKnowledgeUp
        )

        val request =
            OneTimeWorkRequestBuilder<UpdateKnowledgeWorker>()
                .setInputData(data)
                .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
