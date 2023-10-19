package com.quixicon.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.quixicon.data.db.converters.CollectionTypeValueConverter
import com.quixicon.data.db.converters.DateTimeStringConverter
import com.quixicon.data.db.converters.LanguageCodeValueConverter
import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "collections")
data class CollectionEntity(
    @field:PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "id")
    override var id: Long = 0,

    @field:TypeConverters(CollectionTypeValueConverter::class)
    @field:ColumnInfo(name = "type")
    override var collectionType: CollectionType = CollectionType.DEFAULT,

    @field:ColumnInfo(name = "name")
    override var name: String? = null,

    @field:ColumnInfo(name = "description")
    override var description: String? = null,

    @field:TypeConverters(LanguageCodeValueConverter::class)
    @field:ColumnInfo(name = "codeSubject")
    override var codeSubject: LanguageCode? = null,

    @field:TypeConverters(LanguageCodeValueConverter::class)
    @field:ColumnInfo(name = "codeStudent")
    override var codeStudent: LanguageCode? = null,

    @field:ColumnInfo(name = "imei")
    override var imei: String? = null,

    @field:ColumnInfo(name = "parentImei")
    override var parentImei: String? = null,

    @field:TypeConverters(DateTimeStringConverter::class)
    @field:ColumnInfo(name = "timestamp")
    override var timestamp: OffsetDateTime? = null,

    @field:ColumnInfo(name = "parentCollectionId")
    override var parentCollectionId: Long? = null

) : BaseEntity(), Collection
