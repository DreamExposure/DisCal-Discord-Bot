package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.MessageSourceLoader
import reactor.core.publisher.Mono

interface SlashCommand {
    val name: String
    val hasSubcommands: Boolean
    val ephemeral: Boolean

    fun shouldDefer(event: ChatInputInteractionEvent): Boolean = true

    @Deprecated("Use new handleSuspend for K-coroutines")
    fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return mono { suspendHandle(event, settings) }
    }

    suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return handle(event, settings).awaitSingle()
    }

    fun getMessage(key: String, settings: GuildSettings, vararg args: String): String {
        val src = MessageSourceLoader.getSourceByPath("command/$name/$name")

        return src.getMessage(key, args, settings.locale)
    }
}
