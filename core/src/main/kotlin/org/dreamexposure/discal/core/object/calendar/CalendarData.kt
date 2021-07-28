package org.dreamexposure.discal.core.`object`.calendar

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer
import org.dreamexposure.novautils.crypto.KeyGenerator
import java.time.Instant

@Serializable
data class CalendarData(
        @Serializable(with = SnowflakeAsStringSerializer::class)
        @SerialName("guild_id")
        val guildId: Snowflake = Snowflake.of(0),
        @SerialName("calendar_number")
        val calendarNumber: Int = 1,
        val host: CalendarHost,
        @SerialName("calendar_id")
        val calendarId: String = "primary",
        @SerialName("calendar_address")
        val calendarAddress: String = "primary",
        val external: Boolean = false,

        //secure values that should not be serialized
        @Transient
        val credentialId: Int = 0,
        @Transient
        var privateKey: String = KeyGenerator.csRandomAlphaNumericString(16),
        @Transient
        var encryptedAccessToken: String = "N/a",
        @Transient
        var encryptedRefreshToken: String = "N/a",
        @Transient
        var expiresAt: Instant = Instant.now()
) {
    constructor(guildId: Snowflake, calendarNumber: Int, host: CalendarHost, calendarId: String,
                calendarAddress: String, credentialId: Int) :
            this(guildId, calendarNumber, host, calendarId, calendarAddress, false, credentialId)

    companion object {
        @JvmStatic
        fun empty(guildId: Snowflake, host: CalendarHost) = CalendarData(guildId, host = host)

        fun emptyExternal(guildId: Snowflake, host: CalendarHost) = CalendarData(guildId, external = true, host = host)
    }

    fun expired() = Instant.now().isAfter(expiresAt)
}
