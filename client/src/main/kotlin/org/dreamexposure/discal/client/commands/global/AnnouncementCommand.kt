package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import discord4j.rest.util.AllowedMentions
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.AnnouncementWizardState
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.AnnouncementService
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.`object`.new.Announcement
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class AnnouncementCommand(
    private val announcementService: AnnouncementService,
    private val permissionService: PermissionService,
    private val embedService: EmbedService,
    private val calendarService: CalendarService,
) : SlashCommand {
    override val name = "announcement"
    override val hasSubcommands = true
    override val ephemeral = true

    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return when (event.options[0].name) {
            "create" -> create(event, settings)
            "type" -> type(event, settings)
            "event" -> event(event, settings)
            "color" -> color(event, settings)
            "channel" -> channel(event, settings)
            "minutes" -> minutes(event, settings)
            "hours" -> hours(event, settings)
            "info" -> info(event, settings)
            "calendar" -> calendar(event, settings)
            "publish" -> publish(event, settings)
            "review" -> review(event, settings)
            "confirm" -> confirm(event, settings)
            "cancel" -> cancel(event, settings)
            "edit" -> edit(event, settings)
            "copy" -> copy(event, settings)
            "delete" -> delete(event, settings)
            "enable" -> enable(event, settings)
            "view" -> view(event, settings)
            "list" -> list(event, settings)
            "subscribe" -> subscribe(event, settings)
            "unsubscribe" -> unsubscribe(event, settings)
            else -> throw IllegalStateException("Invalid subcommand specified")
        }
    }

    private suspend fun create(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val type = event.options[0].getOption("type")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(Announcement.Type::valueOf)
            .orElse(Announcement.Type.UNIVERSAL)
        val channelId = event.options[0].getOption("channel")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .orElse(event.interaction.channelId)
        val minutes = event.options[0].getOption("minutes")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(0)
        val hours = event.options[0].getOption("hours")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(0)
        val calendar = event.options[0].getOption("calendar")
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
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) {
            return event.createFollowup(getMessage("error.wizard.started", settings))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        val newWizard = AnnouncementWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = false,
            entity = Announcement(
                guildId = settings.guildId,
                calendarNumber = calendar,
                type = type,
                channelId = channelId,
                hoursBefore = hours,
                minutesBefore = minutes,
            )
        )
        announcementService.putWizard(newWizard)

        return event.createFollowup(getMessage("create.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(newWizard, settings))
            .awaitSingle()
    }

    private suspend fun type(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val type = event.options[0].getOption("type")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(Announcement.Type::valueOf)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()


        val altered = existingWizard.copy(
            entity = existingWizard.entity.copy(
                type = type,
                // Handle edge case where event is already set, but has recurrence suffix.
                eventId = existingWizard.entity.eventId?.split("_")?.get(0)
            )
        )
        announcementService.putWizard(altered)

        return event.createFollowup(getMessage("type.success", settings))
            .withEphemeral(true)
            .withEmbeds(embedService.announcementWizardEmbed(altered, settings))
            .awaitSingle()
    }

    private suspend fun event(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        val announcement = existingWizard.entity

        // Validate current type
        if (announcement.type != Announcement.Type.RECUR && announcement.type != Announcement.Type.SPECIFIC) {
            return event.createFollowup(getMessage("event.failure.type", settings))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        // Validate event actually exists
        val calendarEvent = calendarService.getEvent(announcement.guildId, announcement.calendarNumber, eventId)
        if (calendarEvent == null) {
            return event.createFollowup(getCommonMsg("error.notFound.event", settings.locale))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        // Handle what format the ID is actually saved in
        val idToSet = if (announcement.type == Announcement.Type.RECUR) calendarEvent.id.split("_")[0]
        else eventId

        val alteredWizard = existingWizard.copy(entity = announcement.copy(eventId = idToSet))
        announcementService.putWizard(alteredWizard)

        return event.createFollowup(getMessage("event.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(alteredWizard, settings))
            .awaitSingle()
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
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()

        // Make sure type matches
        if (existingWizard.entity.type != Announcement.Type.COLOR) {
            return event.createFollowup(getMessage("color.failure.type", settings))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        val alteredWizard = existingWizard.copy(entity = existingWizard.entity.copy(eventColor =  color))
        announcementService.putWizard(alteredWizard)

        return event.createFollowup(getMessage("color.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun channel(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val channelId = event.options[0].getOption("channel")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .orElse(event.interaction.channelId)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        val announcement = existingWizard.entity

        val alteredWizard = existingWizard.copy(entity = announcement.copy(channelId = channelId))
        announcementService.putWizard(alteredWizard)

        return event.createFollowup(getMessage("channel.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun minutes(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val minutes = event.options[0].getOption("minutes")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        val announcement = existingWizard.entity

        val alteredWizard = existingWizard.copy(entity = announcement.copy(minutesBefore = minutes))
        announcementService.putWizard(alteredWizard)

        return event.createFollowup(getMessage("minutes.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun hours(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val hours = event.options[0].getOption("hours")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(0)

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        val announcement = existingWizard.entity

        val alteredWizard = existingWizard.copy(entity = announcement.copy(hoursBefore = hours))
        announcementService.putWizard(alteredWizard)

        return event.createFollowup(getMessage("hours.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun info(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val info = event.options[0].getOption("info")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .filter { it.isNotBlank() || !it.equals("None", true) }
            .getOrNull()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        val announcement = existingWizard.entity

        val alteredWizard = existingWizard.copy(entity = announcement.copy(info = info))
        announcementService.putWizard(alteredWizard)

        return event.createFollowup(getMessage("info.success.set", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun calendar(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendar = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        val announcement = existingWizard.entity

        val alteredWizard = existingWizard.copy(entity = announcement.copy(calendarNumber = calendar))
        announcementService.putWizard(alteredWizard)

        return event.createFollowup(getMessage("calendar.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun publish(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val publish = event.options[0].getOption("publish")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        val announcement = existingWizard.entity

        // Confirm guild has access to feature
        if (!settings.patronGuild) {
            return event.createFollowup(getCommonMsg("error.patronOnly", settings.locale))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        val alteredWizard = existingWizard.copy(entity = announcement.copy(publish = publish))
        announcementService.putWizard(alteredWizard)

        return event.createFollowup(getMessage("publish.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(alteredWizard, settings))
            .awaitSingle()
    }

    private suspend fun review(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()

        return event.createFollowup()
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
            .awaitSingle()
    }

    private suspend fun confirm(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard not started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
            ?: return event.createFollowup(getMessage("error.wizard.notStarted", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        val announcement = existingWizard.entity

        // Check if required values set
        val failureReason =
            if ((announcement.type == Announcement.Type.SPECIFIC || announcement.type == Announcement.Type.RECUR) && announcement.eventId.isNullOrBlank()) {
                getMessage("confirm.failure.missing-event-id", settings)
            } else if (announcement.type == Announcement.Type.COLOR && announcement.eventColor == EventColor.NONE) {
                getMessage("confirm.failure.missing-event-color", settings)
            } else if (announcement.getCalculatedTime().isZero) {
                getMessage("confirm.failure.minimum-time", settings)
            } else null
        if (failureReason != null) {
            return event.createFollowup(failureReason)
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        if (existingWizard.editing) announcementService.updateAnnouncement(announcement)
        else announcementService.createAnnouncement(announcement)
        announcementService.cancelWizard(settings.guildId, event.interaction.user.id)

        val message = if (existingWizard.editing) getMessage("confirm.success.edit", settings)
        else getMessage("confirm.success.create", settings)

        return event.createFollowup(message)
            .withEphemeral(false)
            .withEmbeds(embedService.viewAnnouncementEmbed(announcement, settings))
            .awaitSingle()
    }

    private suspend fun cancel(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        announcementService.cancelWizard(settings.guildId, event.interaction.user.id)

        return event.createFollowup(getMessage("cancel.success", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun edit(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard already started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) {
            return event.createFollowup(getMessage("error.wizard.started", settings))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        val announcement = announcementService.getAnnouncement(settings.guildId, announcementId)
            ?: return event.createFollowup(getCommonMsg("error.notFound.announcement", settings.locale))
                .withEphemeral(true)
                .awaitSingle()

        val newWizard = AnnouncementWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = true,
            entity = announcement
        )
        announcementService.putWizard(newWizard)

        return event.createFollowup(getMessage("edit.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(newWizard, settings))
            .awaitSingle()
    }

    private suspend fun copy(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        // Check if wizard already started
        val existingWizard = announcementService.getWizard(settings.guildId, event.interaction.user.id)
        if (existingWizard != null) {
            return event.createFollowup(getMessage("error.wizard.started", settings))
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.announcementWizardEmbed(existingWizard, settings))
                .awaitSingle()
        }

        val announcement = announcementService.getAnnouncement(settings.guildId, announcementId)
            ?: return event.createFollowup(getCommonMsg("error.notFound.announcement", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()

        val newWizard = AnnouncementWizardState(
            guildId = settings.guildId,
            userId = event.interaction.user.id,
            editing = false,
            entity = announcement.copy(id = KeyGenerator.generateAnnouncementId())
        )
        announcementService.putWizard(newWizard)

        return event.createFollowup(getMessage("copy.success", settings))
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.announcementWizardEmbed(newWizard, settings))
            .awaitSingle()
    }

    private suspend fun delete(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        announcementService.deleteAnnouncement(settings.guildId, announcementId)

        return event.createFollowup(getMessage("delete.success", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun enable(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val enabled = event.options[0].getOption("enabled")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        // Validate permissions
        val hasControlRole = permissionService.hasControlRole(settings.guildId, event.interaction.user.id)
        if (!hasControlRole) return event.createFollowup(getCommonMsg("error.perms.privileged", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val announcement = announcementService.getAnnouncement(settings.guildId, announcementId)
            ?: return event.createFollowup(getCommonMsg("error.notFound.announcement", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()

        val new = announcement.copy(enabled = enabled)
        announcementService.updateAnnouncement(new)

        val message = if (enabled) "enable.success" else "disable.success"
        return event.createFollowup()
            .withEphemeral(ephemeral)
            .withContent("${getMessage(message, settings)}\n\n${new.subscribers.buildMentions()}")
            .withEmbeds(embedService.viewAnnouncementEmbed(new, settings))
            .withAllowedMentions(AllowedMentions.suppressAll())
            .awaitSingle()
    }

    private suspend fun view(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val announcement = announcementService.getAnnouncement(settings.guildId, announcementId)
            ?: return event.createFollowup(getCommonMsg("error.notFound.announcement", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()

        return event.createFollowup()
            .withEphemeral(ephemeral)
            .withEmbeds(embedService.viewAnnouncementEmbed(announcement, settings))
            .withContent(announcement.subscribers.buildMentions())
            .withAllowedMentions(AllowedMentions.suppressAll())
            .awaitSingle()
    }

    private suspend fun list(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val amount = event.options[0].getOption("amount")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()
        val calendar = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)
        val showDisabled = event.options[0].getOption("show-disabled")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(false)
        val type = event.options[0].getOption("type")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(Announcement.Type::valueOf)
            .getOrNull()

        // Get filtered announcements
        val announcements = announcementService.getAllAnnouncements(settings.guildId, type, showDisabled)
            .filter { it.calendarNumber == calendar }

        return if (announcements.isEmpty()) {
            event.createFollowup(getMessage("list.success.none", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        } else if (announcements.size == 1) {
            event.createFollowup()
                .withEphemeral(ephemeral)
                .withEmbeds(embedService.viewAnnouncementEmbed(announcements[0], settings))
                .withContent(announcements[0].subscribers.buildMentions())
                .withAllowedMentions(AllowedMentions.suppressAll())
                .awaitSingle()
        } else {
            val limit = if (amount > 0) amount.coerceAtMost(announcements.size) else announcements.size

            val message = event.createFollowup(getMessage("list.success.many", settings, "$limit"))
                .withEphemeral(ephemeral)
                .awaitSingle()

            announcements.subList(0, limit).forEach { announcement ->
                event.createFollowup()
                    .withEmbeds(embedService.condensedAnnouncementEmbed(announcement, settings))
                    .withEphemeral(ephemeral)
                    .awaitSingle()
            }

            message
        }


    }

    private suspend fun subscribe(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val userId = event.options[0].getOption("user")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .getOrNull()
        val roleId = event.options[0].getOption("role")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .getOrNull()

        val announcement = announcementService.getAnnouncement(settings.guildId, announcementId)
            ?: return event.createFollowup(getCommonMsg("error.notFound.announcement", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()

        var newSubs = announcement.subscribers
        if (userId != null) newSubs = newSubs.copy(users = newSubs.users + userId)
        if (roleId != null) newSubs = newSubs.copy(roles = newSubs.roles + roleId.asString())
        if (roleId == null && userId == null) newSubs = newSubs.copy(users = newSubs.users + event.interaction.user.id)

        val new = announcement.copy(subscribers = newSubs)
        announcementService.updateAnnouncement(new)

        return event.createFollowup()
            .withEphemeral(ephemeral)
            .withContent("${getMessage("subscribe.success", settings)}\n\n${new.subscribers.buildMentions()}")
            .withEmbeds(embedService.viewAnnouncementEmbed(new, settings))
            .withAllowedMentions(AllowedMentions.suppressAll())
            .awaitSingle()
    }

    private suspend fun unsubscribe(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val announcementId = event.options[0].getOption("announcement")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()
        val userId = event.options[0].getOption("user")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .getOrNull()
        val roleId = event.options[0].getOption("role")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .getOrNull()

        val announcement = announcementService.getAnnouncement(settings.guildId, announcementId)
            ?: return event.createFollowup(getCommonMsg("error.notFound.announcement", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()

        var newSubs = announcement.subscribers
        if (userId != null) newSubs = newSubs.copy(users = newSubs.users - userId)
        if (roleId != null) newSubs = newSubs.copy(roles = newSubs.roles - roleId.asString())
        if (roleId == null && userId == null) newSubs = newSubs.copy(users = newSubs.users - event.interaction.user.id)

        val new = announcement.copy(subscribers = newSubs)
        announcementService.updateAnnouncement(new)

        return event.createFollowup()
            .withEphemeral(ephemeral)
            .withContent("${getMessage("unsubscribe.success", settings)}\n\n${new.subscribers.buildMentions()}")
            .withEmbeds(embedService.viewAnnouncementEmbed(new, settings))
            .withAllowedMentions(AllowedMentions.suppressAll())
            .awaitSingle()
    }
}
