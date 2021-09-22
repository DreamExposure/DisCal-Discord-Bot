package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.message.Responder
import org.dreamexposure.discal.client.message.embed.SettingsEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.enums.announcement.AnnouncementStyle
import org.dreamexposure.discal.core.enums.time.TimeFormat
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SettingsCommand : SlashCommand {
    override val name = "settings"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
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
                Responder.followupEphemeral(event, getCommonMsg("error.perms.elevated", settings)).then()
            }
        }
    }

    private fun viewSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        return event.interaction.guild
              .flatMap { SettingsEmbed.getView(it, settings) }
              .flatMap { Responder.followup(event, it) }
              .then()
    }

    private fun roleSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        return Mono.justOrEmpty(event.options[0].getOption("role"))
              .map { it.value.get() }
              .flatMap(ApplicationCommandInteractionOptionValue::asRole)
              .doOnNext { settings.controlRole = it.id.asString() }
              .flatMap { role ->
                  DatabaseManager.updateSettings(settings)
                        .then(Responder.followupEphemeral(event, getMessage("role.success", settings, role.name)))
              }.then()
    }

    private fun announcementStyleSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        return Mono.justOrEmpty(event.options[0].getOption("style"))
              .map { it.value.get() }
              .map { AnnouncementStyle.fromValue(it.asLong().toInt()) }
              .doOnNext { settings.announcementStyle = it }
              .flatMap { DatabaseManager.updateSettings(settings) }
              .flatMap {
                  Responder.followupEphemeral(
                        event,
                        getMessage("style.success", settings, settings.announcementStyle.name)
                  )
              }.then()
    }

    private fun languageSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        return Mono.justOrEmpty(event.options[0].getOption("lang"))
              .map { it.value.get() }
              .map(ApplicationCommandInteractionOptionValue::asString)
              .doOnNext { settings.lang = it }
              .flatMap { DatabaseManager.updateSettings(settings) }
              .then(Responder.followupEphemeral(event, getMessage("lang.success", settings)))
              .then()
    }

    private fun timeFormatSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        return Mono.justOrEmpty(event.options[0].getOption("format"))
              .map { it.value.get() }
              .map { TimeFormat.fromValue(it.asLong().toInt()) }
              .doOnNext { settings.timeFormat = it }
              .flatMap { DatabaseManager.updateSettings(settings) }
              .flatMap {
                  Responder.followupEphemeral(event, getMessage("format.success", settings, settings.timeFormat.name))
              }.then()
    }

    private fun brandingSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        return if (settings.patronGuild) {
            Mono.justOrEmpty(event.options[0].getOption("use"))
                  .map { it.value.get() }
                  .map(ApplicationCommandInteractionOptionValue::asBoolean)
                  .doOnNext { settings.branded = it }
                  .flatMap {
                      DatabaseManager.updateSettings(settings).then(Responder.followupEphemeral(
                            event,
                            getMessage("brand.success", settings, "$it")
                      ))
                  }.then()
        } else {
            Responder.followupEphemeral(event, getCommonMsg("error.patronOnly", settings)).then()
        }
    }
}
