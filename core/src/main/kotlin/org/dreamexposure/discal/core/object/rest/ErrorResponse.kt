package org.dreamexposure.discal.core.`object`.rest

data class ErrorResponse(
    val error: String,
    val exception: Exception? = null
)
