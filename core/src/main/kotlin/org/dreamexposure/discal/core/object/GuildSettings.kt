package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer

@Serializable
data class GuildSettings(
        @Serializable(with = SnowflakeAsStringSerializer::class)
        @SerialName("guild_id")
        val guildID: Snowflake,

        @SerialName("control_role")
        var controlRole: String = "everyone",
        @SerialName("discal_channel")
        var discalChannel: String = "all",

        @SerialName("simple_announcement")
        var simpleAnnouncements: Boolean = false,
        var lang: String = "ENGLISH",
        var prefix: String = "!",

        @SerialName("patron_guild")
        var patronGuild: Boolean = false,
        @SerialName("dev_guild")
        var devGuild: Boolean = false,
        @SerialName("max_calendars")
        var maxCalendars: Int = 1,

        @SerialName("twelve_hour")
        var twelveHour: Boolean = true,
        var branded: Boolean = false,
) {
    @SerialName("")
    val dmAnnouncements: MutableList<String> = mutableListOf()

    companion object {
        @JvmStatic
        fun empty(guildId: Snowflake) = GuildSettings(guildId)
    }

    fun getDmAnnouncementsString(): String {
        val dm = StringBuilder()

        for ((i, sub) in this.dmAnnouncements.withIndex()) {
            if (i == 0) dm.append(sub)
            else dm.append(",").append(sub)
        }

        return dm.toString()
    }

    fun setDmAnnouncementsString(dm: String) {
        this.dmAnnouncements += dm.split(",")
    }
}
