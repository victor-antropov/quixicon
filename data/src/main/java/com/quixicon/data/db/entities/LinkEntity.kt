package com.quixicon.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quixicon.domain.entities.db.LinkInterface

@Entity(tableName = "links")
data class LinkEntity(

    @field:PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    override var id: Long = 0,

    @field:ColumnInfo(name = "cardId")
    override var cardId: Long? = null,

    @field:ColumnInfo(name = "collectionId")
    override var collectionId: Long? = null

) : BaseEntity(), LinkInterface
