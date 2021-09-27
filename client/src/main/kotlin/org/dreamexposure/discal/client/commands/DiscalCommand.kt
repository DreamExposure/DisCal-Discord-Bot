package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.message.embed.DiscalEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.followup
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DiscalCommand : SlashCommand {
    override val name = "discal"
    override val ephemeral = false

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        return event.interaction.guild
              .flatMap(DiscalEmbed::info)
              .flatMap(event::followup)
              .then()
    }
}
