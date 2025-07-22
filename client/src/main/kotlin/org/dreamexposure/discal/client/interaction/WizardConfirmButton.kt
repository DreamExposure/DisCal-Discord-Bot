package org.dreamexposure.discal.client.interaction

import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.core.business.*
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.Announcement
import org.dreamexposure.discal.core.`object`.new.Calendar
import org.dreamexposure.discal.core.`object`.new.Event
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCmdMessage
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class WizardConfirmButton(
    private val permissionService: PermissionService,
    private val calendarService: CalendarService,
    private val announcementService: AnnouncementService,
    private val embedService: EmbedService,
    private val componentService: ComponentService,
): InteractionHandler<ButtonInteractionEvent> {
    override val ids = arrayOf("wizard-confirm-")
    override val ephemeral = true

    override suspend fun handle(event: ButtonInteractionEvent, settings: GuildSettings) {
        when (event.customId.split("-")[2]) {
            "calendar" -> confirmCalendar(event, settings)
            "event" -> confirmEvent(event, settings)
            "announcement" -> confirmAnnouncement(event, settings)
            else -> throw NotImplementedError("Unrecognized wizard type ${event.customId}")
        }
    }

    private suspend fun confirmCalendar(event: ButtonInteractionEvent, settings: GuildSettings) {
        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) {
            event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }
        // Check if wizard not started
        val existingWizard = calendarService.getCalendarWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) {
            event.createFollowup(getCmdMessage("calendar", "error.wizard.notStarted", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }

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

            val message = if (existingWizard.editing) getCmdMessage("calendar", "confirm.success.edit", settings.locale)
            else getCmdMessage("calendar", "confirm.success.create", settings.locale)

            event.createFollowup(message)
                .withEmbeds(embedService.linkCalendarEmbed(calendar))
                .withEphemeral(ephemeral)
                .awaitSingle()

        } catch (ex: Exception) {
            LOGGER.error("Failed to create calendar via confirm button interaction", ex)

            val message = if (existingWizard.editing) getCmdMessage("calendar", "confirm.failure.edit", settings.locale)
            else getCmdMessage("calendar", "confirm.failure.create", settings.locale)

            event.createFollowup(message)
                .withEmbeds(embedService.calendarWizardEmbed(existingWizard, settings))
                .withComponents(*componentService.getWizardComponents(existingWizard, settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        }
    }

    private suspend fun confirmEvent(event: ButtonInteractionEvent, settings: GuildSettings) {
        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) {
            event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }
        // Check if wizard not yet started
        val existingWizard = calendarService.getEventWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) {
            event.createFollowup(getCmdMessage("event", "error.wizard.notStarted", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }
        // Validate that nothing required is missing
        if (existingWizard.entity.start == null || existingWizard.entity.end == null) {
            event.createFollowup(getCmdMessage("event", "confirm.failure.missing.time", settings.locale))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
                .withComponents(*componentService.getWizardComponents(existingWizard, settings))
                .awaitSingle()
            return
        }

        // Attempt to confirm event
        val confirmedEvent = try {
            if (existingWizard.editing) calendarService.updateEvent(
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
        } catch (exception: Exception) {
            LOGGER.error("Failed to confirm event with unexpected exception in UI level", exception)
            null
        }

        if (confirmedEvent == null) {
            val message = if (existingWizard.editing)
                getCmdMessage("event", "confirm.failure.edit", settings.locale)
            else getCmdMessage("event", "confirm.failure.create", settings.locale)

            event.createFollowup(message)
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.eventWizardEmbed(existingWizard, settings))
                .withComponents(*componentService.getWizardComponents(existingWizard, settings))
                .awaitSingle()
            return
        }

        val message = if (existingWizard.editing)
            getCmdMessage("event", "confirm.success.edit", settings.locale)
        else getCmdMessage("event", "confirm.success.create", settings.locale)

        // Basically, since the first followup is just editing the original, what if I delete the original defer message and then create a non-ephemeral followup???
        event.interactionResponse.deleteInitialResponse().awaitSingleOrNull()

        event.createFollowup(message)
            .withEphemeral(false)
            .withEmbeds(embedService.fullEventEmbed(confirmedEvent, settings))
            .withComponents(*componentService.getEventRsvpComponents(confirmedEvent, settings))
            .awaitSingle()
    }

    private suspend fun confirmAnnouncement(event: ButtonInteractionEvent, settings: GuildSettings) {
        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) {
            event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard == null) {
            event.createFollowup(getCmdMessage("announcement", "error.wizard.notStarted", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()
            return
        }
        val announcement = existingWizard.entity

        // Check if required values set
        val failureReason =
            if ((announcement.type == Announcement.Type.SPECIFIC || announcement.type == Announcement.Type.RECUR) && announcement.eventId.isNullOrBlank()) {
                getCmdMessage("announcement", "confirm.failure.missing-event-id", settings.locale)
            } else if (announcement.type == Announcement.Type.COLOR && announcement.eventColor == EventColor.NONE) {
                getCmdMessage("announcement", "confirm.failure.missing-event-color", settings.locale)
            } else if (announcement.getCalculatedTime().isZero) {
                getCmdMessage("announcement", "confirm.failure.minimum-time", settings.locale)
            } else null
        if (failureReason != null) {
            event.createFollowup(failureReason)
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .withComponents(*componentService.getWizardComponents(existingWizard, settings))
                .awaitSingle()
            return
        }

        if (existingWizard.editing) announcementService.updateAnnouncement(announcement)
        else announcementService.createAnnouncement(announcement)
        announcementService.cancelWizard(settings.guildId, event.interaction.user.id)

        val message = if (existingWizard.editing) getCmdMessage("announcement", "confirm.success.edit", settings.locale)
        else getCmdMessage("announcement", "confirm.success.create", settings.locale)

        event.createFollowup(message)
            .withEphemeral(false)
            .withEmbeds(embedService.viewAnnouncementEmbed(announcement, settings))
            .awaitSingle()
    }
}