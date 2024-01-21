package org.dreamexposure.discal.core.`object`.rest.v1.security

import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.core.`object`.new.security.TokenType

data class ValidateRequest(
    val token: String,
    val schemas: List<TokenType>,
    val scopes: List<Scope>,
)
