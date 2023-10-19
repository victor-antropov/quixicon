package com.quixicon.domain.boundaries

import android.net.Uri
import com.quixicon.domain.entities.remotes.CollectionData
import io.reactivex.Single

interface FilesBoundary {

    fun readCollection(uri: Uri): Single<CollectionData>
}
