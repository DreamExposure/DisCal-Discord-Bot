package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.springframework.stereotype.Component

@Component
class HelpCommand : SlashCommand {
    override val name = "help"
    override val ephemeral = true

    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return event.followupEphemeral(
            getMessage("error.workInProgress", settings, "${Config.URL_BASE.getString()}/commands")
        ).awaitSingle()
    }
}
