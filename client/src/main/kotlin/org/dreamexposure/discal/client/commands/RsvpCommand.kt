package org.dreamexposure.discal.client.commands

import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import org.dreamexposure.discal.client.message.embed.RsvpEmbed
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.extensions.discord4j.followupEphemeral
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

    override fun handle(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
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

    //TODO: Add to waitlist if there is no room remaining.
    private fun onTime(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.guild.flatMap { guild ->
            guild.getCalendar(calendarNumber).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        val member = event.interaction.member.get()
                        calEvent.getRsvp()
                            .filter { it.hasRoom(member.id.asString()) }
                            .flatMap { it.removeCompletely(member).thenReturn(it) }
                            .flatMap { it.addGoingOnTime(member).thenReturn(it) }
                            .flatMap { calEvent.updateRsvp(it).thenReturn(it) }
                            .flatMap { RsvpEmbed.list(guild, settings, calEvent, it) }
                            .flatMap {
                                event.followupEphemeral(getMessage("onTime.success", settings), it)
                            }.switchIfEmpty(event.followupEphemeral(getMessage("onTime.failure.limit", settings)))
                    } else {
                        event.followupEphemeral(getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }
    }

    //TODO: Add to waitlist if there is no room remaining.
    private fun late(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.guild.flatMap { guild ->
            guild.getCalendar(calendarNumber).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        val member = event.interaction.member.get()
                        calEvent.getRsvp()
                            .filter { it.hasRoom(member.id.asString()) }
                            .flatMap { it.removeCompletely(member).thenReturn(it) }
                            .flatMap { it.addGoingLate(member).thenReturn(it) }
                            .flatMap { calEvent.updateRsvp(it).thenReturn(it) }
                            .flatMap { RsvpEmbed.list(guild, settings, calEvent, it) }
                            .flatMap {
                                event.followupEphemeral(getMessage("late.success", settings), it)
                            }.switchIfEmpty(event.followupEphemeral(getMessage("late.failure.limit", settings)))
                    } else {
                        event.followupEphemeral(getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }
    }

    private fun unsure(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.guild.flatMap { guild ->
            guild.getCalendar(calendarNumber).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        val member = event.interaction.member.get()
                        calEvent.getRsvp()
                            .flatMap { it.removeCompletely(member).thenReturn(it) }
                            .doOnNext { it.undecided.add(member.id.asString()) }
                            .flatMap { calEvent.updateRsvp(it).thenReturn(it) }
                            .flatMap { RsvpEmbed.list(guild, settings, calEvent, it) }
                            .flatMap {
                                event.followupEphemeral(getMessage("unsure.success", settings), it)
                            }
                    } else {
                        event.followupEphemeral(getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }
    }

    private fun notGoing(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.guild.flatMap { guild ->
            guild.getCalendar(calendarNumber).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        val member = event.interaction.member.get()
                        calEvent.getRsvp()
                            .flatMap { it.removeCompletely(member).thenReturn(it) }
                            .doOnNext { it.notGoing.add(member.id.asString()) }
                            .flatMap { calEvent.updateRsvp(it).thenReturn(it) }
                            .flatMap { RsvpEmbed.list(guild, settings, calEvent, it) }
                            .flatMap {
                                event.followupEphemeral(getMessage("notGoing.success", settings), it)
                            }
                    } else {
                        event.followupEphemeral(getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }
    }

    private fun remove(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.guild.flatMap { guild ->
            guild.getCalendar(calendarNumber).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    if (!calEvent.isOver()) {
                        val member = event.interaction.member.get()
                        calEvent.getRsvp().flatMap { rsvp ->
                            // Add next person on waitlist if this user was previously going to attend
                            rsvp.removeCompletely(member, true)
                                .then(calEvent.updateRsvp(rsvp))
                                .then(RsvpEmbed.list(guild, settings, calEvent, rsvp))
                                .flatMap { event.followupEphemeral(getMessage("remove.success", settings), it) }
                        }
                    } else {
                        event.followupEphemeral(getCommonMsg("error.event.ended", settings))
                    }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }
    }

    private fun list(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        return event.interaction.guild.flatMap { guild ->
            guild.getCalendar(calendarNumber).flatMap { cal ->
                cal.getEvent(eventId).flatMap { calEvent ->
                    RsvpEmbed.list(guild, settings, calEvent).flatMap { event.followupEphemeral(it) }
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
        }
    }

    //TODO: If limit is increased, make sure to add users who are on the waitlist
    private fun limit(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val limit = event.options[0].getOption("limit")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .get()

        return Mono.justOrEmpty(event.interaction.member)
            .filterWhen(Member::hasControlRole)
            .flatMap { event.interaction.guild }
            .flatMap { guild ->
                guild.getCalendar(calendarNumber).flatMap { cal ->
                    cal.getEvent(eventId).flatMap { calEvent ->
                        if (!calEvent.isOver()) {
                            calEvent.getRsvp()
                                .doOnNext { it.limit = limit }
                                .flatMap { calEvent.updateRsvp(it).thenReturn(it) }
                                .flatMap { RsvpEmbed.list(guild, settings, calEvent, it) }
                                .flatMap {
                                    event.followupEphemeral(getMessage("limit.success", settings, "$limit"), it)
                                }
                        } else {
                            event.followupEphemeral(getCommonMsg("error.event.ended", settings))
                        }
                    }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
            }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.privileged", settings)))
    }

    private fun role(event: ChatInputInteractionEvent, settings: GuildSettings): Mono<Message> {
        val calendarNumber = event.options[0].getOption("calendar")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .map(Long::toInt)
            .orElse(1)

        val eventId = event.options[0].getOption("event")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .get()

        val roleMono = Mono.justOrEmpty(
            event.options[0].getOption("role")
                .flatMap(ApplicationCommandInteractionOption::getValue)
        ).flatMap(ApplicationCommandInteractionOptionValue::asRole)

        return Mono.zip(event.interaction.guild, roleMono).flatMap(function { guild, role ->
            if (!settings.patronGuild || !settings.devGuild) {
                return@function event.followupEphemeral(getCommonMsg("error.patronOnly", settings))
            }

            Mono.justOrEmpty(event.interaction.member)
                .filterWhen(Member::hasElevatedPermissions)
                .flatMap { member ->
                    guild.getCalendar(calendarNumber).flatMap { cal ->
                        cal.getEvent(eventId).flatMap { calEvent ->
                            if (!calEvent.isOver()) {
                                calEvent.getRsvp().flatMap { rsvp ->
                                    if (role.isEveryone) {
                                        rsvp.clearRole(member.client.rest())
                                            .then(calEvent.updateRsvp(rsvp))
                                            .flatMap { RsvpEmbed.list(guild, settings, calEvent, rsvp) }
                                            .flatMap {
                                                event.followupEphemeral(getMessage("role.success.remove", settings), it)
                                            }
                                    } else {
                                        rsvp.setRole(role)
                                            .then(calEvent.updateRsvp(rsvp))
                                            .then(RsvpEmbed.list(guild, settings, calEvent, rsvp))
                                            .flatMap {
                                                event.followupEphemeral(
                                                    getMessage("role.success.set", settings, role.name),
                                                    it
                                                )
                                            }
                                    }
                                }
                            } else {
                                event.followupEphemeral(getCommonMsg("error.event.ended", settings))
                            }
                        }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.event", settings)))
                    }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.notFound.calendar", settings)))
                }.switchIfEmpty(event.followupEphemeral(getCommonMsg("error.perms.elevated", settings)))
        })
    }
}
