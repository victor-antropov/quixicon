package com.quixicon.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverters
import com.quixicon.data.db.converters.CardTypeValueConverter
import com.quixicon.data.db.converters.DateTimeStringConverter
import com.quixicon.data.db.entities.CardEntity
import com.quixicon.domain.entities.enums.CardType
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.threeten.bp.OffsetDateTime

@Dao
interface CardDao : BaseDao<CardEntity> {

/*
    Сначала определим основные методы, чтобы не перегружать логику.
    Например откажемся от сортировки.

    •	insert -> insertReplace
    •	insertList -> insertReplace
    •	update -> updateReplace
    •	delete -> delete

    •	getCard ? maybe или single
    •	selectByCollectionOrderByName ? Flowable или single
    •	selectByTypeOrderByName

    •	deleteCardLinkTransaction
    •	deleteCardLinkMultiTransaction
    •	updateRotationById
    •	deleteByIdList
*/

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCard(id: Long): Maybe<CardEntity>

    // select all from ordinary collections

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id ORDER BY lower(cd.name)")
    fun selectByCollectionOrderByName(id: Long): Single<List<CardEntity>>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id ORDER BY cd.id")
    fun selectByCollectionOrderByDefault(id: Long): Single<List<CardEntity>>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id")
    fun selectByCollectionOrderByDefaultOnce(id: Long): List<CardEntity>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id ORDER BY datetime(cd.timestamp)")
    fun selectByCollectionOrderByDateAsc(id: Long): Single<List<CardEntity>>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id ORDER BY datetime(cd.timestamp) desc")
    fun selectByCollectionOrderByDateDesc(id: Long): Single<List<CardEntity>>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id ORDER BY cd.cardType desc, lower(cd.name)")
    fun selectByCollectionOrderByType(id: Long): Single<List<CardEntity>>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id ORDER BY cd.knowledge")
    fun selectByCollectionOrderByKnowledgeAsc(id: Long): Single<List<CardEntity>>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id ORDER BY cd.knowledge desc")
    fun selectByCollectionOrderByKnowledgeDesc(id: Long): Single<List<CardEntity>>

    // select all from super collections
    @TypeConverters(CardTypeValueConverter::class)
    @Query("SELECT * FROM  cards where cardType=:type order by id")
    fun selectByTypeOrderByDefault(type: CardType): Single<List<CardEntity>>

    @TypeConverters(CardTypeValueConverter::class)
    @Query("SELECT * FROM  cards  where cardType=:type order by lower(name)")
    fun selectByTypeOrderByName(type: CardType): Single<List<CardEntity>>

    @TypeConverters(CardTypeValueConverter::class)
    @Query("SELECT * FROM  cards  where cardType=:type order by knowledge")
    fun selectByTypeOrderByKnowledgeAsc(type: CardType): Single<List<CardEntity>>

    @TypeConverters(CardTypeValueConverter::class)
    @Query("SELECT * FROM  cards  where cardType=:type order by knowledge desc")
    fun selectByTypeOrderByKnowledgeDesc(type: CardType): Single<List<CardEntity>>

    @TypeConverters(CardTypeValueConverter::class)
    @Query("SELECT * FROM  cards  where cardType=:type order by datetime(timestamp)")
    fun selectByTypeOrderByDateAsc(type: CardType): Single<List<CardEntity>>

    @TypeConverters(CardTypeValueConverter::class)
    @Query("SELECT * FROM  cards  where cardType=:type order by dateTime(timestamp) desc")
    fun selectByTypeOrderByDateDesc(type: CardType): Single<List<CardEntity>>

    // select ALL cards
    @Query("SELECT * FROM  cards order by id")
    fun selectAllOrderByDefault(): Single<List<CardEntity>>

    @Query("SELECT * FROM  cards order by lower(name)")
    fun selectAllOrderByName(): Single<List<CardEntity>>

    @Query("SELECT * FROM  cards order by knowledge")
    fun selectAllOrderByKnowledgeAsc(): Single<List<CardEntity>>

    @Query("SELECT * FROM  cards order by knowledge desc")
    fun selectAllOrderByKnowledgeDesc(): Single<List<CardEntity>>

    @Query("SELECT * FROM  cards order by datetime(timestamp)")
    fun selectAllOrderByDateAsc(): Single<List<CardEntity>>

    @Query("SELECT * FROM cards order by dateTime(timestamp) desc")
    fun selectAllOrderByDateDesc(): Single<List<CardEntity>>

    @Query("SELECT * FROM cards order by cardType")
    fun selectAllOrderByType(): Single<List<CardEntity>>

    // Запрос для карточек для теста
    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id and (cd.knowledge<100 or cd.knowledge IS NULL or :show_all) and cd.translation<>'' and cd.translation IS NOT NULL")
    fun selectByCollectionForTest(id: Long, show_all: Boolean): Single<List<CardEntity>>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id  and cd.translation<>'' and cd.translation IS NOT NULL limit :limit offset :offset")
    fun selectByCollectionForTest(id: Long, offset: Int, limit: Int): Single<List<CardEntity>>

    @Query("select cd.* from cards 'cd' join links 'l' on cd.id=l.cardId where l.collectionId=:id order by datetime(timestamp) limit :limit offset :offset")
    fun selectByCollectionForTestOrderByDate(id: Long, offset: Int, limit: Int): Single<List<CardEntity>>

    @Query("DELETE FROM cards WHERE id = :id")
    fun deleteById(id: Long): Completable

    @Query("DELETE from cards WHERE id IN (:idList)")
    fun deleteByIdList(idList: List<Long>): Single<Int>

    @Query("UPDATE cards set knowledge=:value where id=:id")
    fun updateRotationById(id: Long, value: Int): Single<Int>

    @TypeConverters(DateTimeStringConverter::class)
    @Query("UPDATE cards set knowledge=:value, timestamp=:date where id=:id")
    fun updateRotationDateById(id: Long, value: Int, date: OffsetDateTime): Single<Int>

    @TypeConverters(DateTimeStringConverter::class)
    @Query("UPDATE cards set timestamp=:date where id=:id")
    fun updateDateById(id: Long, date: OffsetDateTime): Single<Int>

    // Транзакции для совместных операций Cards и Links
    @Query("DELETE FROM links WHERE cardId=:id")
    fun deleteLinkByCardId(id: Long): Completable

    @Query("DELETE FROM links WHERE id=:id")
    fun deleteLinkById(id: Long): Completable
}
