package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.Message
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.MessageSourceLoader

interface SlashCommand {
    val name: String
    val hasSubcommands: Boolean
    val ephemeral: Boolean

    fun shouldDefer(event: ChatInputInteractionEvent): Boolean = true


    suspend fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Message

    fun getMessage(key: String, settings: GuildSettings, vararg args: String): String {
        val src = MessageSourceLoader.getSourceByPath("command/$name/$name")

        return src.getMessage(key, args, settings.locale)
    }
}
