package org.dreamexposure.discal.core.`object`.network.google

import discord4j.core.`object`.entity.User
import org.dreamexposure.discal.core.`object`.GuildSettings

data class Poll(
        val User: User,

        val settings: GuildSettings,

        var interval: Int,

        val expiresInt: Int,

        var remainingSeconds: Int,

        val deviceCode: String,
)
