package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class LinkCalendarCommand(
    private val embedService: EmbedService,
    private val calendarService: CalendarService,
) : SlashCommand {
    override val name = "linkcal"
    override val hasSubcommands = false
    override val ephemeral = false

    private val OVERVIEW_EVENT_COUNT = Config.CALENDAR_OVERVIEW_DEFAULT_EVENT_COUNT.getInt()


    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val showOverview = event.getOption("overview")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .orElse(true)
        val calendarNumber = event.getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val calendar = calendarService.getCalendar(settings.guildId, calendarNumber)
        if (calendar == null) {
            return event.createFollowup(getCommonMsg("error.notFound.calendar", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
        }

        val events = if (showOverview)
            calendarService.getUpcomingEvents(settings.guildId, calendarNumber, OVERVIEW_EVENT_COUNT)
        else null

        return event.createFollowup()
            .withEmbeds(embedService.linkCalendarEmbed(calendar, events))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}
