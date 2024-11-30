package org.dreamexposure.discal.core.`object`.new.security

enum class Scope {
    CALENDAR_TOKEN_READ,

    CALENDAR_READ,
    CALENDAR_WRITE,
    CALENDAR_EVENT_READ,
    CALENDAR_EVENT_WRITE,

    GUILD_SETTINGS_READ,
    GUILD_SETTINGS_WRITE,

    EVENT_RSVP_READ,
    EVENT_RSVP_WRITE,

    ANNOUNCEMENT_READ,
    ANNOUNCEMENT_WRITE,

    OAUTH2_DISCORD,

    INTERNAL_CAM_VALIDATE_TOKEN,
    INTERNAL_HEARTBEAT,
    ;

    companion object {
        fun defaultWebsiteLoginScopes() = listOf(
            OAUTH2_DISCORD,
        )

        fun defaultBasicAppScopes() = listOf<Scope>()
    }
}
