package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.ComponentService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.extensions.toZoneId
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.CalendarMetadata
import org.dreamexposure.discal.core.`object`.new.CalendarWizardState
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId

@Component
class CalendarCommand(
    private val calendarService: CalendarService,
    private val permissionService: PermissionService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
) : SlashCommand {
    override val name = "calendar"
    override val hasSubcommands = true
    override val ephemeral = true
    private val OVERVIEW_EVENT_COUNT = Config.CALENDAR_OVERVIEW_DEFAULT_EVENT_COUNT.getInt()

    override suspend fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return when (event.options[0].name) {
            "view" -> view(event, settings)
            "list" -> list(event, settings)
            "create" -> create(event, settings)
            "name" -> name(event, settings)
            "description" -> description(event, settings)
            "timezone" -> timezone(event, settings)
            "review" -> review(event, settings)
            "confirm" -> confirm(event, settings)
            "cancel" -> cancel(event, settings)
            "delete" -> delete(event, settings)
            "edit" -> edit(event, settings)
            else -> throw IllegalStateException("Invalid subcommand specified")
        }
    }

    private suspend fun view(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val showOverview = event.options[0].getOption("overview")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(true)
        val calendarNumber = event.options[0].getOption("calendar")
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

    private suspend fun list(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendars = calendarService.getAllCalendars(settings.guildId)

        if (calendars.isEmpty()) {
            return event.createFollowup(getMessage("list.success.none", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else if (calendars.size == 1) {
            return event.createFollowup(getMessage("list.success.one", settings))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.linkCalendarEmbed(calendars[0]))
                .awaitSingle()
        } else {
            val response = event.createFollowup(getMessage("list.success.many", settings, "${calendars.size}"))
                .withEphemeral(ephemeral)
                .awaitSingle()

            calendars.forEach {
                event.createFollowup()
                    .withEmbeds(embedService.linkCalendarEmbed(it))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }

            return response
        }
    }

    private suspend fun create(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val name = event.options[0].getOption("name")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val description = event.options[0].getOption("description")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")
        val timezone = event.options[0].getOption("timezone")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("UTC")
            .toZoneId() ?: ZoneId.of("UTC")
        val host = event.options[0].getOption("host")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(CalendarMetadata.Host::valueOf)
            .orElse(CalendarMetadata.Host.GOOGLE)

        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard already started
        val existingWizard = calendarService.getCalendarWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) return event.createFollowup(getMessage("error.wizard.started", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.calendarWizardEmbed(existingWizard, settings))
            .withComponents(*componentService.getWizardComponents(existingWizard, settings))
            .awaitSingle()

        // Check if new calendar can be added
        if (!calendarService.canAddNewCalendar(settings.guildId))
            return event.createFollowup(getCommonMsg("error.calendar.max", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()

        val newWizard = CalendarWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = false,
            entity = Calendar(
                metadata = CalendarMetadata(
                    guildId = settings.guildId,
                    number = calendarService.getNextCalendarNumber(settings.guildId),
                    host = host,
                    id = "NOT_YET_GENERATED",
                    address = "NOT_YET_GENERATED",
                    external = false,
                    secrets = CalendarMetadata.Secrets(
                        credentialId = 0,
                        privateKey = "NOT_YET_GENERATED",
                        expiresAt = Instant.now(),
                        refreshToken = "NOT_YET_GENERATED",
                        accessToken = "NOT_YET_GENERATED",
                    ),
                ),
                name = name,
                description = description,
                timezone = timezone,
                hostLink = "NOT_YET_GENERATED",
            )
        )
        calendarService.putCalendarWizard(newWizard)

        return event.createFollowup(getMessage("create.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.calendarWizardEmbed(newWizard, settings))
            .withComponents(*componentService.getWizardComponents(newWizard, settings))
            .awaitSingle()
    }

    private suspend fun name(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val name = event.options[0].getOption("name")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getCalendarWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(name = name))
        calendarService.putCalendarWizard(alteredWizard)


        return event.createFollowup(getMessage("name.success", settings))
            .withEmbeds(embedService.calendarWizardEmbed(alteredWizard, settings))
            .withComponents(*componentService.getWizardComponents(alteredWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun description(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val desc = event.options[0].getOption("description")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getCalendarWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(description = desc))
        calendarService.putCalendarWizard(alteredWizard)

        return event.createFollowup(getMessage("description.success", settings))
            .withEmbeds(embedService.calendarWizardEmbed(alteredWizard, settings))
            .withComponents(*componentService.getWizardComponents(alteredWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun timezone(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val timezone = event.options[0].getOption("timezone")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get().toZoneId()

        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getCalendarWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Timezone will be null if not a valid tz
        if (timezone == null) return event.createFollowup(getMessage("timezone.failure.invalid", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.calendarWizardEmbed(existingWizard, settings))
            .withComponents(*componentService.getWizardComponents(existingWizard, settings))
            .awaitSingle()


        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(timezone = timezone))
        calendarService.putCalendarWizard(alteredWizard)

        return event.createFollowup(getMessage("timezone.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.calendarWizardEmbed(alteredWizard, settings))
            .withComponents(*componentService.getWizardComponents(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getCalendarWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        return event.createFollowup()
            .withEmbeds(embedService.calendarWizardEmbed(existingWizard, settings))
            .withComponents(*componentService.getWizardComponents(existingWizard, settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = calendarService.getCalendarWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) return event.createFollowup(getMessage("error.wizard.notStarted", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Would add checks for required values here, but I think I've basically hand-waved that away now
        try {
            val calendar = if (existingWizard.editing) calendarService.updateCalendar(
                settings.guildId,
                existingWizard.entity.metadata.number,
                Calendar.UpdateSpec(
                    name = existingWizard.entity.name,
                    description = existingWizard.entity.description,
                    timezone = existingWizard.entity.timezone,
                )
            ) else calendarService.createCalendar(
                settings.guildId,
                Calendar.CreateSpec(
                    host = existingWizard.entity.metadata.host,
                    number = existingWizard.entity.metadata.number,
                    name = existingWizard.entity.name,
                    description = existingWizard.entity.description,
                    timezone = existingWizard.entity.timezone,
                )
            )
            calendarService.cancelCalendarWizard(settings.guildId, calendar.metadata.number)
            calendarService.cancelCalendarWizard(existingWizard.guildId, event.interaction.user.id)

            val message = if (existingWizard.editing) getMessage("confirm.success.edit", settings)
            else getMessage("confirm.success.create", settings)

            return event.createFollowup(message)
                .withEmbeds(embedService.linkCalendarEmbed(calendar))
                .withEphemeral(ephemeral)
                .awaitSingle()

        } catch (ex: Exception) {
            LOGGER.error("Failed to create calendar via command interaction", ex)

            val message = if (existingWizard.editing) getMessage("confirm.failure.edit", settings)
            else getMessage("confirm.failure.create", settings)

            return event.createFollowup(message)
                .withEmbeds(embedService.calendarWizardEmbed(existingWizard, settings))
                .withComponents(*componentService.getWizardComponents(existingWizard, settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        }
    }

    private suspend fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        calendarService.cancelCalendarWizard(settings.guildId, event.interaction.user.id)

        return event.createFollowup(getMessage("cancel.success", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        calendarService.deleteCalendar(settings.guildId, calendarNumber)

        return event.createFollowup(getMessage("delete.success", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun edit(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard already started
        val existingWizard = calendarService.getCalendarWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) return event.createFollowup(getMessage("error.wizard.started", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.calendarWizardEmbed(existingWizard, settings))
            .withComponents(*componentService.getWizardComponents(existingWizard, settings))
            .awaitSingle()

        val calendar = calendarService.getCalendar(settings.guildId, calendarNumber)
        if (calendar == null) return event.createFollowup(getCommonMsg("error.notFound.calendar", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val newWizard = CalendarWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = true,
            entity = calendar
        )
        calendarService.putCalendarWizard(newWizard)

        return event.createFollowup(getMessage("edit.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.calendarWizardEmbed(newWizard, settings))
            .withComponents(*componentService.getWizardComponents(newWizard, settings))
            .awaitSingle()
    }
}
