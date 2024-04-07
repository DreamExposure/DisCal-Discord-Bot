package org.dreamexposure.discal.client.commands.global

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.core.business.EmbedService
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class LinkCalendarCommand(
    private val embedService: EmbedService,
) : SlashCommand {
    override val name = "linkcal"
    override val hasSubcommands = false
    override val ephemeral = false


    override suspend fun suspendHandle(event: ChatInputInteractionEvent, settings: GuildSettings): Message {
        val showOverview = event.getOption("overview")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .orElse(true)
        val calendarNumber = event.getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val calendar = event.interaction.guild.flatMap {
            it.getCalendar(calendarNumber)
        }.awaitSingleOrNull()
        if (calendar == null) {
            return event.createFollowup(getCommonMsg("error.notFound.calendar", settings))
                .withEphemeral(ephemeral)
                .awaitSingle()
        }

        return event.createFollowup()
            .withEmbeds(embedService.linkCalendarEmbed(calendarNumber, settings, showOverview))
            .withEphemeral(ephemeral)
            .awaitSingle()
    }
}
