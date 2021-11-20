package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import org.dreamexposure.discal.client.message.embed.DiscalEmbed
import org.dreamexposure.discal.core.`object`.BotSettings.ID
import reactor.core.publisher.Mono

object BotMentionListener {

    fun handle(event: MessageCreateEvent): Mono<Void> {
        if (event.guildId.isPresent //in guild
            && !event.message.author.map(User::isBot).orElse(false) // not from a bot
            && containsMention(event.message) // mentions the bot
        ) {
            return event.guild.flatMap(DiscalEmbed::info).flatMap { embed ->
                event.message.channel.flatMap { channel ->
                    channel.createMessage(embed).then()
                }
            }
        }
        //Ignore everything else
        return Mono.empty()
    }

    private fun containsMention(message: Message): Boolean {
        return message.userMentionIds.size == 1 && // only 1 user mentioned
                message.roleMentionIds.isEmpty() && // no roles mentioned
                message.userMentionIds.contains(message.client.selfId) && // only the bot is mentioned
                !message.mentionsEveryone() // no @everyone mentioned
    }

    private fun containsMention(msg: String) = msg == "<@${ID.get()}>" || msg == "<@!${ID.get()}>"
}

