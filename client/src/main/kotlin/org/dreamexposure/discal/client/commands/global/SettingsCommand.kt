package org.dreamexposure.discal.client.commands.global

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.client.message.embed.SettingsEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.extensions.discord4j.followup
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SettingsCommand : SlashCommand {
    override val name = "settings"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        //Check if user has permission to use this
        return event.interaction.member.get().hasElevatedPermissions().flatMap { hasPerm ->
            if (hasPerm) {
                return@flatMap when (event.options[0].name) {
                    "view" -> viewSubcommand(event, settings)
                    "role" -> roleSubcommand(event, settings)
                    "announcement-style" -> announcementStyleSubcommand(event, settings)
                    "language" -> languageSubcommand(event, settings)
                    "time-format" -> timeFormatSubcommand(event, settings)
                    "branding" -> brandingSubcommand(event, settings)
                    else -> Mono.empty() //Never can reach this, makes compiler happy.
                }
            } else {
                event.followupEphemeral(getCommonMsg("error.perms.elevated", settings))
            }
        }
    }

    private fun viewSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return event.interaction.guild
              .flatMap { SettingsEmbed.getView(it, settings) }
              .flatMap(event::followup)
    }

    private fun roleSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.options[0].getOption("role"))
              .map { it.value.get() }
              .flatMap(ApplicationCommandInteractionOptionValue::asRole)
              .doOnNext { settings.controlRole = it.id.asString() }
              .flatMap { role ->
                  DatabaseManager.updateSettings(settings).then(
                      event.followupEphemeral(getMessage("role.success", settings, role.name))
                  )
              }
    }

    private fun announcementStyleSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val announcementStyle = event.options[0].getOption("style")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map(AnnouncementStyle::fromValue)
            .get()

        settings.announcementStyle = announcementStyle

        return DatabaseManager.updateSettings(settings)
            .flatMap { event.followupEphemeral(getMessage("style.success", settings, announcementStyle.name)) }
    }

    private fun languageSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val lang = event.options[0].getOption("lang")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        settings.lang = lang

        return DatabaseManager.updateSettings(settings)
            .flatMap { event.followupEphemeral(getMessage("lang.success", settings)) }
    }

    private fun timeFormatSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val timeFormat = event.options[0].getOption("format")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .map(TimeFormat::fromValue)
            .get()

        settings.timeFormat = timeFormat

        return DatabaseManager.updateSettings(settings)
            .flatMap { event.followupEphemeral(getMessage("format.success", settings, timeFormat.name)) }
    }

    private fun brandingSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return if (settings.patronGuild) {
            val useBranding = event.options[0].getOption("use")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .get()

            settings.branded = useBranding

            DatabaseManager.updateSettings(settings)
                .flatMap { event.followupEphemeral(getMessage("brand.success", settings, "$useBranding")) }
        } else {
            event.followupEphemeral(getCommonMsg("error.patronOnly", settings))
        }
    }
}
