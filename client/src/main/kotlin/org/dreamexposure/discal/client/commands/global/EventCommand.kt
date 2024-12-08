package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.enums.event.EventFrequency
import org.dreamexposure.discal.core.`object`.event.Recurrence
import org.dreamexposure.discal.core.`object`.new.Event
import org.dreamexposure.discal.core.`object`.new.EventWizardState
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.ImageValidator
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import java.time.*
import java.time.temporal.ChronoUnit

@Suppress("DuplicatedCode")
@Component
class EventCommand(
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
    private val embedService: EmbedService,
) : SlashCommand {
    override val name = "event"
    override val hasSubcommands = true
    override val ephemeral = true

    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return when (event.options[0].name) {
            "create" -> create(event, settings)
            "name" -> name(event, settings)
            "description" -> description(event, settings)
            "start" -> start(event, settings)
            "end" -> end(event, settings)
            "color" -> color(event, settings)
            "location" -> location(event, settings)
            "image" -> image(event, settings)
            "recur" -> recur(event, settings)
            "review" -> review(event, settings)
            "confirm" -> confirm(event, settings)
            "cancel" -> cancel(event, settings)
            "edit" -> edit(event, settings)
            "copy" -> copy(event, settings)
            "view" -> view(event, settings)
            "delete" -> delete(event, settings)
            else -> throw IllegalStateException("Invalid subcommand specified") // Never can reach this, makes compiler happy.
        }
    }

    private suspend fun create(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val name = event.options[0].getOption("name")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")
        val description = event.options[0].getOption("description")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")
        val location = event.options[0].getOption("location")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard already started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) return event.createFollowup(getMessage("error.wizard.started", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
            .awaitSingle()

        // Make sure calendar exists
        val calendar = calendarService.getCalendar(settings.guildId, calendarNumber)
        if (calendar == null) return event.createFollowup(getCommonMsg("error.notFound.calendar", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val newWizard = EventWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = false,
            entity = Event.PartialEvent(
                id = null,
                guildId = settings.guildId,
                calendarNumber = calendarNumber,
                name = name,
                description = description,
                location = location,
                color = EventColor.NONE,
                start = null,
                end = null,
                recur = false,
                recurrence = null,
                image = null,
                timezone = calendar.timezone,
            )
        )
        calendarService.putEventWizard(newWizard)

        return event.createFollowup(getMessage("create.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(newWizard, settings))
            .awaitSingle()
    }

    private suspend fun name(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val name = event.options[0].getOption("name")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .filter { !it.equals("N/a") || !it.equals("None") }
            .orElse("")

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(name = name))
        calendarService.putEventWizard(alteredWizard)

        return event.createFollowup(getMessage("name.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun description(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val description = event.options[0].getOption("description")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .filter { !it.equals("N/a") || !it.equals("None") }
            .orElse("")

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(description = description))
        calendarService.putEventWizard(alteredWizard)

        return event.createFollowup(getMessage("description.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun start(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val year = event.options[0].getOption("year")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map { it.coerceAtLeast(Year.MIN_VALUE).coerceAtMost(Year.MAX_VALUE) }
            .get()
        val month = event.options[0].getOption("month")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map(Month::of)
            .get()
        val day = event.options[0].getOption("day")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map { it.coerceAtLeast(1).coerceAtMost(month.maxLength()) }
            .get()
        val hour = event.options[0].getOption("hour")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(0)
        val minute = event.options[0].getOption("minute")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map { it.coerceAtLeast(0).coerceAtMost(59) }
            .orElse(0)
        val keepDuration = event.options[0].getOption("keep-duration")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(settings.eventKeepDuration)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        //Build date time object
        val start = ZonedDateTime.of(
            LocalDateTime.of(year, month, day, hour, minute),
            existingWizard.entity.timezone
        ).toInstant()

        return if (existingWizard.entity.end == null) {
            val modifiedWizard = existingWizard.copy(entity = existingWizard.entity.copy(start = start, end = start.plus(1, ChronoUnit.HOURS)))
            calendarService.putEventWizard(modifiedWizard)

            // Handle special messaging if event is scheduled for the past
            val message = if (modifiedWizard.entity.start!!.isAfter(Instant.now()))
                getMessage("start.success", settings)
            else getMessage("start.success.past", settings)

            event.createFollowup(message)
                .withEmbeds(embedService.eventWizardEmbed(modifiedWizard, settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else {
            // Event end already set, make sure everything is in order
            val originalDuration = if (existingWizard.entity.start != null) Duration.between(existingWizard.entity.start, existingWizard.entity.end) else null
            val shouldChangeDuration = keepDuration && originalDuration != null

            if (existingWizard.entity.end!!.isAfter(start) || shouldChangeDuration) {
                val modifiedEnd = if (shouldChangeDuration) start.plus(originalDuration) else existingWizard.entity.end
                val modifiedWizard = existingWizard.copy(entity = existingWizard.entity.copy(start = start, end = modifiedEnd))
                calendarService.putEventWizard(modifiedWizard)

                // Handle special messaging if event is scheduled for the past
                val message = if (modifiedWizard.entity.start!!.isAfter(Instant.now()))
                    getMessage("start.success", settings)
                else getMessage("start.success.past", settings)

                event.createFollowup(message)
                    .withEmbeds(embedService.eventWizardEmbed(modifiedWizard, settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            } else {
                // Event end cannot be before event start
                event.createFollowup(getMessage("start.failure.afterEnd", settings))
                    .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }
        }
    }

    private suspend fun end(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val year = event.options[0].getOption("year")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map { it.coerceAtLeast(Year.MIN_VALUE).coerceAtMost(Year.MAX_VALUE) }
            .get()
        val month = event.options[0].getOption("month")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map(Month::of)
            .get()
        val day = event.options[0].getOption("day")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map { it.coerceAtLeast(1).coerceAtMost(month.maxLength()) }
            .get()
        val hour = event.options[0].getOption("hour")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(0)
        val minute = event.options[0].getOption("minute")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map { it.coerceAtLeast(0).coerceAtMost(59) }
            .orElse(0)
        val keepDuration = event.options[0].getOption("keep-duration")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(settings.eventKeepDuration)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        //Build date time object
        val end = ZonedDateTime.of(
            LocalDateTime.of(year, month, day, hour, minute),
            existingWizard.entity.timezone
        ).toInstant()


        return if (existingWizard.entity.start == null) {
            // Add default start time to 1 hour before end.
            val modifiedWizard = existingWizard.copy(entity = existingWizard.entity.copy(end = end, start = end.minus(1, ChronoUnit.HOURS)))
            calendarService.putEventWizard(modifiedWizard)

            // Handle special messaging if event is scheduled for the past
            val message = if (modifiedWizard.entity.end!!.isAfter(Instant.now()))
                getMessage("end.success", settings)
            else getMessage("end.success.past", settings)

            event.createFollowup(message)
                .withEmbeds(embedService.eventWizardEmbed(modifiedWizard, settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else {
            // Event start already set, make sure everything is in order
            val originalDuration = if (existingWizard.entity.end != null) Duration.between(existingWizard.entity.start, existingWizard.entity.end) else null
            val shouldChangeDuration = keepDuration && originalDuration != null


            if (existingWizard.entity.start!!.isBefore(end) || shouldChangeDuration) {
                val modifiedStart = if (shouldChangeDuration) end.minus(originalDuration) else existingWizard.entity.start
                val modifiedWizard = existingWizard.copy(entity = existingWizard.entity.copy(start = modifiedStart, end = end))
                calendarService.putEventWizard(modifiedWizard)

                // Handle special messaging if event is scheduled for the past
                val message = if (modifiedWizard.entity.end!!.isAfter(Instant.now()))
                    getMessage("end.success", settings)
                else getMessage("end.success.past", settings)

                event.createFollowup(message)
                    .withEmbeds(embedService.eventWizardEmbed(modifiedWizard, settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()

            } else {
                // Event start cannot be after event end
                event.createFollowup(getMessage("end.failure.beforeStart", settings))
                    .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }
        }
    }

    private suspend fun color(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val color = event.options[0].getOption("color")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map(EventColor.Companion::fromId)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(color = color))
        calendarService.putEventWizard(alteredWizard)

        return event.createFollowup(getMessage("color.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun location(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val location = event.options[0].getOption("location")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .filter { !it.equals("N/a") || !it.equals("None") }
            .orElse("")

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(location = location))
        calendarService.putEventWizard(alteredWizard)

        return event.createFollowup(getMessage("location.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun image(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val image = event.options[0].getOption("image")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if provided link is actually an image we can consume
        val isValidImage = ImageValidator.validate(image, settings.patronGuild || settings.devGuild).awaitSingle()
        if (!isValidImage) return event.createFollowup(getMessage("image.failure", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
            .awaitSingle()

        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(image = image))
        calendarService.putEventWizard(alteredWizard)

        return event.createFollowup(getMessage("image.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun recur(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val shouldRecur = event.options[0].getOption("recur")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(true)
        val frequency = event.options[0].getOption("frequency")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(EventFrequency.Companion::fromValue)
            .orElse(EventFrequency.WEEKLY)
        val interval = event.options[0].getOption("interval")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val count = event.options[0].getOption("count")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(-1)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard already started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val modifiedWizard = if (shouldRecur)
            existingWizard.copy(entity = existingWizard.entity.copy(recur = true, recurrence = Recurrence(frequency, interval, count)))
        else existingWizard.copy(entity = existingWizard.entity.copy(recur = false, recurrence = null))
        calendarService.putEventWizard(modifiedWizard)

        // Handle message
        val message = if (shouldRecur)
            getMessage("recur.success.enable", settings)
        else getMessage("recur.success.disable", settings)

        return event.createFollowup(message)
            .withEmbeds(embedService.eventWizardEmbed(modifiedWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        return event.createFollowup()
            .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not yet started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Validate that nothing required is missing
        if (existingWizard.entity.start == null || existingWizard.entity.end == null) {
            return event.createFollowup(getMessage("confirm.failure.missing.time", settings))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        // TODO: Should probably wrap this in a try/catch
        val confirmedEvent = if (existingWizard.editing) calendarService.updateEvent(
            guildId = existingWizard.guildId,
            existingWizard.entity.calendarNumber,
            spec = Event.UpdateSpec(
                id = existingWizard.entity.id!!,
                name = existingWizard.entity.name,
                description = existingWizard.entity.description,
                start = existingWizard.entity.start!!,
                end = existingWizard.entity.end!!,
                color = existingWizard.entity.color,
                location = existingWizard.entity.location,
                image = existingWizard.entity.image,
                recur = existingWizard.entity.recur,
                recurrence = existingWizard.entity.recurrence,
            )
        ) else calendarService.createEvent(
            guildId = existingWizard.guildId,
            existingWizard.entity.calendarNumber,
            spec = Event.CreateSpec(
                name = existingWizard.entity.name,
                description = existingWizard.entity.description,
                start = existingWizard.entity.start!!,
                end = existingWizard.entity.end!!,
                color = existingWizard.entity.color,
                location = existingWizard.entity.location,
                image = existingWizard.entity.image,
                recur = existingWizard.entity.recur,
                recurrence = existingWizard.entity.recurrence,
            )
        )

        val message = if (existingWizard.editing)
            getMessage("confirm.success.edit", settings)
        else getMessage("confirm.success.create", settings)

        // Basically, since the first followup is just editing the original, what if I delete the original defer message and then create a non-ephemeral followup???
        event.interactionResponse.deleteInitialResponse().awaitSingleOrNull()

        return event.createFollowup(message)
            .withEphemeral(false)
            .withEmbeds(embedService.fullEventEmbed(confirmedEvent, settings))
            .awaitSingle()
    }

    private suspend fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        calendarService.cancelEventWizard(settings.guildId, event.interaction.user.id)

        return event.createFollowup(getMessage("cancel.success", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun edit(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard already started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) return event.createFollowup(getMessage("error.wizard.started", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
            .awaitSingle()

        // Make sure calendar exists
        val calendar = calendarService.getCalendar(settings.guildId, calendarNumber)
        if (calendar == null) return event.createFollowup(getCommonMsg("error.notFound.calendar", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Make sure event actually exists
        val existingEvent = calendarService.getEvent(settings.guildId, calendarNumber, eventId)
        if (existingEvent == null) return event.createFollowup(getCommonMsg("error.notFound.event", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val newWizard = EventWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = true,
            entity = Event.PartialEvent(
                id = existingEvent.id,
                guildId = settings.guildId,
                calendarNumber = existingEvent.calendarNumber,
                name = existingEvent.name,
                description = existingEvent.description,
                location = existingEvent.location,
                color = existingEvent.color,
                start = existingEvent.start,
                end = existingEvent.end,
                recur = existingEvent.recur,
                recurrence = if (existingEvent.recur) existingEvent.recurrence else null,
                image = existingEvent.image,
                timezone = existingEvent.timezone,
            )
        )
        calendarService.putEventWizard(newWizard)

        return event.createFollowup(getMessage("edit.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(newWizard, settings))
            .awaitSingle()
    }

    private suspend fun copy(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val targetCalendarNumber = event.options[0].getOption("target")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(calendarNumber)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard already started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) return event.createFollowup(getMessage("error.wizard.started", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
            .awaitSingle()

        // Make sure source calendar exists
        val calendar = calendarService.getCalendar(settings.guildId, calendarNumber)
        if (calendar == null) return event.createFollowup(getCommonMsg("error.notFound.calendar", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Make sure target calendar exists
        val targetCalendar = calendarService.getCalendar(settings.guildId, targetCalendarNumber) ?: calendar

        // Make sure event actually exists
        val existingEvent = calendarService.getEvent(settings.guildId, calendarNumber, eventId)
        if (existingEvent == null) return event.createFollowup(getCommonMsg("error.notFound.event", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val newWizard = EventWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = false,
            entity = Event.PartialEvent(
                id = null,
                guildId = settings.guildId,
                calendarNumber = targetCalendar.metadata.number,
                name = existingEvent.name,
                description = existingEvent.description,
                location = existingEvent.location,
                color = existingEvent.color,
                start = existingEvent.start,
                end = existingEvent.end,
                recur = existingEvent.recur,
                recurrence = if (existingEvent.recur) existingEvent.recurrence else null,
                image = existingEvent.image,
                timezone = targetCalendar.timezone,
            )
        )
        calendarService.putEventWizard(newWizard)

        return event.createFollowup(getMessage("copy.success", settings))
            .withEmbeds(embedService.eventWizardEmbed(newWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun view(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val calendarEvent = calendarService.getEvent(settings.guildId, calendarNumber, eventId)
        return if (calendarEvent != null) {
            // Basically, since the first followup is just editing the original, what if I delete the original defer message and then create a non-ephemeral followup???
            event.interactionResponse.deleteInitialResponse().awaitSingleOrNull()

            event.createFollowup()
                .withEphemeral(false)
                .withEmbeds(embedService.fullEventEmbed(calendarEvent, settings))
                .awaitSingle()
        } else {
            event.createFollowup(getCommonMsg("error.notFound.event", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
        }
    }

    private suspend fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Message{
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        calendarService.deleteEvent(settings.guildId, calendarNumber, eventId)

        return event.createFollowup(getMessage("delete.success", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}
