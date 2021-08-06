package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.dreamexposure.discal.client.message.Responder
import org.dreamexposure.discal.client.message.embed.EventEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import java.time.Instant

@Component
class EventsCommand : SlashCommand {
    override val name = "events"
    override val ephemeral = false

    override fun handle(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        return when (event.options[0].name) {
            "upcoming" -> upcomingEventsSubcommand(event, settings)
            "ongoing" -> ongoingEventsSubcommand(event, settings)
            "today" -> eventsTodaySubcommand(event, settings)
            else -> Mono.empty() //Never can reach this, makes compiler happy.
        }
    }

    private fun upcomingEventsSubcommand(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        //Determine which calendar they want to use...
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val amountMono = Mono.justOrEmpty(event.options[0].getOption("amount").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        return Mono.zip(calNumMono, amountMono).flatMap(TupleUtils.function { calNumb, amount ->
            if (amount < 1 || amount > 15) {
                Responder.followupEphemeral(event, getMessage("upcoming.failure.outOfRange", settings))
            }

            event.interaction.guild.flatMap { guild ->
                guild.getCalendar(calNumb).flatMap { cal ->
                    cal.getUpcomingEvents(amount).collectList().flatMap { events ->
                        if (events.isEmpty()) {
                            Responder.followup(event, getMessage("upcoming.success.none", settings))
                        } else if (events.size == 1) {
                            Responder.followup(
                                  event,
                                  getMessage("upcoming.success.one", settings),
                                  EventEmbed.getFull(guild, settings, events[0])
                            )
                        } else {
                            Responder.followup(event, getMessage("upcoming.success.many", settings, "${events.size}"))
                                  .flatMapMany {
                                      Flux.fromIterable(events)
                                  }.flatMap {
                                      Responder.followup(event, EventEmbed.getCondensed(guild, settings, it))
                                  }.then(Mono.just(""))
                        }
                    }
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
            }
        }).then()
    }

    private fun ongoingEventsSubcommand(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        return Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1).flatMap { calNum ->
                  event.interaction.guild.flatMap { guild ->
                      guild.getCalendar(calNum).flatMap { cal ->
                          cal.getOngoingEvents().collectList().flatMap { events ->
                              if (events.isEmpty()) {
                                  Responder.followup(
                                        event,
                                        getMessage("ongoing.success.none", settings)
                                  )
                              } else if (events.size == 1) {
                                  Responder.followup(
                                        event,
                                        getMessage("ongoing.success.one", settings),
                                        EventEmbed.getFull(guild, settings, events[0])
                                  )
                              } else {
                                  Responder.followup(event,
                                        getMessage("ongoing.success.many", settings, "${events.size}")
                                  ).flatMapMany {
                                      Flux.fromIterable(events)
                                  }.flatMap {
                                      Responder.followup(event, EventEmbed.getCondensed(guild, settings, it))
                                  }.then(Mono.just(""))
                              }
                          }
                      }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
                  }
              }.then()
    }

    private fun eventsTodaySubcommand(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        return Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1).flatMap { calNum ->
                  event.interaction.guild.flatMap { guild ->
                      guild.getCalendar(calNum).flatMap { cal ->
                          cal.getEventsInNext24HourPeriod(Instant.now()).collectList().flatMap { events ->
                              if (events.isEmpty()) {
                                  Responder.followup(
                                        event,
                                        getMessage("today.success.none", settings)
                                  )
                              } else if (events.size == 1) {
                                  Responder.followup(
                                        event,
                                        getMessage("today.success.one", settings),
                                        EventEmbed.getFull(guild, settings, events[0])
                                  )
                              } else {
                                  Responder.followup(event,
                                        getMessage("today.success.many", settings, "${events.size}")
                                  ).flatMapMany {
                                      Flux.fromIterable(events)
                                  }.flatMap {
                                      Responder.followup(event, EventEmbed.getCondensed(guild, settings, it))
                                  }.then(Mono.just(""))
                              }
                          }
                      }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
                  }
              }.then()
    }
}
