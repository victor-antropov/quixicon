package com.quixicon.presentation.activities.editcollection.mappers

import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.editcollection.models.EditCollectionModel
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

interface EditCollectionModelMapper : Mapper<Collection, EditCollectionModel>

class EditCollectionModelMapperImpl @Inject constructor() : EditCollectionModelMapper {

    override fun mapToInput(output: EditCollectionModel): Collection {
        return object : Collection {
            override var id: Long = output.id
            override var collectionType: CollectionType = CollectionType.USER
            override var name: String? = output.name
            override var description: String? = output.description
            override var codeSubject: LanguageCode? = output.subject
            override var codeStudent: LanguageCode? = null
            override var imei: String? = null
            override var parentImei: String? = null
            override var timestamp: OffsetDateTime? = null
            override var parentCollectionId: Long? = null
        }
    }

    override fun mapToOutput(input: Collection): EditCollectionModel {
        return with(input) {
            val subject = enumValues<LanguageCode>().firstOrNull() { it == this.codeSubject } ?: LanguageCode.UNDEFINED
            EditCollectionModel(
                id = this.id,
                name = this.name ?: "",
                description = this.description ?: "",
                subject = subject
            )
        }
    }
}
