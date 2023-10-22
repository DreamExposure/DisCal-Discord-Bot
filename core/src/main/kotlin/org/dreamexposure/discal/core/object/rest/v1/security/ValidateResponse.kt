package org.dreamexposure.discal.core.`object`.rest.v1.security

import org.springframework.http.HttpStatus

data class ValidateResponse(
    val valid: Boolean,
    val code: HttpStatus,
    val message: String,
)
