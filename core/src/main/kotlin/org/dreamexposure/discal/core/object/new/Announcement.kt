package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.database.AnnouncementData
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.asSnowflake
import org.dreamexposure.discal.core.extensions.asStringListFromDatabase
import java.time.Duration

data class Announcement(
    val id: String = KeyGenerator.generateAnnouncementId(),
    val guildId: Snowflake,
    val calendarNumber: Int = 1,

    val type: Type = Type.UNIVERSAL,
    val modifier: Modifier = Modifier.BEFORE,
    val channelId: Snowflake,


    val subscribers: Subscribers = Subscribers(),
    val eventId: String = "N/a",
    val eventColor: EventColor = EventColor.NONE,

    val hoursBefore: Int = 0,
    val minutesBefore: Int = 0,

    val info: String = "None",
    val enabled: Boolean = true,
    val publish: Boolean = false,
) {
    constructor(data: AnnouncementData) : this(
        id = data.announcementId,
        guildId = data.guildId.asSnowflake(),
        calendarNumber = data.calendarNumber,

        type = Type.valueOf(data.announcementType),
        modifier = Modifier.valueOf(data.modifier),
        channelId = Snowflake.of(data.channelId),

        subscribers = Subscribers(
            roles = data.subscribersRole.asStringListFromDatabase(),
            users = data.subscribersUser.asStringListFromDatabase().map(Snowflake::of),
        ),
        eventId = data.eventId,
        eventColor = EventColor.fromNameOrHexOrId(data.eventColor),

        hoursBefore = data.hoursBefore,
        minutesBefore = data.minutesBefore,

        info = data.info,
        enabled = data.enabled,
        publish = data.publish,
    )

    fun getCalculatedTime(): Duration = Duration.ofHours(hoursBefore.toLong()).plusMinutes(minutesBefore.toLong())


    data class Subscribers(
        val roles: List<String> = listOf(),
        val users: List<Snowflake> = listOf(),
    )

    enum class Type {
        UNIVERSAL,
        SPECIFIC,
        COLOR,
        RECUR,
    }

    enum class Modifier {
        BEFORE,
        DURING,
        END,
    }
}
