package com.quixicon.domain.boundaries

import com.quixicon.domain.entities.db.Collection
import com.quixicon.domain.entities.remotes.CollectionData

/**
 * Interface for working with transformations.
 */
interface TransformBoundary {

    // fun calcResultsToResearchItemModels(calcResults: List<CalcResult>): List<ResearchItemModel>

    fun CollectionDataToCollectionEntity(data: CollectionData): Collection
}
