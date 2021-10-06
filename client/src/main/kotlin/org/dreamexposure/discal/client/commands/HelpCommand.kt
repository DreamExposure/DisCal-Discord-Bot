package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HelpCommand : SlashCommand {
    override val name = "help"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return event.followupEphemeral(
            getMessage("error.workInProgress", settings, "${BotSettings.BASE_URL.get()}/commands")
        )
    }
}
