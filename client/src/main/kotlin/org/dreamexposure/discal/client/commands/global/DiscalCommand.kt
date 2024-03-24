package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.AnnouncementService
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.extensions.discord4j.followup
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.springframework.stereotype.Component

@Component
class DiscalCommand(
    private val announcementService: AnnouncementService,
    private val calendarService: CalendarService,
    private val embedService: EmbedService,
) : SlashCommand {
    override val name = "discal"
    override val hasSubcommands = false
    override val ephemeral = false

    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val announcementCount = announcementService.getAnnouncementCount()
        val calendarCount = calendarService.getCalendarCount()

        val embed = embedService.discalInfoEmbed(settings, calendarCount, announcementCount)

        return event.followup(embed).awaitSingle()
    }
}
