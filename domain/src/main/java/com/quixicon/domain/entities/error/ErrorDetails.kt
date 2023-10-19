package com.quixicon.domain.entities.error

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Class for store error messages from the server side.
 *
 * @param entityId The identifier of the entity that has been created and not rolled back from DB
 * upon error. For example the wave calc result.
 * @param message Message with error from the server side.
 */
data class ErrorDetails(
    @Expose
    @SerializedName("entity_id")
    val entityId: Long = 0L,
    @Expose
    @SerializedName("message")
    val message: String? = null
)
