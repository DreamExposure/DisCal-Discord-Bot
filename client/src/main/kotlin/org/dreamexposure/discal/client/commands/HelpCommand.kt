package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.dreamexposure.discal.client.message.Responder
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HelpCommand : SlashCommand {
    override val name = "help"
    override val ephemeral = true

    override fun handle(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        return Responder.followupEphemeral(
              event,
              getMessage("error.workInProgress", settings, "${BotSettings.BASE_URL.get()}/commands")
        ).then()
    }
}
