package org.dreamexposure.discal.core.`object`.new.model.discord

import discord4j.common.util.Snowflake
import discord4j.discordjson.possible.Possible
import java.util.Optional

data class PartialGuildData(
    val id: Snowflake,
    val name: String,
    val icon: Optional<String>,
    val owner: Possible<Boolean>,
    val permissions: Possible<Long>,
)
