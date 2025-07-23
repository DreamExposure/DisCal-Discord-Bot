package org.dreamexposure.discal.client.commands.dev

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.business.GuildSettingsService
import org.dreamexposure.discal.core.`object`.new.GuildSettings
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.stereotype.Component

@Component
class DevCommand(
    private val settingsService: GuildSettingsService,
    private val embedService: EmbedService,
) : SlashCommand {
    override val name = "dev"
    override val hasSubcommands = true
    override val ephemeral = true

    override suspend fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        // Validate this user is actually a dev
        if (!GlobalVal.devUserIds.contains(event.interaction.user.id))
            return event.createFollowup(getMessage("error.notDeveloper", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()

        return when (event.options[0].name) {
            "patron" -> patron(event, settings)
            "dev" -> dev(event, settings)
            "maxcal" -> maxCalendars(event, settings)
            "settings" -> settings(event)
            else -> throw IllegalStateException("Invalid subcommand specified")
        }
    }

    private suspend fun patron(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val guildId = event.options[0].getOption("guild")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(Snowflake::of)
            .get()

        val oldTargetSettings = settingsService.getSettings(guildId)
        val newTargetSettings = settingsService.upsertSettings(oldTargetSettings.copy(patronGuild = !oldTargetSettings.patronGuild))

        return event.createFollowup(getMessage("patron.success", settings, "${newTargetSettings.patronGuild}"))
            .withEmbeds(embedService.settingsEmbeds(newTargetSettings, debug = true))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun dev(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val guildId = event.options[0].getOption("guild")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(Snowflake::of)
            .get()

        val oldTargetSettings = settingsService.getSettings(guildId)
        val newTargetSettings = settingsService.upsertSettings(oldTargetSettings.copy(devGuild = !oldTargetSettings.devGuild))

        return event.createFollowup(getMessage("dev.success", settings, newTargetSettings.devGuild.toString()))
            .withEmbeds(embedService.settingsEmbeds(newTargetSettings, debug = true))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun maxCalendars(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val guildId = event.options[0].getOption("guild")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(Snowflake::of)
            .get()
        val amount = event.options[0].getOption("amount")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()

        val oldTargetSettings = settingsService.getSettings(guildId)
        val newTargetSettings = settingsService.upsertSettings(oldTargetSettings.copy(maxCalendars = amount))

        return event.createFollowup(getMessage("maxcal.success", newTargetSettings, "$amount"))
            .withEmbeds(embedService.settingsEmbeds(newTargetSettings, debug = true))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }

    private suspend fun settings(event: ChatInputInteractionEvent): Message {
        val guildId = event.options[0].getOption("guild")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .map(Snowflake::of)
            .get()

        val targetSettings = settingsService.getSettings(guildId)

        return event.createFollowup()
            .withEmbeds(embedService.settingsEmbeds(targetSettings, debug = true))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}
