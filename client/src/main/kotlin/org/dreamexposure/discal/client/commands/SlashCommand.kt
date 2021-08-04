package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.dreamexposure.discal.core.`object`.GuildSettings
import reactor.core.publisher.Mono

interface SlashCommand {
    val name: String

    val ephemeral: Boolean

    fun handle(event: SlashCommandEvent, settings: GuildSettings): Mono<Void>
}
