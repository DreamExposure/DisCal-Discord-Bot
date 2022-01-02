package org.dreamexposure.discal.core.`object`.announcement

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.Pre
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.enums.announcement.AnnouncementModifier
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer
import org.dreamexposure.discal.core.utils.getEmbedMessage

@Serializable
data class Announcement(
    @Serializable(with = SnowflakeAsStringSerializer::class)
    @SerialName("guild_id")
    override val guildId: Snowflake,

    val id: String = KeyGenerator.generateAnnouncementId(),

    @Transient
    override val editing: Boolean = false,
) : Pre(guildId) {

    @SerialName("subscriber_roles")
    val subscriberRoleIds: MutableList<String> = mutableListOf()

    @SerialName("subscriber_users")
    val subscriberUserIds: MutableList<String> = arrayListOf()

    @SerialName("channel_id")
    var announcementChannelId: String = "N/a"
    var type = AnnouncementType.UNIVERSAL
    var modifier = AnnouncementModifier.BEFORE

    @SerialName("calendar_number")
    var calendarNumber: Int = 1

    @SerialName("event_id")
    var eventId: String = "N/a"

    @SerialName("event_color")
    var eventColor = EventColor.NONE

    @SerialName("hours")
    var hoursBefore = 0

    @SerialName("minutes")
    var minutesBefore = 0
    var info = "None"

    var enabled = true

    var publish = false

    fun setSubscriberRoleIdsFromString(subList: String) {
        this.subscriberRoleIds += subList.split(",").filter(String::isNotBlank)
    }

    fun setSubscriberUserIdsFromString(subList: String) {
        this.subscriberUserIds += subList.split(",").filter(String::isNotBlank)
    }

    fun hasRequiredValues(): Boolean {
        return !((this.type == AnnouncementType.SPECIFIC || this.type == AnnouncementType.RECUR) && this.eventId == "N/a")
            && this.announcementChannelId != "N/a"
    }

    override fun generateWarnings(settings: GuildSettings): List<String> {
        val warnings = mutableListOf<String>()

        if ((this.type == AnnouncementType.SPECIFIC || this.type == AnnouncementType.RECUR) && this.eventId == "N/a")
            warnings.add(getEmbedMessage("announcement", "warning.wizard.eventId", settings))
        if (this.type == AnnouncementType.COLOR && this.eventColor == EventColor.NONE)
            warnings.add(getEmbedMessage("announcement", "warning.wizard.color", settings))
        if (this.minutesBefore + (this.hoursBefore * 60) < 5)
            warnings.add(getEmbedMessage("announcement", "warning.wizard.time", settings))
        if (this.calendarNumber > settings.maxCalendars)
            warnings.add(getEmbedMessage("announcement", "warning.wizard.calNum", settings))

        return warnings
    }
}
