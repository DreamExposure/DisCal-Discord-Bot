package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.extensions.asStringList
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer
import java.util.*

@Serializable
data class GuildSettings(
      @Serializable(with = SnowflakeAsStringSerializer::class)
        @SerialName("guild_id")
        val guildID: Snowflake,

      @SerialName("control_role")
        var controlRole: String = "everyone",

      @SerialName("announcement_style")
        var announcementStyle: AnnouncementStyle = AnnouncementStyle.EVENT,
      @SerialName("time_format")
        var timeFormat: TimeFormat = TimeFormat.TWENTY_FOUR_HOUR,

      var lang: String = "ENGLISH",
      var prefix: String = "!",

      @SerialName("patron_guild")
        var patronGuild: Boolean = false,
      @SerialName("dev_guild")
        var devGuild: Boolean = false,
      @SerialName("max_calendars")
        var maxCalendars: Int = 1,

      var branded: Boolean = false,
) {
    @SerialName("dm_announcements")
    val dmAnnouncements: MutableList<String> = mutableListOf()

    companion object {
        fun empty(guildId: Snowflake) = GuildSettings(guildId)
    }

    fun getDmAnnouncementsString() = this.dmAnnouncements.asStringList()

    //TODO: Remove when old translation system is dropped
    fun getLocale(): Locale {

        return when (lang) {
            "ENGLISH" -> Locale.ENGLISH
            "JAPANESE" -> Locale.JAPANESE
            "PORTUGUESE" -> Locale.forLanguageTag("pt")
            "SPANISH" -> Locale.forLanguageTag("es")
            else -> Locale.ENGLISH
        }

    }
}
