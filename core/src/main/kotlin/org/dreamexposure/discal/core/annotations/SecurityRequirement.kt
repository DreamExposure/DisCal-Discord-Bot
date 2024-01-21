package org.dreamexposure.discal.core.annotations

import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.core.`object`.new.security.TokenType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SecurityRequirement(
        val schemas: Array<TokenType> = [], // Default to allowing any token kind
        val scopes: Array<Scope>,
        val disableSecurity: Boolean = false,
)
