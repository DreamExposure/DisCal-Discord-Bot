package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.extensions.autocompleteSafe
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.springframework.stereotype.Component

@Component
class CalendarAutocompleteHandler(
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
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .get()

        val calendars = calendarService.getAllCalendars(settings.guildId)

        // I really wanted to filter by name, but it seems I'd need to accept string input which I really don't want to deal with :/
        val filtered = calendars
            .filter { it.metadata.number == input.toInt() }
            .ifEmpty { calendars }

        val toSend = filtered
            .subList(0, 25.coerceAtMost(filtered.size))
            .map {
                ApplicationCommandOptionChoiceData.builder()
                    .name("[${it.metadata.number}] ${it.name.autocompleteSafe(5)}")
                    .value(it.metadata.number)
                    .build()
            }

        event.respondWithSuggestions(toSend).awaitSingleOrNull()
    }
}