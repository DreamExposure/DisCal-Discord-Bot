package org.dreamexposure.discal.core.`object`

import discord4j.common.util.Snowflake
import java.time.Instant

abstract class Pre(
        open val guildId: Snowflake
) {
    var lastEdit: Instant = Instant.now()
}
