package com.quixicon.domain.entities.cache

import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.enums.NotificationSource
import com.quixicon.domain.entities.enums.TestDirection

data class QuixiconConfig(
    var cardSortOrder: CardSortOrder = CardSortOrder.AZ,
    var collectionSortOrder: CollectionSortOrder = CollectionSortOrder.RECENT,
    var swipeMode: Boolean = false,
    var copyCollectionId: Long? = null,
    var useDarkTheme: Boolean = true,

    var languageStudent: LanguageCode = LanguageCode.RU,
    var languageInterface: LanguageCode = LanguageCode.EN,
    var languageSubject: LanguageCode = LanguageCode.UNDEFINED,

    var testDirection: TestDirection = TestDirection.DIRECT,
    var testVerticalSwipe: Boolean = false,
    var testNotShuffle: Boolean = false,
    var testShowKnown: Boolean = false,
    var testCollectionId: Long? = null,
    var testShowTranscription: Boolean = false,
    var testPlayQuestions: Boolean = false,
    var isInstalled: Boolean = false,

    var filterWords: Boolean = true,
    var filterPhrases: Boolean = false,
    var filterRules: Boolean = false,

    var hintCollections: Boolean = true,
    var hintCards: Boolean = true,
    var hintSwipe: Boolean = true,

    var introTest: Boolean = true,

    var useNotifications: Boolean = true,
    var notificationSource: NotificationSource = NotificationSource.RECENT,
    var notificationTestCollectionId: Long? = null,

    var useFilter: Boolean = false,

    var testCounter: Int = 0,
    var isReviewConfirmed: Boolean = false,

    var hintSocial: Boolean = true,
    var isDrawAnswers: Boolean = false,
    var showGlobalCollections: Boolean = true
)
