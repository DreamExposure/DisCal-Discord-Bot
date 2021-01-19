package org.dreamexposure.discal.core.`object`.web

import discord4j.core.`object`.entity.channel.GuildMessageChannel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import org.dreamexposure.discal.core.`object`.GuildSettings

@Serializable
data class WebChannel(
        @Serializable(with = LongAsStringSerializer::class)
        val id: Long,
        val name: String,
        @SerialName("discal_channel")
        val discalChannel: Boolean,
) {
    companion object {
        fun fromChannel(channel: GuildMessageChannel, settings: GuildSettings): WebChannel {
            val dc = settings.discalChannel.equals(channel.id.asString(), true)

            return WebChannel(channel.id.asLong(), channel.name, dc)
        }

        fun all(settings: GuildSettings): WebChannel {
            val dc = settings.discalChannel.equals("all", true)

            return WebChannel(0, "All Channels", dc)
        }
    }
}
