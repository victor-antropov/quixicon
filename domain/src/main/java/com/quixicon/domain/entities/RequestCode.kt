package com.quixicon.domain.entities

/**
 * Add request codes here.
 */
enum class RequestCode(val code: Int) {
    WRITE_PERMISSIONS(0),
    OPEN_DIRECTORY(1),
    PICK_CSV(2),
    COLLECTION_PREVIEW(3)
}
