package org.dreamexposure.discal.client.commands.global

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.commands.SlashCommand
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.followup
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class LinkCalendarCommand : SlashCommand {
    override val name = "linkcal"
    override val ephemeral = false

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val showOverview = event.getOption("overview")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .orElse(true)

        val calendarNumber = event.getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        return event.interaction.guild.flatMap { guild ->
            CalendarEmbed.link(guild, settings, calendarNumber, showOverview)
                .flatMap(event::followup)
        }.switchIfEmpty(event.followup(getCommonMsg("error.notFound.calendar", settings)))
    }
}
