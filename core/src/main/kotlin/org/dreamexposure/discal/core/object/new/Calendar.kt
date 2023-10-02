package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.crypto.AESEncryption
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
        secrets = Secrets(data)
    )

    data class Secrets(
        val credentialId: Int,
        val privateKey: String,
        var expiresAt: Instant,
    ) {
        lateinit var refreshToken: String
        lateinit var accessToken: String

        constructor(data: CalendarData) : this(
            credentialId = data.credentialId,
            privateKey = data.privateKey,
            expiresAt = data.expiresAt.asInstantMilli()
        ) {
            suspend {
                val aes = AESEncryption(privateKey)
                refreshToken = aes.decrypt(data.refreshToken).awaitSingle()
                accessToken = aes.decrypt(data.accessToken).awaitSingle()
            }
        }
    }
}
