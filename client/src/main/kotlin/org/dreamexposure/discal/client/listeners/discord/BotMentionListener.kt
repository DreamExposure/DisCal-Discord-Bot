package org.dreamexposure.discal.client.listeners.discord

import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.AnnouncementService
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.springframework.stereotype.Component

@Component
class BotMentionListener(
    private val announcementService: AnnouncementService,
    private val calendarService: CalendarService,
    private val embedService: EmbedService,
): EventListener<MessageCreateEvent> {
    override suspend fun handle(event: MessageCreateEvent) {
        if (event.guildId.isPresent // in guild
            && !event.message.author.map(User::isBot).orElse(false) // Not from a bot
            && onlyMentionsBot(event.message)
        ) {
            val settings = event.guild.flatMap(Guild::getSettings).awaitSingle()
            val announcementCount = announcementService.getAnnouncementCount()
            val calendarCount = calendarService.getCalendarCount()
            val channel = event.message.channel.awaitSingle()

            val embed = embedService.discalInfoEmbed(settings, calendarCount, announcementCount)

            channel.createMessage(embed).awaitSingleOrNull()
        }
    }

    private fun onlyMentionsBot(message: Message): Boolean {
        return (message.userMentionIds.size == 1 && message.userMentionIds.contains(message.client.selfId)) // Only bot user mentioned
            && message.roleMentionIds.isEmpty() // Does not mention any roles
            && !message.mentionsEveryone() // Does not mention everyone
    }
}

