package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import java.time.Instant
import java.util.*

@Serializable
abstract class Pre(
    @Transient
    open val guildId: Snowflake = Snowflake.of(0),

    @Transient
    open val editing: Boolean = false,
) {
    @Transient
    var lastEdit: Instant = Instant.now()

    open fun generateWarnings(settings: GuildSettings): List<String> = Collections.emptyList()
}
