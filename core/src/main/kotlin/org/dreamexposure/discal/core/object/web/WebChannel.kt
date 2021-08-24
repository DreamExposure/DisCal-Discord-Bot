package org.dreamexposure.discal.core.`object`.web

import discord4j.core.`object`.entity.channel.GuildMessageChannel
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer

@Serializable
data class WebChannel(
        @Serializable(with = LongAsStringSerializer::class)
        val id: Long,
        val name: String,
) {
    companion object {
        fun fromChannel(channel: GuildMessageChannel): WebChannel {
            return WebChannel(channel.id.asLong(), channel.name)
        }
    }
}
