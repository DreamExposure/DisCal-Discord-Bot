package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.extensions.discord4j.followup
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class TimeCommand(
    private val embedService: EmbedService,
) : SlashCommand {
    override val name = "time"
    override val hasSubcommands = false
    override val ephemeral = true

    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val calendarNumber = event.getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)


        val calendar = event.interaction.guild.flatMap {
            it.getCalendar(calendarNumber)
        }.awaitSingleOrNull()
        if (calendar == null) {
            return event.followup(getCommonMsg("error.notFound.calendar", settings)).awaitSingle()
        }

        return event.followup(embedService.calendarTimeEmbed(calendar, settings)).awaitSingle()
    }
}
