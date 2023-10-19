package com.quixicon.presentation.activities.test.models

import androidx.databinding.BaseObservable
import com.quixicon.domain.entities.enums.CollectionType
import com.quixicon.domain.entities.enums.LanguageCode
import java.io.Serializable

class TestCollectionModel(
    val id: Long?,
    val name: String,
    val size: Int,
    val type: CollectionType = CollectionType.DEFAULT,
    val subject: LanguageCode? = null
) : BaseObservable(), Serializable {

    override fun toString(): String {
        return name
    }
}
