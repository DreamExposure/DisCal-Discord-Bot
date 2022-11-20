package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.message.embed.DiscalEmbed
import org.springframework.stereotype.Component

@Component
class BotMentionListener: EventListener<MessageCreateEvent> {
    override suspend fun handle(event: MessageCreateEvent) {
        if (event.guildId.isPresent // in guild
            && !event.message.author.map(User::isBot).orElse(false) // Not from a bot
            && onlyMentionsBot(event.message)
        ) {
            val embed = event.guild.flatMap(DiscalEmbed::info).awaitSingle()
            val channel = event.message.channel.awaitSingle()

            channel.createMessage(embed).awaitSingleOrNull()
        }
    }

    private fun onlyMentionsBot(message: Message): Boolean {
        return (message.userMentionIds.size == 1 && message.userMentionIds.contains(message.client.selfId)) // Only bot user mentioned
            && message.roleMentionIds.isEmpty() // Does not mention any roles
            && !message.mentionsEveryone() // Does not mention everyone
    }
}

