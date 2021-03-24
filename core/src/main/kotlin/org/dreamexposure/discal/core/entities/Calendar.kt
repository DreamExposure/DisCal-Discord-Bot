package org.dreamexposure.discal.core.entities

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.BotSettings

interface Calendar {
    /**
     * The ID of the guild this calendar belongs to.
     */
    val guildId: Snowflake

    /**
     * The calendar's ID, usually but not always the same as the calendar's address.
     * Use this for unique identification of the calendar
     */
    val calendarId: String

    /**
     * The calendar's address, usually but not always the same as the calendar's ID.
     * This property may not be unique
     */
    val calendarAddress: String

    /**
     * The relative number of the calendar in order it was created in for the guild.
     * Calendar number <code>1</code> is the "main" calendar for the guild, used as the default.
     */
    val calendarNumber: Int

    /**
     * Whether or not the calendar is "external" meaning it is owned by a user account.
     * This does not indicate the service used to host the calendar, but whether it is owned by DisCal, or a user.
     */
    val external: Boolean

    /**
     * The name of the calendar. Renamed from "summary" to be more user-friendly and clear.
     */
    var name: String

    /**
     * A longer form description of the calendar.
     * If this is not present, an empty string is returned
     */
    var description: String

    /**
     * The timezone the calendar uses. Normally in its longer name, such as "America/New_York"
     */
    var timezone: String

    /**
     * Gets the link to the calendar on the official bot website.
     */
    fun getLink(): String {
        return if (BotSettings.PROFILE.get().equals("TEST", true))
            "https://dev.discalbot.com/embed/${guildId.asString()}/calendar/$calendarNumber"
        else
            "https://dev.discalbot.com/embed/${guildId.asString()}/calendar/$calendarNumber"
    }

    //Reactive - Events
    //TODO: Get events by id/next n events/range/month/etc
    //TODO: Create/update/delete event
    //TODO: Announcements?
}
