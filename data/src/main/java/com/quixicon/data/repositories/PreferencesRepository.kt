package com.quixicon.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.quixicon.data.preferences.entities.PrefKeys
import com.quixicon.data.repositories.base.BaseRepository
import com.quixicon.domain.boundaries.PreferencesBoundary
import com.quixicon.domain.entities.cache.QuixiconConfig
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.enums.NotificationSource
import com.quixicon.domain.entities.enums.TestDirection

class PreferencesRepository(private val context: Context) : BaseRepository(), PreferencesBoundary {

    override fun getConfig(): QuixiconConfig {
        return with(PreferenceManager.getDefaultSharedPreferences(context)) {
            QuixiconConfig(
                cardSortOrder = getEnum(PrefKeys.CARD_SORT_ORDER.name, CardSortOrder.AZ),
                collectionSortOrder = getEnum(
                    PrefKeys.COLLECTION_SORT_ORDER.name,
                    CollectionSortOrder.RECENT
                ),
                swipeMode = getBoolean(PrefKeys.SWIPE_MODE.name, false),
                useDarkTheme = getBoolean(PrefKeys.USE_DARK_THEME.name, true),
                copyCollectionId = getLong(PrefKeys.COPY_COLLECTION_ID.name, 0),
                testCollectionId = getLong(PrefKeys.TEST_COLLECTION_ID.name, 0),
                testVerticalSwipe = getBoolean(PrefKeys.TEST_VERTICAL_SWIPE.name, false),
                testNotShuffle = getBoolean(PrefKeys.TEST_NOT_SHUFFLE.name, false),
                testShowKnown = getBoolean(PrefKeys.TEST_SHOW_KNOWN.name, false),
                testShowTranscription = getBoolean(PrefKeys.TEST_SHOW_TRANSCRIPTION.name, false),
                testDirection = getEnum(PrefKeys.TEST_DIRECTION.name, TestDirection.DIRECT),
                languageStudent = getEnum(PrefKeys.LANGUAGE_STUDENT.name, LanguageCode.RU),
                languageInterface = getEnum(PrefKeys.LANGUAGE_INTERFACE.name, LanguageCode.EN),
                isInstalled = getBoolean(PrefKeys.IS_INSTALLED.name, false),
                hintCollections = getBoolean(PrefKeys.HINT_COLLECTIONS.name, true),
                hintCards = getBoolean(PrefKeys.HINT_CARDS.name, true),
                hintSocial = getBoolean(PrefKeys.HINT_SOCIAL.name, true),
                hintSwipe = getBoolean(PrefKeys.HINT_SWIPE.name, true),
                introTest = getBoolean(PrefKeys.INTRO_TEST.name, true),
                useNotifications = getBoolean(PrefKeys.USE_NOTIFICATIONS.name, true),
                notificationSource = getEnum(PrefKeys.NOTIFICATIONS_SOURCE.name, NotificationSource.RECENT),
                notificationTestCollectionId = getLong(PrefKeys.NOTIFICATION_COLLECTION_ID.name, 0),
                testPlayQuestions = getBoolean(PrefKeys.TEST_PLAY_QUESTION.name, false),
                isReviewConfirmed = getBoolean(PrefKeys.IS_REVIEW_CONFIRMED.name, false),
                testCounter = getInt(PrefKeys.TEST_COUNTER.name, 0),
                isDrawAnswers = getBoolean(PrefKeys.IS_DRAW_ANSWERS.name, false),
                showGlobalCollections = getBoolean(PrefKeys.SHOW_GLOBAL_COLLECTIONS.name, true),
                useFilter = getBoolean(PrefKeys.USE_FILTER.name, false),
                languageSubject = getEnum(PrefKeys.LANGUAGE_SUBJECT.name, LanguageCode.UNDEFINED)
            )
        }
    }

    override fun saveConfig(config: QuixiconConfig) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putEnum(PrefKeys.CARD_SORT_ORDER.name, config.cardSortOrder)
            putEnum(PrefKeys.COLLECTION_SORT_ORDER.name, config.collectionSortOrder)
            putBoolean(PrefKeys.SWIPE_MODE.name, config.swipeMode)
            putLong(PrefKeys.COPY_COLLECTION_ID.name, config.copyCollectionId ?: 0)
            putBoolean(PrefKeys.USE_DARK_THEME.name, config.useDarkTheme)
            putLong(PrefKeys.TEST_COLLECTION_ID.name, config.testCollectionId ?: 0)
            putBoolean(PrefKeys.TEST_VERTICAL_SWIPE.name, config.testVerticalSwipe)
            putBoolean(PrefKeys.TEST_NOT_SHUFFLE.name, config.testNotShuffle)
            putBoolean(PrefKeys.TEST_SHOW_KNOWN.name, config.testShowKnown)
            putBoolean(PrefKeys.TEST_SHOW_TRANSCRIPTION.name, config.testShowTranscription)
            putEnum(PrefKeys.TEST_DIRECTION.name, config.testDirection)
            putEnum(PrefKeys.LANGUAGE_STUDENT.name, config.languageStudent)
            putEnum(PrefKeys.LANGUAGE_INTERFACE.name, config.languageInterface)
            putBoolean(PrefKeys.IS_INSTALLED.name, config.isInstalled)
            putBoolean(PrefKeys.HINT_COLLECTIONS.name, config.hintCollections)
            putBoolean(PrefKeys.HINT_CARDS.name, config.hintCards)
            putBoolean(PrefKeys.HINT_SOCIAL.name, config.hintSocial)
            putBoolean(PrefKeys.HINT_SWIPE.name, config.hintSwipe)
            putBoolean(PrefKeys.INTRO_TEST.name, config.introTest)
            putBoolean(PrefKeys.USE_NOTIFICATIONS.name, config.useNotifications)
            putEnum(PrefKeys.NOTIFICATIONS_SOURCE.name, config.notificationSource)
            putLong(PrefKeys.NOTIFICATION_COLLECTION_ID.name, config.notificationTestCollectionId ?: 0)
            putBoolean(PrefKeys.TEST_PLAY_QUESTION.name, config.testPlayQuestions)
            putBoolean(PrefKeys.IS_REVIEW_CONFIRMED.name, config.isReviewConfirmed)
            putInt(PrefKeys.TEST_COUNTER.name, config.testCounter)
            putBoolean(PrefKeys.IS_DRAW_ANSWERS.name, config.isDrawAnswers)
            putBoolean(PrefKeys.SHOW_GLOBAL_COLLECTIONS.name, config.showGlobalCollections)
            putBoolean(PrefKeys.USE_FILTER.name, config.useFilter)
            putEnum(PrefKeys.LANGUAGE_SUBJECT.name, config.languageSubject)
            apply()
        }
    }

    override fun getNotificationHint(): Boolean {
        return with(PreferenceManager.getDefaultSharedPreferences(context)) {
            getBoolean(PrefKeys.HINT_NOTIFICATION.name, true)
        }
    }

    override fun setNotificationHint(value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putBoolean(PrefKeys.HINT_NOTIFICATION.name, value)
            apply()
        }
    }

    fun <T : Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: T?): SharedPreferences.Editor =
        this.putString(key, value?.name ?: "")

    inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, default: T) =
        this.getString(key, "").let { value ->
            if (!value.isNullOrEmpty()) enumValues<T>().firstOrNull { it.name == value } ?: default else default
        }
}
