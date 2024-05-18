package org.dreamexposure.discal.core.`object`.new.model

import org.dreamexposure.discal.core.`object`.rest.ErrorResponse

data class ResponseModel<T>(
    val code: Int,
    val entity: T?,
    val error: ErrorResponse?
) {
    constructor(entity: T, code: Int = 200): this(code, entity, null)

    constructor(error: ErrorResponse, code: Int): this(code, null, error)
}
