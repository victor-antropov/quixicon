package com.quixicon.data.repositories

import android.annotation.SuppressLint
import android.util.LongSparseArray
import androidx.core.util.containsKey
import com.quixicon.data.db.AppDatabase
import com.quixicon.data.db.entities.CardEntity
import com.quixicon.data.db.entities.CollectionEntity
import com.quixicon.data.db.entities.LinkEntity
import com.quixicon.domain.boundaries.DatabaseBoundary
import com.quixicon.domain.entities.db.Card
import com.quixicon.domain.entities.db.CardExtraData
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.db.CollectionSize
import com.quixicon.domain.entities.enums.CardSortOrder
import com.quixicon.domain.entities.enums.CardType
import com.quixicon.domain.entities.enums.CollectionSortOrder
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.entities.remotes.CollectionData
import io.reactivex.Single
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("CheckResult")
class DatabaseRepository : DatabaseBoundary {

    override fun insertCollectionData(collection: CollectionData): Single<Long> {
        val database = AppDatabase.getInstance()

        var parentCollectionId: Long?
        val relativeCollectionsIds = mutableListOf<Long>()

        val codeSubject = enumValues<LanguageCode>().firstOrNull { it.value == collection.codeSubject } ?: LanguageCode.UNDEFINED
        val codeStudent = enumValues<LanguageCode>().firstOrNull { it.value == collection.codeStudent } ?: LanguageCode.UNDEFINED

        val parentImei: String? = collection.imei?.split(".").let {
            if (it != null && it.size > 1) {
                it[0]
            } else {
                null
            }
        }

        val idMap = LongSparseArray<Long>() // uid -> CardEntity.id

        return Single.fromCallable {
            // Abort if collection with IMEI exists
            collection.imei?.let {
                Timber.e("Checking IMEI $it ...")
                database.collectionDao().checkImei(it).blockingGet()?.run {
                    Timber.e("Abort installation the collection with imei $it")
                    error("Abort installation the collection with imei $it")
                }
            }

            // Check PARENT ID
            parentImei?.let {
                Timber.e("Checking PARENT_ID $it ...")
                parentCollectionId = database.collectionDao().checkImei(it).blockingGet()

                if (parentCollectionId != null) {
                    Timber.e("Parent DETECTED!")
                    relativeCollectionsIds.add(parentCollectionId!!)
                } else {
                    Timber.e("Parent NOT detected. Looking for other relatives...")
                    val relatives = database.collectionDao().getByParentImei(parentImei).blockingGet().map { entity ->
                        entity.id
                    }
                    relativeCollectionsIds.addAll(relatives)
                }

                Timber.e("Relatives ids: $relativeCollectionsIds")

                relativeCollectionsIds.forEach { relativeCollectionId ->
                    Timber.e("Select cards from $relativeCollectionId")

                    database.cardDao().selectByCollectionOrderByDefault(relativeCollectionId).blockingGet().forEach { card ->
                        card.uid?.let { uid ->
                            if (uid > 0) idMap.append(uid, card.id)
                        }
                    }
                }

                Timber.e("idMap Ready: ${idMap.size()}")
            }

            // ADD COLLECTION ENTITY
            val collectionEntity = CollectionEntity(
                name = collection.name,
                description = collection.description,
                codeSubject = codeSubject,
                codeStudent = codeStudent,
                imei = collection.imei,
                collectionType = collection.collectionType,
                parentImei = parentImei,
                timestamp = OffsetDateTime.now(ZoneOffset.UTC),
            )
            val collectionId = database.collectionDao().insertReplace(collectionEntity).blockingGet()

            // ADD CARDS
            val cardsForInsert = mutableListOf<CardEntity>()
            val idsForLink = mutableListOf<Long>()

            Timber.e("IDMap size ${idMap.size()}")

            collection.cards?.forEach {
                // uid не указано - добавляем карточку целиком
                if (it.uid == null) {
                    cardsForInsert.add(
                        CardEntity(
                            name = it.name,
                            transcription = it.transcription,
                            translation = it.translation,
                            extraData = CardExtraData(it.description),
                            cardType = it.cardType ?: CardType.WORD,
                            uid = it.uid,
                            subject = codeSubject,
                        ),
                    )
                }
                // uid указано, но в родственных коллекциях карточки нет (например первая установка)
                else if (!idMap.containsKey(it.uid!!)) {
                    cardsForInsert.add(
                        CardEntity(
                            name = it.name,
                            transcription = it.transcription,
                            translation = it.translation,
                            extraData = CardExtraData(it.description),
                            cardType = it.cardType ?: CardType.WORD,
                            uid = it.uid,
                            subject = codeSubject,
                        ),
                    )
                }
                // uid указано, в родственных коллекция присутствует
                else {
                    it.uid?.let { uid -> idsForLink.add(idMap.get(uid)) }
                }
            }

            Timber.e("Full cards: ${cardsForInsert.size}")
            Timber.e("Links Only: ${idsForLink.size}")

            if (cardsForInsert.isNotEmpty()) {
                idsForLink.addAll(database.cardDao().insertReplace(cardsForInsert).blockingGet())
            }

            // ADD LINKS
            database.linkDao().insertReplace(
                idsForLink.map {
                    LinkEntity(
                        cardId = it,
                        collectionId = collectionId,
                    )
                },
            ).blockingGet()

            // idsForLink.size
            collectionId
        }
    }

