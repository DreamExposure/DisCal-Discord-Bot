package org.dreamexposure.discal.core.`object`.new.model.discal.cam

import org.dreamexposure.discal.core.`object`.new.security.AccessLevel
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.dreamexposure.discal.core.`object`.new.security.TokenType

data class SecurityValidateV1Request(
    val token: String,
    val schemas: List<TokenType>,
    val scopes: List<Scope>,
    val accessLevel: AccessLevel,
    val guildId: String?,
    val calendarNumber: Int?,
)
