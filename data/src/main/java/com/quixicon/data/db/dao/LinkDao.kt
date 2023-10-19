package com.quixicon.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.quixicon.data.db.entities.CollectionEntity
import com.quixicon.data.db.entities.LinkEntity
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface LinkDao : BaseDao<LinkEntity> {
/*
•	insert -> insertReplace (base)
•	insertList -> insertReplace (base)
•	getLinkId +
•	getByCardId +
•	deleteById +
•	getCollectionsByCardId +
•	deleteByCollectionId +
•	getFreeCards +
*/

    @Query("SELECT id from links where collectionId=:collectionId and cardId=:cardId limit 1")
    fun getLinkId(collectionId: Long, cardId: Long): Maybe<Long>

    @Query("Select * from links where cardId=:cardId")
    fun getByCardId(cardId: Long): Single<List<LinkEntity>>

    @Query("DELETE FROM links WHERE id=:id")
    fun deleteById(id: Long): Completable

    @Query("Select c.* from links 'l' join collections 'c' on l.collectionId=c.id where l.cardId=:cardId")
    fun getCollectionsByCardId(cardId: Long): Single<List<CollectionEntity>>

    @Query("DELETE FROM links WHERE collectionId=:id")
    fun deleteByCollectionId(id: Long): Completable

    @Query("select cd.id from cards 'cd' LEFT JOIN links 'l' ON cd.id=l.cardId WHERE l.id IS NULL")
    fun getFreeCards(): Single<List<Long>>
}