    override fun insertCollectionDataSimple(collection: CollectionData): Single<Long> {
        val database = AppDatabase.getInstance()

        val imei: String = collection.imei ?: "undefined"

        val codeSubject = enumValues<LanguageCode>().firstOrNull { it.value == collection.codeSubject } ?: LanguageCode.UNDEFINED
        val codeStudent = enumValues<LanguageCode>().firstOrNull { it.value == collection.codeStudent } ?: LanguageCode.UNDEFINED

        val result: Single<Long> = database.collectionDao().checkImei(imei).toSingle(0).flatMap {
            if (it != 0L) {
                return@flatMap Single.just(0)
            } else {
                val parentImei: String? = imei.split("\\.").let { parts ->
                    if (parts.size > 1) {
                        parts[0]
                    } else {
                        null
                    }
                }

                val collectionEntity = CollectionEntity(
                    name = collection.name,
                    description = collection.description,
                    codeSubject = codeSubject,
                    codeStudent = codeStudent,
                    imei = collection.imei,
                    collectionType = collection.collectionType,
                    parentImei = parentImei,
                    timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                )

                return@flatMap database.collectionDao().insertReplace(collectionEntity)
            }
        }.doOnError {
            Timber.e("It was an error: ${it.message}")
        }.flatMap { collectionId ->
            if (collectionId == 0L) return@flatMap Single.just(0)
            Timber.e("Adding cards")
            val cards = collection.cards?.map {
                CardEntity(
                    name = it.name,
                    transcription = it.transcription,
                    translation = it.translation,
                    extraData = CardExtraData(it.description),
                    cardType = it.cardType ?: CardType.WORD,
                    uid = it.uid,
                    subject = codeSubject,
                )
            }
            if (cards.isNullOrEmpty()) {
                return@flatMap Single.just(0)
            } else {
                return@flatMap database.cardDao().insertReplace(cards).map {
                    it.map { cardId ->
                        LinkEntity(
                            cardId = cardId,
                            collectionId = collectionId,
                        )
                    }
                }.flatMap {
                    database.linkDao().insertReplace(it)
                }.map {
                    collectionId
                }
            }
        }

        return result
    }

    override fun selectCollections(order: CollectionSortOrder, subjectFilter: LanguageCode?, studentFilter: LanguageCode?): Single<List<Collection>> {
        val dao = AppDatabase.getInstance().collectionDao()
        return when (order) {
            CollectionSortOrder.RECENT -> dao.getAllOrderByDate()
            CollectionSortOrder.AZ -> dao.getAllOrderByName()
        }.map { ArrayList<Collection>(it) }
    }

