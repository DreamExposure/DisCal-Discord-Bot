package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.business.PermissionService
import org.dreamexposure.discal.core.enums.time.DiscordTimestampFormat.LONG_DATETIME
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.extensions.asDiscordTimestamp
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class SettingsCommand(
    private val settingsService: GuildSettingsService,
    private val embedService: EmbedService,
    private val permissionService: PermissionService,
) : SlashCommand {
    override val name = "settings"
    override val hasSubcommands = true
    override val ephemeral = true

    override suspend fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate permissions
        val hasElevatedPerms = permissionService.hasElevatedPermissions(settings.guildId, event.interaction.user.id)
        if (!hasElevatedPerms) return event.createFollowup(getCommonMsg("error.perms.elevated", settings.locale))
                .withEphemeral(ephemeral)
                .awaitSingle()

        return when (event.options[0].name) {
            "view" -> view(event, settings)
            "role" -> role(event, settings)
            "announcement-style" -> announcementStyle(event, settings)
            "language" -> language(event, settings)
            "time-format" -> timeFormat(event, settings)
            "keep-event-duration" -> eventKeepDuration(event, settings)
            "show-rsvp-dropdown" -> showRsvpDropdown(event, settings)
            "branding" -> branding(event, settings)
            "pause-announcements" -> pauseAnnouncements(event, settings)
            else -> throw IllegalStateException("Invalid subcommand specified")
        }
    }

    private suspend fun view(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return event.createFollowup()
            .withEmbeds(embedService.settingsEmbeds(settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun role(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val roleId = event.options[0].getOption("role")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .orElse(settings.guildId)

        val newSettings= settingsService.upsertSettings(settings.copy(controlRole = roleId))

        return event.createFollowup(getMessage("role.success", settings))
            .withEmbeds(embedService.settingsEmbeds(newSettings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun announcementStyle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val style = event.options[0].getOption("style")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map { v -> GuildSettings.AnnouncementStyle.entries.first { it.value == v } }
            .get()

        val newSettings = settingsService.upsertSettings(settings.copy(interfaceStyle = settings.interfaceStyle.copy(announcementStyle = style)))

        return event.createFollowup(getMessage("style.success", settings, style.name))
            .withEmbeds(embedService.settingsEmbeds(newSettings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun language(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val locale = event.options[0].getOption("lang")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(Locale::forLanguageTag)
            .get()

        val newSettings = settingsService.upsertSettings(settings.copy(locale = locale))

        return event.createFollowup(getMessage("lang.success", newSettings))
            .withEmbeds(embedService.settingsEmbeds(newSettings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun timeFormat(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val timeFormat = event.options[0].getOption("format")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map(TimeFormat::fromValue)
            .get()

        val newSettings = settingsService.upsertSettings(settings.copy(interfaceStyle = settings.interfaceStyle.copy(timeFormat = timeFormat)))

        return event.createFollowup(getMessage("format.success", settings, timeFormat.name))
            .withEmbeds(embedService.settingsEmbeds(newSettings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun eventKeepDuration(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val keepDuration = event.options[0].getOption("value")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        val newSettings = settingsService.upsertSettings(settings.copy(eventKeepDuration = keepDuration))

        return event.createFollowup(getMessage("eventKeepDuration.success.$keepDuration", settings))
            .withEmbeds(embedService.settingsEmbeds(newSettings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun showRsvpDropdown(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val shown = event.options[0].getOption("shown")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        val newSettings = settingsService.upsertSettings(settings.copy(showRsvpDropdown = shown))

        return event.createFollowup(getMessage("showRsvpDropdown.success.$shown", settings))
            .withEmbeds(embedService.settingsEmbeds(newSettings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun branding(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val useBranding = event.options[0].getOption("use")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .get()

        if (!settings.patronGuild) return event.createFollowup(getCommonMsg("error.patronOnly", settings.locale))
            .withEphemeral(ephemeral)
            .awaitSingle()

        val newSettings = settingsService.upsertSettings(settings.copy(interfaceStyle = settings.interfaceStyle.copy(branded = useBranding)))

        return event.createFollowup(getMessage("brand.success", settings, "$useBranding"))
            .withEmbeds(embedService.settingsEmbeds(newSettings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun pauseAnnouncements(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val hours = event.options[0].getOption("hours")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(0)
        val days = event.options[0].getOption("days")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(0)
        val weeks = event.options[0].getOption("weeks")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(0)

        // Remove pause
        if (hours + days + weeks == 0L) {
            val newSettings = settingsService.upsertSettings(settings.copy(pauseAnnouncementsUntil = null))

            return event.createFollowup(getMessage("pauseAnnouncements.success.unpause", settings))
                .withEmbeds(embedService.settingsEmbeds(newSettings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        }

        val pauseUntil = Instant.now()
            .plus(hours, ChronoUnit.HOURS)
            .plus(days, ChronoUnit.DAYS)
            .plus(weeks * 7, ChronoUnit.DAYS) // Weeks not supported lmao
        val newSettings = settingsService.upsertSettings(settings.copy(pauseAnnouncementsUntil = pauseUntil))

        return event.createFollowup(getMessage("pauseAnnouncements.success.pause", settings, pauseUntil.asDiscordTimestamp(LONG_DATETIME)))
            .withEmbeds(embedService.settingsEmbeds(newSettings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}
