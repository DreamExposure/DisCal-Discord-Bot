package org.dreamexposure.discal.core.`object`.rest

import kotlinx.serialization.Serializable

@Serializable
enum class RestError(
        val code: Int,
        val message: String,
) {
    INTERNAL_SERVER_ERROR(0, "Internal Server Error"),
    BAD_REQUEST(1, "Bad request"),


    ACCESS_REVOKED(1001, "Access to resource revoked"),
    NOT_FOUND(1002, "Resource not found"),
}
