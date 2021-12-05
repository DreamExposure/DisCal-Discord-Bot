package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import java.time.Instant
import java.util.*

abstract class Pre(
        open val guildId: Snowflake
) {
    var lastEdit: Instant = Instant.now()

    open fun generateWarnings(settings: GuildSettings): List<String> = Collections.emptyList()
}
