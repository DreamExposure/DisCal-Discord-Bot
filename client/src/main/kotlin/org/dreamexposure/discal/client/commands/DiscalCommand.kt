package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.dreamexposure.discal.client.message.Responder
import org.dreamexposure.discal.client.message.embed.DiscalEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DiscalCommand : SlashCommand {
    override val name = "discal"
    override val ephemeral = false

    override fun handle(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        return event.interaction.guild
              .flatMap(DiscalEmbed::info)
              .flatMap { Responder.followup(event, it) }
              .then()
    }
}
