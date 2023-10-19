package com.quixicon.data.repositories

import android.app.Application
import android.net.Uri
import com.quixicon.data.repositories.base.BaseRepository
import com.quixicon.domain.boundaries.FilesBoundary
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.remotes.CardData
import com.quixicon.domain.entities.remotes.CollectionData
import io.reactivex.Single
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

class FilesRepository(private val app: Application) : BaseRepository(), FilesBoundary {

    override fun readCollection(uri: Uri): Single<CollectionData> {
        return Single.fromCallable {
            try {
                val cards = mutableListOf<CardData>()
                app.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            Timber.e("Line: $line")
                            strToRemoteCardData(line)?.run {
                                cards.add(this)
                            }
                            line = reader.readLine()
                        }
                    }
                }

                CollectionData(
                    name = "",
                    cards = cards,
                    collectionType = CollectionType.USER
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.e("Exception: $e")
                null
            }
        }
    }

    private fun strToRemoteCardData(input: String): CardData? {
        val parts = input.split("\t")
        // name - translation - transcription - description - type
        if (parts.size > 1) { // Name, Translation
            val type = parts.getOrNull(4)?.toByteOrNull().let { code ->
                enumValues<CardType>().firstOrNull { it.value == code }
            }
            return CardData(
                name = parts[0],
                translation = parts[1],
                transcription = parts.getOrNull(2),
                description = parts.getOrNull(3),
                cardType = type
            )
        } else {
            return null
        }
    }
}
