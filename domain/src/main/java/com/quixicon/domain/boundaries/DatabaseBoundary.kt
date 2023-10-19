package com.quixicon.domain.boundaries

import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionData
import io.reactivex.Single
import java.util.*

/**
 * Interface for working with a local database.
 */
interface DatabaseBoundary {

    fun insertCollectionData(collection: CollectionData): Single<Long>

    fun insertCollectionDataSimple(collection: CollectionData): Single<Long>

    fun selectCollections(order: CollectionSortOrder, subjectFilter: LanguageCode? = null, studentFilter: LanguageCode? = null): Single<List<Collection>>

    fun selectCollectionsWithCards(subjectFilter: LanguageCode? = null, studentFilter: LanguageCode? = null): Single<List<Pair<Collection, Int>>>

    fun selectOtherCollections(id: Long, subjectFilter: LanguageCode? = null, studentFilter: LanguageCode? = null): Single<List<Collection>>

    fun getRecentCollection(): Single<Collection>

    fun selectCardsByCollectionId(id: Long, order: CardSortOrder, subjectFilter: LanguageCode? = null): Single<List<Card>>

    fun selectCardsByType(type: CardType, order: CardSortOrder, subjectFilter: LanguageCode? = null): Single<List<Card>>

    fun selectAllCards(order: CardSortOrder, subjectFilter: LanguageCode? = null, studentFilter: LanguageCode? = null): Single<List<Card>>

    fun selectCardsForTest(collectionId: Long, showAll: Boolean, offset: Int? = null, limit: Int? = null): Single<List<Card>>

    fun selectCardsForShortTest(collectionId: Long, offset: Int, limit: Int): Single<List<Card>>

    fun clearDb(): Single<Unit>

    fun updateCardKnowledge(id: Long, value: Int): Single<Int>

    fun updateCardDate(id: Long): Single<Int>

    fun copyCard(cardId: Long, collectionId: Long): Single<Long>

    fun deleteCard(cardId: Long, collectionId: Long? = null): Single<Unit>

    fun getCard(cardId: Long): Single<Card>

    fun updateCard(card: Card): Single<Unit>

    fun insertCard(card: Card, collectionId: Long): Single<Unit>

    fun selectLinkedCollections(cardId: Long): Single<List<Collection>>

    fun selectCollectionsWithSizeInfo(order: CollectionSortOrder, subjectFilter: LanguageCode? = null, studentFilter: LanguageCode? = null): Single<List<Pair<Collection, EnumMap<CardType, Int>>>>

    fun selectSuperTypeSizes(subjectFilter: LanguageCode? = null, studentFilter: LanguageCode? = null): Single<EnumMap<CardType, Int>>

    fun getCollection(collectionId: Long): Single<Collection>

    fun updateCollection(collectionId: Long, name: String?, description: String? = null, subject: LanguageCode? = null): Single<Unit>

    fun updateCollectionDate(collectionId: Long): Single<Unit>

    fun insertCollection(name: String?, description: String?, subject: LanguageCode? = null): Single<Unit>

    fun deleteCollection(collectionId: Long): Single<Unit>
}
