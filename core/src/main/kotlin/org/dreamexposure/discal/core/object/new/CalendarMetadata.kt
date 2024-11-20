package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.CalendarMetadataData
import org.dreamexposure.discal.core.extensions.asInstantMilli
import org.dreamexposure.discal.core.extensions.asSnowflake
import org.dreamexposure.discal.core.extensions.isExpiredTtl
import reactor.core.publisher.Mono
import java.time.Instant
import javax.crypto.IllegalBlockSizeException

data class CalendarMetadata(
    val guildId: Snowflake,
    val number: Int,
    val host: Host,
    val id: String,
    val address: String,
    val external: Boolean,
    val secrets: Secrets,
) {
    companion object {
        suspend operator fun invoke(data: CalendarMetadataData): CalendarMetadata {
            val aes = AESEncryption(data.privateKey)
            val accessToken =
                if (!data.expiresAt.asInstantMilli().isExpiredTtl() && data.external) aes.decrypt(data.accessToken)
                    .onErrorResume(IllegalBlockSizeException::class.java) {
                        Mono.empty()
                    }.awaitSingleOrNull()
                else null // No point in trying to decrypt if it's expired
            val refreshToken =
                if (data.external) aes.decrypt(data.refreshToken)
                    .onErrorResume(IllegalBlockSizeException::class.java) {
                        Mono.empty()
                    }.awaitSingleOrNull()
                else null // No point in trying to decrypt if calendar is not external, we don't use these internally

            return CalendarMetadata(
                guildId = data.guildId.asSnowflake(),
                number = data.calendarNumber,
                host = Host.valueOf(data.host),
                id = data.calendarId,
                address = data.calendarAddress,
                external = data.external,
                secrets = Secrets(
                    credentialId = data.credentialId,
                    privateKey = data.privateKey,
                    expiresAt = if (accessToken != null) data.expiresAt.asInstantMilli() else Instant.EPOCH,
                    refreshToken = refreshToken ?: "",
                    accessToken = accessToken ?: "",
                )
            )
        }
    }

    ////////////////////////////
    ////// Nested classes //////
    ////////////////////////////
    data class Secrets(
        val credentialId: Int,
        val privateKey: String,
        var expiresAt: Instant,
        var refreshToken: String,
        var accessToken: String,
    )

    enum class Host {
        GOOGLE,
    }
}