    override fun selectCollectionsWithSizeInfo(order: CollectionSortOrder, subjectFilter: LanguageCode?, studentFilter: LanguageCode?): Single<List<Pair<Collection, EnumMap<CardType, Int>>>> {
        Timber.e("Select collections order by $order, $subjectFilter")
        val dao = AppDatabase.getInstance().collectionDao()

        return Single.fromCallable {
            val sizes = dao.getCollectionTypeSizes().blockingGet()

            when (order) {
                CollectionSortOrder.RECENT -> dao.getAllOrderByDate()
                CollectionSortOrder.AZ -> dao.getAllOrderByName()
            }.flattenAsObservable { it.asIterable() }
                .filter {
                    (subjectFilter == null || it.codeSubject == subjectFilter) && (studentFilter == null || it.codeStudent == studentFilter)
                }
                .map { collection ->
                    val enumMap: EnumMap<CardType, Int> = EnumMap(CardType::class.java)
                    sizes.filter { it.id == collection.id }.forEach { enumMap[it.cardType] = it.cnt }
                    Pair(collection, enumMap)
                }
                .toList()
                .map { ArrayList<Pair<Collection, EnumMap<CardType, Int>>>(it) }
                .blockingGet()
        }
    }

    override fun selectSuperTypeSizes(subjectFilter: LanguageCode?, studentFilter: LanguageCode?): Single<EnumMap<CardType, Int>> {
        return Single.fromCallable {
            val sizes =
                if (subjectFilter == null) {
                    AppDatabase.getInstance().collectionDao().getTypeSizes().blockingGet()
                } else {
                    AppDatabase.getInstance().collectionDao().getTypeSizesBySubject(subjectFilter).blockingGet()
                }

            val enumMap: EnumMap<CardType, Int> = EnumMap(CardType::class.java)
            sizes.forEach { enumMap[it.cardType] = it.cnt }
            enumMap
        }
    }

    override fun selectOtherCollections(id: Long, subjectFilter: LanguageCode?, studentFilter: LanguageCode?): Single<List<Collection>> {
        return AppDatabase.getInstance().collectionDao().getOthersOnce(id)
            .flattenAsObservable { it.asIterable() }
            .filter {
                (subjectFilter == null || it.codeSubject == subjectFilter) && (studentFilter == null || it.codeStudent == studentFilter)
            }
            .toList()
            .map { ArrayList<Collection>(it) }
    }

    override fun getRecentCollection(): Single<Collection> {
        return Single.fromCallable {
            val collection = AppDatabase.getInstance().collectionDao().getRecentCollection().blockingGet()
            collection
        }
    }

    override fun selectCollectionsWithCards(subjectFilter: LanguageCode?, studentFilter: LanguageCode?): Single<List<Pair<Collection, Int>>> {
        val db = AppDatabase.getInstance()

        return Single.fromCallable {
            val sizes = db.collectionDao().countAllCollections().blockingGet()

            db.collectionDao().getAllOrderByName().flattenAsObservable { it.asIterable() }
                .filter {
                    (subjectFilter == null || it.codeSubject == subjectFilter) && (studentFilter == null || it.codeStudent == studentFilter)
                }
                .map { collection ->
                    val size = sizes.firstOrNull { it.id == collection.id } ?: CollectionSize(collection.id)
                    Pair(collection, size.cnt)
                }
                .filter {
                    it.second > 0
                }
                .toList()
                .map { ArrayList<Pair<Collection, Int>>(it) }
                .blockingGet()
        }
    }

    override fun selectCardsByCollectionId(id: Long, order: CardSortOrder, subjectFilter: LanguageCode?): Single<List<Card>> {
        val dao = AppDatabase.getInstance().cardDao()
        return when (order) {
            CardSortOrder.DEFAULT -> dao.selectByCollectionOrderByDefault(id)
            CardSortOrder.AZ -> dao.selectByCollectionOrderByName(id)
            CardSortOrder.RECENT -> dao.selectByCollectionOrderByDateDesc(id)
            CardSortOrder.OLDEST -> dao.selectByCollectionOrderByDateAsc(id)
            CardSortOrder.TYPE -> dao.selectByCollectionOrderByType(id)
            CardSortOrder.UNKNOWN -> dao.selectByCollectionOrderByKnowledgeAsc(id)
            CardSortOrder.KNOWN -> dao.selectByCollectionOrderByKnowledgeDesc(id)
        }.flattenAsObservable { it.asIterable() }
            .filter {
                if (subjectFilter == null) {
                    true
                } else {
                    it.subject == subjectFilter
                }
            }
            .toList()
            .map { ArrayList<Card>(it) }
    }

