package org.dreamexposure.discal.client.commands.global

import discord4j.common.util.Snowflake
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.spec.MessageCreateSpec
import discord4j.rest.http.client.ClientException
import org.dreamexposure.discal.client.commands.SlashCommand
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
import reactor.function.TupleUtils
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class DisplayCalendarCommand : SlashCommand {
    override val name = "displaycal"
    override val ephemeral = true

    @Deprecated("Use new handleSuspend for K-coroutines")
    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return when (event.options[0].name) {
            "new" -> new(event, settings)
            "update" -> update(event, settings)
            else -> Mono.empty() //Never can reach this, makes compiler happy.
        }
    }

    private fun new(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val hour = event.options[0].getOption("time")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .orElse(0) // default to midnight

        val calendarNumber = event.options[0].getOption("calendar")
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

    private fun update(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        return Mono.defer {
            val messageIdString = event.options[0].getOption("message")
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asString)
                    .get()

            Mono.justOrEmpty(event.interaction.member).filterWhen(Member::hasElevatedPermissions).flatMap {
                val messageId = Snowflake.of(messageIdString)
                DatabaseManager.getStaticMessage(settings.guildID, messageId)
                        .filter { it.type == StaticMessage.Type.CALENDAR_OVERVIEW }
                        .flatMap { static ->
                            event.client.getMessageById(static.channelId, static.messageId)
                                    .onErrorResume(ClientException.isStatusCode(403, 404)) {
                                        Mono.empty()
                                    }.flatMap { msg ->
                                        val gMono = event.interaction.guild.cache()
                                        val cMono = gMono.flatMap { it.getCalendar(static.calendarNumber) }

                                        Mono.zip(gMono, cMono).flatMap(TupleUtils.function { guild, calendar ->
                                            CalendarEmbed.overview(guild, settings, calendar, true)
                                                    .flatMap { msg.edit().withEmbedsOrNull(listOf(it)) }
                                                    .flatMap {
                                                        DatabaseManager.updateStaticMessage(
                                                                static.copy(lastUpdate = Instant.now())
                                                        )
                                                    }.then(event.followupEphemeral(getCommonMsg("success.generic", settings)))
                                        })
                                    }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.message", settings)))
                        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.staticMessage", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
        }.onErrorResume(NumberFormatException::class.java) {
            event.followupEphemeral(getCommonMsg("error.format.snowflake.message", settings))
        }
    }
}
