package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactive.awaitSingle
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Component
class EventsCommand(
    private val calendarService: CalendarService,
    private val embedService: EmbedService,
) : SlashCommand {
    override val name = "events"
    override val hasSubcommands = true
    override val ephemeral = false


    override suspend fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return when (event.options[0].name) {
            "upcoming" -> upcomingEvents(event, settings)
            "ongoing" -> ongoingEvents(event, settings)
            "today" -> eventsToday(event, settings)
            "range" -> eventsRange(event, settings)
            else -> throw IllegalStateException("Invalid subcommand specified")
        }
    }

    private suspend fun upcomingEvents(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val amount = event.options[0].getOption("amount")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
            .coerceIn(1, 15)

        val events = calendarService.getUpcomingEvents(settings.guildId, calendarNumber, amount)

        return if (events.isEmpty()) {
            event.createFollowup(getMessage("upcoming.success.none", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else if (events.size == 1) {
            event.createFollowup(getMessage("upcoming.success.one", settings))
                .withEmbeds(embedService.fullEventEmbed(events[0], settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else {
            val response = event.createFollowup(getMessage("upcoming.success.many", settings, "${events.size}"))
                .withEphemeral(ephemeral)
                .awaitSingle()

            events.forEach {
                event.createFollowup()
                .withEmbeds(embedService.condensedEventEmbed(it, settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }

            response
        }
    }

    private suspend fun ongoingEvents(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val events = calendarService.getOngoingEvents(settings.guildId, calendarNumber)

        return if (events.isEmpty()) {
            event.createFollowup(getMessage("ongoing.success.none", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else if (events.size == 1) {
            event.createFollowup(getMessage("ongoing.success.one", settings))
                .withEmbeds(embedService.fullEventEmbed(events[0], settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else {
            val response = event.createFollowup(getMessage("ongoing.success.many", settings, "${events.size}"))
                .withEphemeral(ephemeral)
                .awaitSingle()

            events.forEach {
                event.createFollowup()
                    .withEmbeds(embedService.condensedEventEmbed(it, settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }

            response
        }
    }

    private suspend fun eventsToday(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val events = calendarService.getEventsInNext24HourPeriod(settings.guildId, calendarNumber, Instant.now())

        return if (events.isEmpty()) {
            event.createFollowup(getMessage("today.success.none", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else if (events.size == 1) {
            event.createFollowup(getMessage("today.success.one", settings))
                .withEmbeds(embedService.fullEventEmbed(events[0], settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else {
            val response = event.createFollowup(getMessage("today.success.many", settings, "${events.size}"))
                .withEphemeral(ephemeral)
                .awaitSingle()

            events.forEach {
                event.createFollowup()
                    .withEmbeds(embedService.condensedEventEmbed(it, settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }

            response
        }
    }

    private suspend fun eventsRange(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val startInput = event.options[0].getOption("start")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val endInput = event.options[0].getOption("end")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        // In order to parse the inputs with timezone, we need to fetch the calendar
        val calendar = calendarService.getCalendar(settings.guildId, calendarNumber)
        if (calendar == null) {
            return event.createFollowup(getCommonMsg("error.notFound.calendar", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
        }

        try {
            val start = LocalDate.parse(startInput, formatter).atStartOfDay(calendar.timezone).toInstant()
            val end = LocalDate.parse(endInput, formatter).plusDays(1).atStartOfDay(calendar.timezone).toInstant()

            val events = calendarService.getEventsInTimeRange(settings.guildId, calendarNumber, start, end)

            return if (events.isEmpty()) {
                event.createFollowup(getMessage("range.success.none", settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            } else if (events.size == 1) {
                event.createFollowup(getMessage("range.success.one", settings))
                    .withEmbeds(embedService.fullEventEmbed(events[0], settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            } else if (events.size > 15) {
                event.createFollowup(getMessage("range.success.tooMany", settings, "${events.size}", calendar.link))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            } else {
                val response = event.createFollowup(getMessage("range.success.many", settings, "${events.size}"))
                    .withEphemeral(ephemeral)
                    .awaitSingle()

                events.forEach {
                    event.createFollowup()
                        .withEmbeds(embedService.condensedEventEmbed(it, settings))
                        .withEphemeral(ephemeral)
                        .awaitSingle()
                }

                response
            }
        } catch (_: DateTimeParseException) {
            return event.createFollowup(getCommonMsg("error.format.date", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
        }
    }
}
