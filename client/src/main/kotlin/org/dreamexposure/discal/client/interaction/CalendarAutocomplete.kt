package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.extensions.autocompleteSafe
import org.dreamexposure.discal.core.extensions.toMarkdown
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.springframework.stereotype.Component

@Component
class CalendarAutocomplete(
    private val calendarService: CalendarService,
): InteractionHandler<ChatInputAutoCompleteEvent> {
    override val ids = arrayOf(
        "calendar.calendar",
        "announcement.calendar",
        "event.calendar",
        "event.target",
        "displaycal.calendar",
        "events.calendar",
        "rsvp.calendar",
        "linkcal.calendar",
        "time.calendar",
        )
    override val ephemeral = true

    override suspend fun handle(event: ChatInputAutoCompleteEvent, settings: GuildSettings) {
        val input = event.focusedOption.value
            .map(ApplicationCommandInteractionOptionValue::getRaw)
            .get()

        val calendars = calendarService.getAllCalendars(settings.guildId)

        val filtered = calendars
            .filter {
                it.name.contains(input, ignoreCase = true)
                    || it.description.contains(input, ignoreCase = true)
                    || it.metadata.number == input.toIntOrNull()
            }.ifEmpty { calendars }

        val toSend = filtered
            .subList(0, 25.coerceAtMost(filtered.size))
            .map {
                ApplicationCommandOptionChoiceData.builder()
                    .name("[${it.metadata.number}] ${it.name.toMarkdown().autocompleteSafe(6)}")
                    .value(it.metadata.number)
                    .build()
            }

        event.respondWithSuggestions(toSend).awaitSingleOrNull()
    }
}