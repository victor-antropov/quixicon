package com.quixicon.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverters
import com.quixicon.data.db.converters.CollectionTypeValueConverter
import com.quixicon.data.db.converters.DateTimeStringConverter
import com.quixicon.data.db.converters.LanguageCodeValueConverter
import com.quixicon.data.db.entities.CollectionEntity
import com.quixicon.data.db.entities.TypeSizeEntity
import com.quixicon.domain.entities.db.CollectionSize
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.threeten.bp.OffsetDateTime
import java.util.*

@Dao
interface CollectionDao : BaseDao<CollectionEntity> {
    /*
•	insert -> insertReplace (base)
•	update -> updateReplace (base)
•	getById
•	getByIdOnce -> Пробуем заменить на Single оба метода getById
•	deleteById +
•	updateDateById + (нужно разобраться с датой Data или один из OffsetDateTime)

•	getImeiList
•	getImeiListsOnce
•	checkImei + (Подумать как переделать вариант с imei)


•	getExtraById
•	getExtraByTypeFiltered
•	getAvgById
•	getAvgByTypeFiltered

•	getAllFilteredOnceOther
•	getByParentId
•	lastCollection
•	getAllFilteredOnce
•	countAllCollections
•	getAllExtraFlowFiltered2
•	getAllExtraFlowFiltered1

•	getAllFiltered
•	getAllFilteredDate


    Уберем все отсылки к полю codeSubject, само поле пусть останется в БД

    getAll+
    getAllOnce+
    Посмотрим, получится ли оставить только одну из них

    */

    @Query("SELECT * from collections where id=:id")
    fun getById(id: Long): Single<CollectionEntity>

    @Query("DELETE FROM collections WHERE id = :id")
    fun deleteById(id: Long): Completable

    @TypeConverters(DateTimeStringConverter::class)
    @Query("UPDATE collections set timestamp=:date where id=:id")
    fun updateDateById(id: Long, date: OffsetDateTime): Completable

    @Query("UPDATE collections set name=:name, description=:description, codeSubject=:subject where id=:id")
    fun updateInfo(id: Long, name: String?, description: String?, subject: String?): Single<Int>

    @Query("SELECT id from collections where imei=:imei")
    fun checkImei(imei: String): Maybe<Long>

    @Query("SELECT * from collections where parentImei=:imei")
    fun getByParentImei(imei: String): Single<List<CollectionEntity>>

    @Query("SELECT imei from collections where imei<>''")
    fun getImeiList(): Flowable<List<String>>

    @Query("SELECT imei from collections where imei<>''")
    fun getImeiListOnce(): Single<List<String>>

    @TypeConverters(CollectionTypeValueConverter::class)
    @Query("SELECT * from collections where type<>:excludeType order by lower(name)")
    fun getAllOrderByName(excludeType: CollectionType = CollectionType.CORE): Single<List<CollectionEntity>>

    @TypeConverters(CollectionTypeValueConverter::class)
    @Query("SELECT * from collections where type<>:excludeType order by datetime(timestamp) desc")
    fun getAllOrderByDate(excludeType: CollectionType = CollectionType.CORE): Single<List<CollectionEntity>>

    @TypeConverters(CollectionTypeValueConverter::class)
    @Query("SELECT * from collections where id!=:myId and type<>:excludeType")
    fun getOthersOnce(myId: Long, excludeType: CollectionType = CollectionType.CORE): Single<List<CollectionEntity>>

    @Query("select l.collectionId as id, count(*) as cnt  from cards 'cd' join links 'l' on cd.id=l.cardId  group by l.collectionId")
    fun countAllCollections(): Single<List<CollectionSize>>

    @Query("select l.collectionId as id, cd.cardType as cardType, count(*) as cnt from cards 'cd' join links 'l' on cd.id=l.cardId  group by cd.cardType, l.collectionId")
    fun getCollectionTypeSizes(): Single<List<TypeSizeEntity>>

    @Query("select 0 as id, cd.cardType as cardType, count(distinct(cd.id)) as cnt from cards 'cd' join links 'l' on cd.id=l.cardId  group by cd.cardType")
    fun getTypeSizes(): Single<List<TypeSizeEntity>>

    @TypeConverters(LanguageCodeValueConverter::class)
    @Query("select 0 as id, cd.cardType as cardType, count(distinct(cd.id)) as cnt from cards 'cd' join links 'l' on cd.id=l.cardId where cd.subject=:language group by cd.cardType")
    fun getTypeSizesBySubject(language: LanguageCode): Single<List<TypeSizeEntity>>

    @Query("select * from collections order by datetime(timestamp) desc limit 1")
    fun getRecentCollection(): Single<CollectionEntity>
}
