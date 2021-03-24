package org.dreamexposure.discal.core.entities.google

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.entities.Calendar

class GoogleCalendar(
        override val guildId: Snowflake,
        override val calendarId: String,
        override val calendarAddress: String,
        override val calendarNumber: Int,
        override val external: Boolean
) : Calendar {

    override var name: String = ""
    override var description: String = ""
    override var timezone: String = ""
}
