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
    val eventId: String? = null,
    val eventColor: EventColor = EventColor.NONE,

    val hoursBefore: Int = 0,
    val minutesBefore: Int = 0,

    val info: String? = null,
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
            roles = data.subscribersRole.asStringListFromDatabase().toSet(),
            users = data.subscribersUser.asStringListFromDatabase().map(Snowflake::of).toSet(),
        ),
        eventId = if (data.eventId.isBlank() || data.eventId.equals("N/a", ignoreCase = true)) null else data.eventId,
        eventColor = EventColor.fromNameOrHexOrId(data.eventColor),

        hoursBefore = data.hoursBefore,
        minutesBefore = data.minutesBefore,

        info = if (data.info.isBlank() || data.info.equals("None", ignoreCase = true)) null else data.info,
        enabled = data.enabled,
        publish = data.publish,
    )

    fun getCalculatedTime(): Duration = Duration.ofHours(hoursBefore.toLong()).plusMinutes(minutesBefore.toLong())


    ////////////////////////////
    ////// Nested classes //////
    ////////////////////////////
    data class Subscribers(
        val roles: Set<String> = setOf(),
        val users: Set<Snowflake> = setOf(),
    ) {
        fun buildMentions(): String {
            if (users.isEmpty() && roles.isEmpty()) return ""

            val userMentions = users.map { "<@${it.asLong()}> " }

            val roleMentions = roles.map {
                if (it.equals("everyone", true)) "@everyone "
                else if (it.equals("here", true)) "@here "
                else "<@&$it> "
            }

            return StringBuilder()
                .append("Subscribers: ")
                .append(*userMentions.toTypedArray())
                .append(*roleMentions.toTypedArray())
                .toString()
        }
    }

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
