package org.dreamexposure.discal.core.`object`.new.model.discal.cam

import org.springframework.http.HttpStatus

data class SecurityValidateV1Response(
    val valid: Boolean,
    val code: HttpStatus,
    val message: String,
)

