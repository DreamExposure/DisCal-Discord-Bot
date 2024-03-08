package org.dreamexposure.discal.core.`object`.new

import discord4j.common.util.Snowflake

data class WizardState<T>(
    val guildId: Snowflake,
    val userId: Snowflake,
    val editing: Boolean,
    val entity: T,
)
