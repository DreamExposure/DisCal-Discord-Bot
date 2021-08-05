package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.dreamexposure.discal.client.message.Responder
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class LinkCalendarCommand : SlashCommand {
    override val name = "linkcal"
    override val ephemeral = false

    override fun handle(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        return Mono.justOrEmpty(event.getOption("number"))
                .flatMap { Mono.justOrEmpty(it.value) }
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .defaultIfEmpty(1)
                .flatMap { calNumber ->
                    event.interaction.guild.flatMap { guild ->
                        CalendarEmbed.getLinkCalEmbed(guild, settings, calNumber).flatMap {
                            Responder.followup(event, it)
                        }
                    }.switchIfEmpty(
                            //TODO: i18n
                            Responder.followup(event, "Calendar not found. Perhaps you should create a new one?")
                    )
                }.then()
    }
}
