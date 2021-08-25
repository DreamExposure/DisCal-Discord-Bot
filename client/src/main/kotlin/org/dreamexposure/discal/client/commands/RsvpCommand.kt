package org.dreamexposure.discal.client.commands

import discord4j.core.event.domain.interaction.SlashCommandEvent
import org.dreamexposure.discal.client.message.Responder
import org.dreamexposure.discal.client.message.embed.RsvpEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.getCalendar
import org.dreamexposure.discal.core.extensions.discord4j.hasControlRole
import org.dreamexposure.discal.core.extensions.discord4j.hasElevatedPermissions
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.function.TupleUtils.function

@Component
class RsvpCommand : SlashCommand {
    override val name = "rsvp"
    override val ephemeral = true

    override fun handle(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        return when (event.options[0].name) {
            "ontime" -> onTime(event, settings)
            "late" -> late(event, settings)
            "not-going" -> notGoing(event, settings)
            "unsure" -> unsure(event, settings)
            "remove" -> remove(event, settings)
            "list" -> list(event, settings)
            "limit" -> limit(event, settings)
            "role" -> role(event, settings)
            else -> Mono.empty() //Never can reach this, makes compiler happy.
        }
    }

    private fun onTime(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val eventIdMono = Mono.justOrEmpty(event.options[0].getOption("event").flatMap { it.value })
              .map { it.asString() }

        val mMono = Mono.justOrEmpty(event.interaction.member)

        return Mono.zip(event.interaction.guild, mMono, calNumMono, eventIdMono).flatMap(function { guild, member, calNum, eventId ->
            guild.getCalendar(calNum).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        calEvent.getRsvp()
                              .filter { it.hasRoom(member.id.asString()) }
                              .flatMap { it.removeCompletely(member).thenReturn(it) }
                              .flatMap { it.addGoingOnTime(member).thenReturn(it) }
                              .flatMap { calEvent.updateRsvp(it) }
                              .then(RsvpEmbed.list(guild, settings, calEvent))
                              .flatMap {
                                  Responder.followupEphemeral(event, getMessage("onTime.success", settings), it)
                              }.switchIfEmpty(Responder.followupEphemeral(
                                    event,
                                    getMessage("onTime.failure.limit", settings)
                              ))
                    } else {
                        Responder.followupEphemeral(event, getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
        }).then()
    }

    private fun late(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val eventIdMono = Mono.justOrEmpty(event.options[0].getOption("event").flatMap { it.value })
              .map { it.asString() }

        val mMono = Mono.justOrEmpty(event.interaction.member)

        return Mono.zip(event.interaction.guild, mMono, calNumMono, eventIdMono).flatMap(function { guild, member, calNum, eventId ->
            guild.getCalendar(calNum).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        calEvent.getRsvp()
                              .filter { it.hasRoom(member.id.asString()) }
                              .flatMap { it.removeCompletely(member).thenReturn(it) }
                              .flatMap { it.addGoingLate(member).thenReturn(it) }
                              .flatMap { calEvent.updateRsvp(it) }
                              .then(RsvpEmbed.list(guild, settings, calEvent))
                              .flatMap {
                                  Responder.followupEphemeral(event, getMessage("late.success", settings), it)
                              }.switchIfEmpty(Responder.followupEphemeral(
                                    event,
                                    getMessage("late.failure.limit", settings)
                              ))
                    } else {
                        Responder.followupEphemeral(event, getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
        }).then()
    }

    private fun unsure(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val eventIdMono = Mono.justOrEmpty(event.options[0].getOption("event").flatMap { it.value })
              .map { it.asString() }

        val mMono = Mono.justOrEmpty(event.interaction.member)

        return Mono.zip(event.interaction.guild, mMono, calNumMono, eventIdMono).flatMap(function { guild, member, calNum, eventId ->
            guild.getCalendar(calNum).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        calEvent.getRsvp()
                              .flatMap { it.removeCompletely(member).thenReturn(it) }
                              .doOnNext { it.undecided.add(member.id.asString()) }
                              .flatMap { calEvent.updateRsvp(it) }
                              .then(RsvpEmbed.list(guild, settings, calEvent))
                              .flatMap {
                                  Responder.followupEphemeral(event, getMessage("unsure.success", settings), it)
                              }
                    } else {
                        Responder.followupEphemeral(event, getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
        }).then()
    }

    private fun notGoing(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val eventIdMono = Mono.justOrEmpty(event.options[0].getOption("event").flatMap { it.value })
              .map { it.asString() }

        val mMono = Mono.justOrEmpty(event.interaction.member)

        return Mono.zip(event.interaction.guild, mMono, calNumMono, eventIdMono).flatMap(function { guild, member, calNum, eventId ->
            guild.getCalendar(calNum).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        calEvent.getRsvp()
                              .flatMap { it.removeCompletely(member).thenReturn(it) }
                              .doOnNext { it.notGoing.add(member.id.asString()) }
                              .flatMap { calEvent.updateRsvp(it) }
                              .then(RsvpEmbed.list(guild, settings, calEvent))
                              .flatMap {
                                  Responder.followupEphemeral(event, getMessage("notGoing.success", settings), it)
                              }
                    } else {
                        Responder.followupEphemeral(event, getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
        }).then()
    }

    private fun remove(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val eventIdMono = Mono.justOrEmpty(event.options[0].getOption("event").flatMap { it.value })
              .map { it.asString() }

        val mMono = Mono.justOrEmpty(event.interaction.member)

        return Mono.zip(event.interaction.guild, mMono, calNumMono, eventIdMono).flatMap(function { guild, member, calNum, eventId ->
            guild.getCalendar(calNum).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        calEvent.getRsvp()
                              .flatMap { it.removeCompletely(member).thenReturn(it) }
                              .flatMap { calEvent.updateRsvp(it) }
                              .then(RsvpEmbed.list(guild, settings, calEvent))
                              .flatMap {
                                  Responder.followupEphemeral(event, getMessage("remove.success", settings), it)
                              }
                    } else {
                        Responder.followupEphemeral(event, getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
        }).then()
    }

    private fun list(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        val calNumMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val eventIdMono = Mono.justOrEmpty(event.options[0].getOption("event").flatMap { it.value })
              .map { it.asString() }

        return Mono.zip(event.interaction.guild, calNumMono, eventIdMono).flatMap(function { guild, calNum, eventId ->
            guild.getCalendar(calNum).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    calEvent.getRsvp()
                          .then(RsvpEmbed.list(guild, settings, calEvent))
                          .flatMap { Responder.followupEphemeral(event, it) }
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
        }).then()
    }

    private fun limit(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        val cMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val eMono = Mono.justOrEmpty(event.options[0].getOption("event").flatMap { it.value })
              .map { it.asString() }

        val lMono = Mono.justOrEmpty(event.options[0].getOption("limit").flatMap { it.value })
              .map { it.asLong().toInt() }

        val gMono = event.interaction.guild

        val mMono = Mono.justOrEmpty(event.interaction.member)

        return Mono.zip(gMono, mMono, cMono, eMono, lMono).flatMap(function { guild, member, calNum, eventId, limit ->
            member.hasControlRole().filter { it }.flatMap {
                guild.getCalendar(calNum).flatMap { cal ->
                    cal.getEvent(eventId).flatMap { calEvent ->
                        if (!calEvent.isOver()) {
                            calEvent.getRsvp()
                                  .doOnNext { it.limit = limit }
                                  .flatMap { calEvent.updateRsvp(it) }
                                  .then(RsvpEmbed.list(guild, settings, calEvent))
                                  .flatMap {
                                      Responder.followupEphemeral(
                                            event,
                                            getMessage("limit.success", settings, "$limit"),
                                            it
                                      )
                                  }
                        } else {
                            Responder.followupEphemeral(event, getCommonMsg("error.event.ended", settings))
                        }
                    }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.event", settings)))
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
            }.switchIfEmpty(Responder.followupEphemeral(event, getMessage("error.perms.privileged", settings)))
        }).then()
    }

    private fun role(event: SlashCommandEvent, settings: GuildSettings): Mono<Void> {
        val cMono = Mono.justOrEmpty(event.options[0].getOption("calendar").flatMap { it.value })
              .map { it.asLong().toInt() }
              .defaultIfEmpty(1)

        val eMono = Mono.justOrEmpty(event.options[0].getOption("event").flatMap { it.value })
              .map { it.asString() }

        val rMono = Mono.justOrEmpty(event.options[0].getOption("role").flatMap { it.value })
              .flatMap { it.asRole() }

        val gMono = event.interaction.guild

        val mMono = Mono.justOrEmpty(event.interaction.member)

        return Mono.zip(gMono, mMono, cMono, eMono, rMono).flatMap(function { guild, member, calNum, eventId, role ->
            if (!settings.patronGuild || !settings.devGuild) {
                return@function Responder.followupEphemeral(event, getMessage("error.patronOnly", settings))
            }

            member.hasElevatedPermissions().filter { it }.flatMap {
                guild.getCalendar(calNum).flatMap { cal ->
                    cal.getEvent(eventId).flatMap { calEvent ->
                        if (!calEvent.isOver()) {
                            calEvent.getRsvp().flatMap { rsvp ->
                                if (role.isEveryone) {
                                    rsvp.clearRole(member.client.rest())
                                          .then(calEvent.updateRsvp(rsvp))
                                          .then(RsvpEmbed.list(guild, settings, calEvent))
                                          .flatMap {
                                              Responder.followupEphemeral(
                                                    event,
                                                    getMessage("role.success.remove", settings),
                                                    it
                                              )
                                          }
                                } else {
                                    rsvp.setRole(role)
                                          .then(calEvent.updateRsvp(rsvp))
                                          .then(RsvpEmbed.list(guild, settings, calEvent))
                                          .flatMap {
                                              Responder.followupEphemeral(
                                                    event,
                                                    getMessage("role.success.set", settings, role.name),
                                                    it
                                              )
                                          }
                                }
                            }
                        } else {
                            Responder.followupEphemeral(event, getCommonMsg("error.event.ended", settings))
                        }
                    }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.event", settings)))
                }.switchIfEmpty(Responder.followupEphemeral(event, getCommonMsg("error.notFound.calendar", settings)))
            }.switchIfEmpty(Responder.followupEphemeral(event, getMessage("error.perms.elevated", settings)))
        }).then()
    }
}
