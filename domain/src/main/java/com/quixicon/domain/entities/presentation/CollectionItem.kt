package com.quixicon.domain.entities.presentation

import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import org.threeten.bp.OffsetDateTime
import java.util.*

data class CollectionItem(
    override var id: Long = 0,
    override var name: String? = null,
    override var description: String? = null,
    override var codeSubject: LanguageCode? = null,
    override var codeStudent: LanguageCode? = null,
    override var imei: String? = null,
    override var parentImei: String? = null,
    override var timestamp: OffsetDateTime? = null,
    override var collectionType: CollectionType = CollectionType.DEFAULT,
    override var parentCollectionId: Long? = null,
    var qntWords: Int = 0,
    var qntPhrases: Int = 0,
    var qntRules: Int = 0,
    var superType: CardType? = null
) : Collection {

    constructor (collection: Collection) : this(
        id = collection.id,
        name = collection.name,
        description = collection.description,
        codeSubject = collection.codeSubject,
        codeStudent = collection.codeStudent,
        imei = collection.imei,
        parentImei = collection.parentImei,
        timestamp = collection.timestamp,
        collectionType = collection.collectionType,
        parentCollectionId = collection.parentCollectionId
    )
}
