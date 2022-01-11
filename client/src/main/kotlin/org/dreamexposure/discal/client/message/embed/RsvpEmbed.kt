package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.event.RsvpData
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.extensions.asStringList
import org.dreamexposure.discal.core.extensions.discord4j.getMembersFromId
import reactor.core.publisher.Mono
import reactor.function.TupleUtils

object RsvpEmbed : EmbedMaker {
    fun list(guild: Guild, settings: GuildSettings, event: Event): Mono<EmbedCreateSpec> {
        return event.getRsvp().flatMap { list(guild, settings, event, it) }
    }

    fun list(guild: Guild, settings: GuildSettings, event: Event, rsvp: RsvpData): Mono<EmbedCreateSpec> {
        val roleMono = Mono.justOrEmpty(rsvp.roleId)
            .flatMap { guild.getRoleById(it) }
            .map(Role::getName)
            .defaultIfEmpty("None")

        val onTimeMono = guild.getMembersFromId(rsvp.goingOnTime)
            .map(Member::getUsername)
            .collectList()
            .map(MutableList<String>::asStringList)
            .map { it.ifEmpty { "N/a" } }

        val lateMono = guild.getMembersFromId(rsvp.goingLate)
            .map(Member::getUsername)
            .collectList()
            .map(MutableList<String>::asStringList)
            .map { it.ifEmpty { "N/a" } }

        val undecidedMono = guild.getMembersFromId(rsvp.undecided)
            .map(Member::getUsername)
            .collectList()
            .map(MutableList<String>::asStringList)
            .map { it.ifEmpty { "N/a" } }

        val notMono = guild.getMembersFromId(rsvp.notGoing)
            .map(Member::getUsername)
            .collectList()
            .map(MutableList<String>::asStringList)
            .map { it.ifEmpty { "N/a" } }

        //TODO: Waitlist users (show up to 3, with (+X) if there are more)
        val waitListMono = Mono.just("User1, User2 + 3 more")

        return Mono.zip(roleMono, onTimeMono, lateMono, undecidedMono, notMono, waitListMono)
            .map(TupleUtils.function { role, onTime, late, undecided, notGoing, waitList ->
                val limitValue = if (rsvp.limit < 0) {
                    getMessage("rsvp", "list.field.limit.value", settings, "${rsvp.getCurrentCount()}")
                } else "${rsvp.getCurrentCount()}/${rsvp.limit}"

                defaultBuilder(guild, settings)
                    .color(event.color.asColor())
                    .title(getMessage("rsvp", "list.title", settings))
                    .addField(getMessage("rsvp", "list.field.event", settings), rsvp.eventId, false)
                    .addField(getMessage("rsvp", "list.field.limit", settings), limitValue, true)
                    .addField(getMessage("rsvp", "list.field.role", settings), role, true)
                    .addField(getMessage("rsvp", "list.field.onTime", settings), onTime, false)
                    .addField(getMessage("rsvp", "list.field.late", settings), late, false)
                    .addField(getMessage("rsvp", "list.field.unsure", settings), undecided, false)
                    .addField(getMessage("rsvp", "list.field.notGoing", settings), notGoing, false)
                    .addField(getMessage("rsvp", "list.field.waitlist", settings), waitList, false)
                    .footer(getMessage("rsvp", "list.footer", settings), null)
                    .build()
            })
    }
}
