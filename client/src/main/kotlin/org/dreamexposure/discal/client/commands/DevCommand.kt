package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.web.UserAPIAccount
import org.dreamexposure.discal.core.crypto.KeyGenerator.csRandomAlphaNumericString
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DevCommand : SlashCommand {
    override val name = "dev"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        if (!GlobalVal.devUserIds.contains(event.interaction.user.id)) {
            return event.followupEphemeral(getMessage("error.notDeveloper", settings))
        }

        return when (event.options[0].name) {
            "patron" -> patronSubcommand(event, settings)
            "dev" -> devSubcommand(event, settings)
            "maxcal" -> maxCalSubcommand(event, settings)
            "api-register" -> apiRegisterSubcommand(event, settings)
            "api-block" -> apiBlockSubcommand(event, settings)
            else -> Mono.empty() //Never can reach this, makes compiler happy.
        }
    }

    private fun patronSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val guildId = event.options[0].getOption("guild")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .get()

        return DatabaseManager.getSettings(guildId)
            .doOnNext { settings.patronGuild = !settings.patronGuild }
            .flatMap {
                DatabaseManager.updateSettings(it).then(
                    event.followupEphemeral(getMessage("patron.success", settings, settings.patronGuild.toString()))
                )
            }.doOnError { LOGGER.error("[cmd] patron failure", it) }
            .onErrorResume { event.followupEphemeral(getMessage("patron.failure.badId", settings)) }
    }

    private fun devSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val guildId = event.options[0].getOption("guild")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .get()

        return DatabaseManager.getSettings(guildId)
            .doOnNext { settings.devGuild = !settings.devGuild }
            .flatMap {
                DatabaseManager.updateSettings(it).then(
                    event.followupEphemeral(getMessage("dev.success", settings, settings.devGuild.toString()))
                )
            }.doOnError { LOGGER.error("[cmd] dev failure", it) }
            .onErrorResume { event.followupEphemeral(getMessage("dev.failure.badId", settings)) }
    }

    private fun maxCalSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val guildId = event.options[0].getOption("guild")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asSnowflake)
            .get()

        return DatabaseManager.getSettings(guildId)
            .doOnNext {
                val amount = event.options[0].getOption("amount").get().value.get().asLong().toInt()
                it.maxCalendars = amount
            }.flatMap {
                DatabaseManager.updateSettings(it).then(
                    event.followupEphemeral(getMessage("maxcal.success", settings, settings.maxCalendars.toString()))
                )
            }
            .onErrorResume {
                event.followupEphemeral(getMessage("maxcal.failure.badInput", settings))
            }
    }

    private fun apiRegisterSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.options[0].getOption("user").flatMap { it.value })
            .flatMap(ApplicationCommandInteractionOptionValue::asUser)
            .flatMap { user ->
                val acc = UserAPIAccount(
                    user.id.asString(),
                    csRandomAlphaNumericString(64),
                    false,
                    System.currentTimeMillis()
                )

                DatabaseManager.updateAPIAccount(acc).flatMap { success ->
                    if (success) {
                        event.followupEphemeral(getMessage("apiRegister.success", settings, acc.APIKey))
                    } else {
                        event.followupEphemeral(getMessage("apiRegister.failure.unable", settings))
                    }
                }
            }.switchIfEmpty(event.followupEphemeral(getMessage("apiRegister.failure.empty", settings)))
    }

    private fun apiBlockSubcommand(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.justOrEmpty(event.options[0].getOption("key").flatMap { it.value })
            .map(ApplicationCommandInteractionOptionValue::asString)
            .flatMap(DatabaseManager::getAPIAccount)
            .map {
                it.copy(blocked = true)
            }.flatMap(DatabaseManager::updateAPIAccount)
            .flatMap { event.followupEphemeral(getMessage("apiBlock.success", settings)) }
            .switchIfEmpty(event.followupEphemeral(getMessage("apiBlock.failure.notFound", settings)))
            .onErrorResume {
                event.followupEphemeral(getMessage("apiBlock.failure.other", settings))
            }
    }
}
