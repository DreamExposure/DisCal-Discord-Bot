package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.Message
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HelpCommand : SlashCommand {
    override val name = "help"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return event.followupEphemeral(
            getMessage("error.workInProgress", settings, "${Config.URL_BASE.getString()}/commands")
        )
    }
}
