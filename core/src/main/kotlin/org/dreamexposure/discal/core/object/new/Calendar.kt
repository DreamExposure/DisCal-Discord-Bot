package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.CalendarData
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.extensions.asInstantMilli
import org.dreamexposure.discal.core.extensions.asSnowflake
import java.time.Instant

data class Calendar(
    val guildId: Snowflake,
    val number: Int,
    val host: CalendarHost,
    val id: String,
    val address: String,
    val external: Boolean,
    val secrets: Secrets,
) {
    constructor(data: CalendarData) : this(
        guildId = data.guildId.asSnowflake(),
        number = data.calendarNumber,
        host = CalendarHost.valueOf(data.host),
        id = data.calendarId,
        address = data.calendarAddress,
        external = data.external,
        secrets = Secrets(
            credentialId = data.credentialId,
            privateKey = data.privateKey,
            encryptedRefreshToken = data.refreshToken,
            encryptedAccessToken = data.accessToken,
            expiresAt = data.expiresAt.asInstantMilli(),
        )
    )

    data class Secrets(
        val credentialId: Int,
        val privateKey: String,
        val encryptedRefreshToken: String, // TODO: Secrets should be unencrypted immediately before/after Db write/read respectively
        var encryptedAccessToken: String, // TODO: Secrets should be unencrypted immediately before/after Db write/read respectively
        var expiresAt: Instant,
    )
}
