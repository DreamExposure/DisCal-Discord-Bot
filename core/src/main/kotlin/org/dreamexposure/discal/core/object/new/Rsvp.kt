package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.database.RsvpData
import org.dreamexposure.discal.core.extensions.asInstantMilli
import org.dreamexposure.discal.core.extensions.asSnowflake
import org.dreamexposure.discal.core.extensions.asStringListFromDatabase
import java.time.Instant

data class Rsvp(
    val guildId: Snowflake,
    val eventId: String,
    val calendarNumber: Int = 1,

    val eventEnd: Instant = Instant.ofEpochMilli(0),

    val goingOnTime: Set<Snowflake> = setOf(),
    val goingLate: Set<Snowflake> = setOf(),
    val notGoing: Set<Snowflake> = setOf(),
    val undecided: Set<Snowflake> = setOf(),
    val waitlist: List<Snowflake> = listOf(),

    val limit: Int = -1,
    val role: Snowflake? = null,
) {

    constructor(data: RsvpData) : this(
        guildId = data.guildId.asSnowflake(),
        eventId = data.eventId,
        calendarNumber = data.calendarNumber,

        eventEnd = data.eventEnd.asInstantMilli(),

        goingOnTime = data.goingOnTime.asStringListFromDatabase().map(Snowflake::of).toSet(),
        goingLate = data.goingLate.asStringListFromDatabase().map(Snowflake::of).toSet(),
        notGoing = data.notGoing.asStringListFromDatabase().map(Snowflake::of).toSet(),
        undecided = data.undecided.asStringListFromDatabase().map(Snowflake::of).toSet(),
        waitlist = data.waitlist.asStringListFromDatabase().map(Snowflake::of),

        limit = data.rsvpLimit,
        role = data.rsvpRole?.asSnowflake(),
    )

    fun getCurrentCount() = this.goingOnTime.size + this.goingLate.size

    fun hasRoom() = limit < 0 || getCurrentCount() < limit

    fun hasRoom(userId: Snowflake) = hasRoom() || (goingOnTime.contains(userId) || goingLate.contains(userId))

    fun copyWithUserStatus(
        userId: Snowflake,
        goingOnTime: Set<Snowflake>? = null,
        goingLate: Set<Snowflake>? = null,
        notGoing: Set<Snowflake>? = null,
        undecided: Set<Snowflake>? = null,
        waitlist: List<Snowflake>? = null,
    ): Rsvp {
        return this.copy(
            goingOnTime = goingOnTime ?: (this.goingOnTime - userId),
            goingLate = goingLate ?: (this.goingLate - userId),
            notGoing = notGoing ?: (this.notGoing - userId),
            undecided = undecided ?: (this.undecided - userId),
            waitlist = waitlist ?: (this.waitlist - userId),
        )
    }

}
