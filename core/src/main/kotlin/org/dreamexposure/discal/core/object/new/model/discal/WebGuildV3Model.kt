package org.dreamexposure.discal.core.`object`.new.model.discal

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.`object`.new.GuildSettings

data class WebGuildV3Model(
    // General properties
    val id: Snowflake,
    val name: String,
    val iconUrl: String?,

    // Bot specific properties
    val botNickname: String?,

    // Properties specific to the user viewing this guild
    val userHasElevatedAccess: Boolean,
    val userHasDisCalControlRole: Boolean,

    // Nested guild objects
    val roles: List<WebRoleV3Model>,

    // Nested DisCal objects
    val settings: GuildSettings,
    val calendars: List<CalendarV3Model>,
)