    override fun selectCardsByType(type: CardType, order: CardSortOrder, subjectFilter: LanguageCode?): Single<List<Card>> {
        val dao = AppDatabase.getInstance().cardDao()
        Timber.e("SELECT with $type by $order, filter: $subjectFilter")
        return when (order) {
            CardSortOrder.DEFAULT -> dao.selectByTypeOrderByDefault(type)
            CardSortOrder.AZ -> dao.selectByTypeOrderByName(type)
            CardSortOrder.RECENT -> dao.selectByTypeOrderByDateDesc(type)
            CardSortOrder.OLDEST -> dao.selectByTypeOrderByDateAsc(type)
            CardSortOrder.TYPE -> dao.selectByTypeOrderByName(type)
            CardSortOrder.UNKNOWN -> dao.selectByTypeOrderByKnowledgeAsc(type)
            CardSortOrder.KNOWN -> dao.selectByTypeOrderByKnowledgeDesc(type)
        }.flattenAsObservable { it.asIterable() }
            .filter {
                if (subjectFilter == null) {
                    true
                } else {
                    it.subject == subjectFilter
                }
            }
            .toList()
            .map { ArrayList<Card>(it) }
    }

    override fun selectAllCards(order: CardSortOrder, subjectFilter: LanguageCode?, studentFilter: LanguageCode?): Single<List<Card>> {
        val dao = AppDatabase.getInstance().cardDao()
        return when (order) {
            CardSortOrder.DEFAULT -> dao.selectAllOrderByDefault()
            CardSortOrder.AZ -> dao.selectAllOrderByName()
            CardSortOrder.RECENT -> dao.selectAllOrderByDateDesc()
            CardSortOrder.OLDEST -> dao.selectAllOrderByDateAsc()
            CardSortOrder.TYPE -> dao.selectAllOrderByType()
            CardSortOrder.UNKNOWN -> dao.selectAllOrderByKnowledgeAsc()
            CardSortOrder.KNOWN -> dao.selectAllOrderByKnowledgeDesc()
        }.flattenAsObservable { it.asIterable() }
            .filter {
                if (subjectFilter == null) {
                    true
                } else {
                    it.subject == subjectFilter
                }
            }
            .toList()
            .map { ArrayList<Card>(it) }
    }

    override fun selectCardsForTest(
        collectionId: Long,
        showAll: Boolean,
        offset: Int?,
        limit: Int?,
    ): Single<List<Card>> {
        val dao = AppDatabase.getInstance().cardDao()
        return if (offset != null && limit != null && offset >= 0 && limit >= 0) {
            dao.selectByCollectionForTest(collectionId, offset, limit)
        } else {
            dao.selectByCollectionForTest(collectionId, showAll)
        }.map { ArrayList<Card>(it) }
    }

    override fun selectCardsForShortTest(collectionId: Long, offset: Int, limit: Int): Single<List<Card>> {
        val dao = AppDatabase.getInstance().cardDao()
        return dao.selectByCollectionForTestOrderByDate(collectionId, offset, limit).map { ArrayList<Card>(it) }
    }

    override fun clearDb(): Single<Unit> {
        return Single.fromCallable {
            AppDatabase.getInstance().clearAllTables()
        }
    }

    override fun updateCardKnowledge(id: Long, value: Int): Single<Int> {
        return AppDatabase.getInstance().cardDao().updateRotationDateById(id, value, OffsetDateTime.now(ZoneOffset.UTC))
    }

    override fun updateCardDate(id: Long): Single<Int> {
        return AppDatabase.getInstance().cardDao().updateDateById(id, OffsetDateTime.now(ZoneOffset.UTC))
    }

    override fun copyCard(cardId: Long, collectionId: Long): Single<Long> {
        return Single.fromCallable {
            val db = AppDatabase.getInstance()
            val linkId = db.linkDao().getLinkId(collectionId, cardId).blockingGet()
            if (linkId == null || linkId == 0L) {
                db.linkDao().insertIgnore(
                    LinkEntity(
                        cardId = cardId,
                        collectionId = collectionId,
                    ),
                ).blockingGet()
            } else {
                0
            }
        }
    }

