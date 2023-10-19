package com.quixicon.domain.entities.db

import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import org.threeten.bp.OffsetDateTime

interface Collection {
    var id: Long
    var collectionType: CollectionType
    var name: String?
    var description: String?
    var codeSubject: LanguageCode?
    var codeStudent: LanguageCode?
    var imei: String?
    var parentImei: String?
    var timestamp: OffsetDateTime?
    var parentCollectionId: Long?
}
