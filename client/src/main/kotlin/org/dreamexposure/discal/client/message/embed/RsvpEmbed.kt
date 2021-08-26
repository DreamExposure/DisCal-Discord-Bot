package org.dreamexposure.discal.client.message.embed

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.extensions.asStringList
import org.dreamexposure.discal.core.extensions.discord4j.getMembersFromId
import reactor.core.publisher.Mono
import reactor.function.TupleUtils

object RsvpEmbed : EmbedMaker {
    fun list(guild: Guild, settings: GuildSettings, event: Event): Mono<EmbedCreateSpec> {

        val rsvpMono = event.getRsvp().cache()

        val roleMono = rsvpMono
              .flatMap<Snowflake> { Mono.justOrEmpty(it.roleId) }
              .flatMap { guild.getRoleById(it) }
              .map(Role::getName)
              .defaultIfEmpty("None")

        val onTimeMono = rsvpMono
              .flatMapMany { guild.getMembersFromId(it.goingOnTime) }
              .map(Member::getUsername)
              .collectList()
              .map(MutableList<String>::asStringList)
              .map { it.ifEmpty { "N/a" } }

        val lateMono = rsvpMono
              .flatMapMany { guild.getMembersFromId(it.goingLate) }
              .map(Member::getUsername)
              .collectList()
              .map(MutableList<String>::asStringList)
              .map { it.ifEmpty { "N/a" } }

        val undecidedMono = rsvpMono
              .flatMapMany { guild.getMembersFromId(it.undecided) }
              .map(Member::getUsername)
              .collectList()
              .map(MutableList<String>::asStringList)
              .map { it.ifEmpty { "N/a" } }

        val notMono = rsvpMono
              .flatMapMany { guild.getMembersFromId(it.notGoing) }
              .map(Member::getUsername)
              .collectList()
              .map(MutableList<String>::asStringList)
              .map { it.ifEmpty { "N/a" } }

        return Mono.zip(rsvpMono, roleMono, onTimeMono, lateMono, undecidedMono, notMono)
              .map(TupleUtils.function { rsvp, role, onTime, late, undecided, notGoing ->
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
                        .footer(getMessage("rsvp", "list.footer", settings), null)
                        .build()
              })
    }
}
