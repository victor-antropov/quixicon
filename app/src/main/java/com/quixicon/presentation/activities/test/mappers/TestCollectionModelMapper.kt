package com.quixicon.presentation.activities.test.mappers

import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.mappers.Mapper
import com.quixicon.presentation.activities.test.models.TestCollectionModel
import javax.inject.Inject

interface TestCollectionModelMapper : Mapper<Pair<Collection, Int>, TestCollectionModel>

class TestCollectionModelMapperImpl @Inject constructor() : TestCollectionModelMapper {

    override fun mapToInput(output: TestCollectionModel): Pair<Collection, Int> {
        TODO("Not yet implemented")
    }

    override fun mapToOutput(input: Pair<Collection, Int>): TestCollectionModel {
        return with(input) {
            TestCollectionModel(
                id = this.first.id,
                name = this.first.name ?: "",
                size = this.second,
                type = this.first.collectionType,
                subject = this.first.codeSubject
            )
        }
    }
}
