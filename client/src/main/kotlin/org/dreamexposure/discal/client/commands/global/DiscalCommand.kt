package org.dreamexposure.discal.client.commands.global

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.client.message.embed.DiscalEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.followup
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DiscalCommand : SlashCommand {
    override val name = "discal"
    override val ephemeral = false

    @Deprecated("Use new handleSuspend for K-coroutines")
    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return event.interaction.guild
              .flatMap(DiscalEmbed::info)
              .flatMap(event::followup)
    }
}
