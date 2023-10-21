package org.dreamexposure.discal.core.`object`.new.security

enum class Scope {
    CALENDAR_TOKEN_READ,

    OAUTH2_DISCORD,
    ;

    companion object {
        fun defaultWebsiteLoginScopes() = listOf(
            OAUTH2_DISCORD,
        )

        fun defaultBasicAppScopes() = listOf<Scope>()
    }
}
