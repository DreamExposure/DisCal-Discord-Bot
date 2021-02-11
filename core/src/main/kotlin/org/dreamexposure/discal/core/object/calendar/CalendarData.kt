package org.dreamexposure.discal.core.`object`.calendar

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer
import org.dreamexposure.novautils.crypto.KeyGenerator

@Serializable
data class CalendarData(
        @Serializable(with = SnowflakeAsStringSerializer::class)
        @SerialName("guild_id")
        val guildId: Snowflake = Snowflake.of(0),
        @SerialName("calendar_number")
        val calendarNumber: Int = 1,
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
) {
    constructor(guildId: Snowflake, calendarNumber: Int, calendarId: String, calendarAddress: String, credentialId:
    Int) : this(guildId, calendarNumber, calendarId, calendarAddress, false, credentialId)

    companion object {
        @JvmStatic
        fun empty(guildId: Snowflake) = CalendarData(guildId)

        @JvmStatic
        fun emptyExternal(guildId: Snowflake) = CalendarData(guildId, external = true)
    }
}
