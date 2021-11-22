package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.MessageCreateSpec
import org.dreamexposure.discal.client.message.embed.CalendarEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.StaticMessage
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class DisplayCalendarCommand : SlashCommand {
    override val name = "displaycal"
    override val ephemeral = true

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val hour = event.getOption("time")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .get()

        val calendarNumber = event.getOption("calendar")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::toInt)
                .orElse(1)



        return Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
            event.interaction.guild.flatMap { guild ->
                guild.getCalendar(calendarNumber).flatMap { cal ->
                    CalendarEmbed.overview(guild, settings, cal, true).flatMap { embed ->
                        event.interaction.channel.flatMap {
                            it.createMessage(
                                    MessageCreateSpec.builder()
                                            .addEmbed(embed)
                                            .build()
                            )
                        }.flatMap { msg ->
                            val nextUpdate = ZonedDateTime.now(cal.timezone)
                                    .truncatedTo(ChronoUnit.DAYS)
                                    .plusHours(hour + 24)
                                    .toInstant()

                            val staticMsg = StaticMessage(
                                    settings.guildID,
                                    msg.id,
                                    msg.channelId,
                                    StaticMessage.Type.CALENDAR_OVERVIEW,
                                    Instant.now(),
                                    nextUpdate,
                                    calendarNumber,
                            )

                            DatabaseManager.updateStaticMessage(staticMsg)
                                    .then(event.followupEphemeral(getCommonMsg("success.generic", settings)))
                        }
                    }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
            }
        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
    }
}
