package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.StaticMessageService
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class DisplayCalendarCommand(
    private val staticMessageService: StaticMessageService,
) : SlashCommand {
    override val name = "displaycal"
    override val hasSubcommands = true
    override val ephemeral = true


    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        return when (event.options[0].name) {
            "new" -> new(event, settings)
            else -> throw IllegalStateException("Invalid subcommand specified")
        }
    }

    private suspend fun new(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val hour = event.options[0].getOption("time")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(0) // default to midnight
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        // Validate control role
        val hasElevatedPerms = event.interaction.member.get().hasElevatedPermissions().awaitSingle()
        if (!hasElevatedPerms)
            return event.createFollowup(getCommonMsg("error.perms.elevated", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()

        // Validate calendar exists
        val calendar = event.interaction.guild.flatMap { it.getCalendar(calendarNumber) }.awaitSingleOrNull()
        if (calendar == null)
            return event.createFollowup(getCommonMsg("error.notFound.calendar", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()

        // Create and respond
        staticMessageService.createStaticMessage(settings.guildID, event.interaction.channelId, calendarNumber, hour)

        return event.createFollowup(getCommonMsg("success.generic", settings))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}
