package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.MessageSourceLoader
import reactor.core.publisher.Mono

interface SlashCommand {
    val name: String

    val ephemeral: Boolean

    fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void>

    fun getMessage(key: String, settings: GuildSettings, vararg args: String): String {
        val src = MessageSourceLoader.getSourceByPath("command/$name/$name")

        return src.getMessage(key, args, settings.getLocale())
    }
}
