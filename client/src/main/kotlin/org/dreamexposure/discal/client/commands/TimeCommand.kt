package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.message.Responder
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class TimeCommand : SlashCommand {
    override val name = "time"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Void> {
        return Mono.justOrEmpty(event.getOption("number"))
              .flatMap { Mono.justOrEmpty(it.value) }
              .map(ApplicationCommandInteractionOptionValue::asLong)
              .map(Long::toInt)
              .defaultIfEmpty(1)
              .flatMap { calNumber ->
                  event.interaction.guild.flatMap { guild ->
                      CalendarEmbed.getTimeEmbed(guild, settings, calNumber).flatMap {
                          Responder.followupEphemeral(event, it)
                      }
                  }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
              }.then()
    }
}
