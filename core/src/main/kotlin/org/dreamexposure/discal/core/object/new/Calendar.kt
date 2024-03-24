package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.CalendarData
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.extensions.asInstantMilli
import org.dreamexposure.discal.core.extensions.asSnowflake
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import java.time.Instant
import javax.crypto.IllegalBlockSizeException

data class Calendar private constructor(
    val guildId: Snowflake,
    val number: Int,
    val host: CalendarHost,
    val id: String,
    val address: String,
    val external: Boolean,
    val secrets: Secrets,
) {
    companion object {
        suspend operator fun invoke(data: CalendarData): Calendar {
            val aes = AESEncryption(data.privateKey)
            val accessToken = if (!data.expiresAt.asInstantMilli().isExpiredTtl()) try {
                aes.decrypt(data.accessToken).awaitSingle()
            } catch (ex: IllegalBlockSizeException) {
                null
            } else null // No point in trying to decrypt if it's expired

            return Calendar(
                guildId = data.guildId.asSnowflake(),
                number = data.calendarNumber,
                host = CalendarHost.valueOf(data.host),
                id = data.calendarId,
                address = data.calendarAddress,
                external = data.external,
                secrets = Secrets(
                    credentialId = data.credentialId,
                    privateKey = data.privateKey,
                    expiresAt = if (accessToken != null) data.expiresAt.asInstantMilli() else Instant.EPOCH,
                    refreshToken = aes.decrypt(data.refreshToken).awaitSingle(),
                    accessToken = accessToken ?: "",
                )
            )
        }
    }

    data class Secrets(
        val credentialId: Int,
        val privateKey: String,
        var expiresAt: Instant,
        var refreshToken: String,
        var accessToken: String,
    )
}