    override fun deleteCard(cardId: Long, collectionId: Long?): Single<Unit> {
        return Single.fromCallable {
            val db = AppDatabase.getInstance()
            if (collectionId == null || collectionId == 0L) {
                Timber.e("Delete from Global: $cardId")
                db.cardDao().deleteLinkByCardId(cardId).blockingGet()
                db.cardDao().deleteById(cardId).blockingGet()
                Timber.e("Delete from Global COMPLETED")
            } else {
                Timber.e("Delete from collection $collectionId: $cardId")
                val links = db.linkDao().getByCardId(cardId).blockingGet()
                links.firstOrNull { collectionId == it.collectionId }?.let {
                    if (links.size > 1) {
                        db.linkDao().deleteById(it.id).blockingGet()
                    } else {
                        Timber.e("Delete link and card: $cardId")
                        db.linkDao().deleteById(it.id).blockingGet()
                        db.cardDao().deleteById(cardId).blockingGet()
                    }
                }
            }
        }
    }

    override fun getCard(cardId: Long): Single<Card> {
        return Single.fromCallable {
            val card = AppDatabase.getInstance().cardDao().getCard(cardId).blockingGet()
            card
        }
    }

    override fun updateCard(card: Card): Single<Unit> {
        val dao = AppDatabase.getInstance().cardDao()

        return Single.fromCallable {
            val updatedCard = dao.getCard(card.id).blockingGet().apply {
                name = card.name
                translation = card.translation
                transcription = card.transcription
                extraData = card.extraData
                cardType = card.cardType
            }
            dao.updateReplace(CardEntity(updatedCard)).blockingGet()
        }
    }

    override fun insertCard(card: Card, collectionId: Long): Single<Unit> {
        val db = AppDatabase.getInstance()

        return Single.fromCallable {
            val cardEntity = CardEntity(
                name = card.name,
                transcription = card.transcription,
                translation = card.translation,
                extraData = card.extraData,
                cardType = card.cardType,
                subject = card.subject,
            )

            val id = db.cardDao().insertReplace(cardEntity).blockingGet()

            if (collectionId > 0) db.linkDao().insertReplace(LinkEntity(cardId = id, collectionId = collectionId)).blockingGet()
        }
    }

    override fun selectLinkedCollections(cardId: Long): Single<List<Collection>> {
        return AppDatabase.getInstance().linkDao().getCollectionsByCardId(cardId)
            .map { ArrayList<Collection>(it) }
    }

    override fun getCollection(collectionId: Long): Single<Collection> {
        return AppDatabase.getInstance().collectionDao().getById(collectionId).map{it}
    }

    override fun updateCollection(collectionId: Long, name: String?, description: String?, subject: LanguageCode?): Single<Unit> {
        return Single.fromCallable {
            AppDatabase.getInstance().collectionDao().updateInfo(collectionId, name, description, subject?.value).blockingGet()
        }
    }

    override fun updateCollectionDate(collectionId: Long): Single<Unit> {
        return Single.fromCallable {
            AppDatabase.getInstance().collectionDao().updateDateById(collectionId, OffsetDateTime.now(ZoneOffset.UTC)).blockingGet()
        }
    }

    override fun insertCollection(name: String?, description: String?, subject: LanguageCode?): Single<Unit> {
        return Single.fromCallable {
            val collectionEntity = CollectionEntity(
                name = name,
                description = description,
                imei = "",
                timestamp = OffsetDateTime.now(ZoneOffset.UTC),
                collectionType = CollectionType.USER,
                codeSubject = subject,
            )
            AppDatabase.getInstance().collectionDao().insertReplace(collectionEntity).blockingGet()
        }
    }

    override fun deleteCollection(collectionId: Long): Single<Unit> {
        val db = AppDatabase.getInstance()

        return Single.fromCallable {
            // 1 - delete all links
            db.linkDao().deleteByCollectionId(collectionId).blockingGet()
            // 2 - Get All Cards without links and delete them
            val freeList = db.linkDao().getFreeCards().blockingGet()
            if (!freeList.isNullOrEmpty()) {
                // Частное и остаток от делителя. 100 - допустимый размер блока
                val n = freeList.size / 100
                val m = freeList.size % 100
                var i = 0
                while (i < n) {
                    db.cardDao().deleteByIdList(freeList.subList(i * 100, (i + 1) * 100)).blockingGet()
                    i++
                }
                if (m > 0) {
                    db.cardDao().deleteByIdList(freeList.subList(i * 100, freeList.size)).blockingGet()
                }
            }
            // 3 - delete from collections
            db.collectionDao().deleteById(collectionId).blockingGet()
        }
    }
}
