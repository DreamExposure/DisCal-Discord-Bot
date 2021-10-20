package org.dreamexposure.discal.core.`object`.rest

import kotlinx.serialization.Serializable

@Serializable
data class RestError(
        val code: Code,
        val message: String = code.message,
) {
    enum class Code(
            val value: Int,
            val message: String,
    ) {
        INTERNAL_SERVER_ERROR(0, "Internal Server Error"),
        BAD_REQUEST(1, "Bad request"),


        ACCESS_REVOKED(1001, "Access to resource revoked"),
    }
}
