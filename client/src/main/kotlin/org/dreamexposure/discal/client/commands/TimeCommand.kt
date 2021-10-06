package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class TimeCommand : SlashCommand {
    override val name = "time"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        return event.interaction.guild.flatMap { guild ->
            CalendarEmbed.time(guild, settings, calendarNumber).flatMap {
                event.followupEphemeral(it)
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
    }
}
