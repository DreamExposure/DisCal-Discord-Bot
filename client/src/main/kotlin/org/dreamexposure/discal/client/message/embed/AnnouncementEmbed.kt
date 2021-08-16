package org.dreamexposure.discal.client.message.embed

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.entities.Event
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.extensions.asDiscordTimestamp
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.utils.GlobalVal
import reactor.core.publisher.Mono
import reactor.function.TupleUtils

object AnnouncementEmbed : EmbedMaker {
    fun full(ann: Announcement, event: Event, guild: Guild): Mono<EmbedCreateSpec> {
        return guild.getSettings().map { settings ->
            val builder = defaultBuilder(guild, settings)
                  .color(event.color.asColor())
                  .title(getMessage("announcement", "full.title", settings))

            if (event.name.isNotEmpty())
                builder.addField(getMessage("announcement", "full.field.name", settings), event.name, false)
            if (event.description.isNotEmpty())
                builder.addField(getMessage("announcement", "full.field.desc", settings), event.description, false)

            builder.addField(
                  getMessage("announcement", "full.field.start", settings),
                  event.start.asDiscordTimestamp(),
                  true
            )
            builder.addField(
                  getMessage("announcement", "full.field.end", settings),
                  event.end.asDiscordTimestamp(),
                  true
            )

            builder.addField(getMessage("announcement", "full.field.location", settings), event.location, false)

            if (ann.info.isNotEmpty() || !ann.info.equals("None", true))
                builder.addField(getMessage("announcement", "full.field.info", settings), ann.info, false)

            builder.addField(
                  getMessage("announcement", "full.field.calendar", settings),
                  "${event.calendar.calendarNumber}",
                  true
            )
            builder.addField(getMessage("announcement", "full.field.event", settings), event.eventId, true)

            if (event.image.isNotEmpty()) {
                builder.image(event.image)
            }

            builder.footer(getMessage("announcement", "full.footer", settings, ann.announcementId.toString()), null)

            builder.build()
        }
    }

    fun simple(ann: Announcement, event: Event, guild: Guild): Mono<EmbedCreateSpec> {
        return guild.getSettings().map { settings ->
            val builder = defaultBuilder(guild, settings)
                  .color(event.color.asColor())
                  .title(getMessage("announcement", "simple.title", settings))

            if (event.name.isNotEmpty())
                builder.addField(getMessage("announcement", "simple.field.name", settings), event.name, false)
            if (event.description.isNotEmpty())
                builder.addField(getMessage("announcement", "simple.field.desc", settings), event.description, false)

            builder.addField(
                  getMessage("announcement", "simple.field.start", settings),
                  event.start.asDiscordTimestamp(),
                  true
            )

            builder.addField(getMessage("announcement", "simple.field.location", settings), event.location, false)

            if (ann.info.isNotEmpty() || !ann.info.equals("None", true))
                builder.addField(getMessage("announcement", "simple.field.info", settings), ann.info, false)

            if (event.image.isNotEmpty()) {
                builder.image(event.image)
            }

            builder.footer(getMessage("announcement", "simple.footer", settings, ann.announcementId.toString()), null)

            builder.build()
        }
    }

    fun event(ann: Announcement, event: Event, guild: Guild): Mono<EmbedCreateSpec> {
        return guild.getSettings().map { settings ->
            val builder = defaultBuilder(guild, settings)
                  .color(event.color.asColor())
                  .title(getMessage("announcement", "event.title", settings))

            if (event.name.isNotEmpty())
                builder.addField(getMessage("announcement", "event.field.name", settings), event.name, false)
            if (event.description.isNotEmpty())
                builder.addField(getMessage("announcement", "event.field.desc", settings), event.description, false)

            builder.addField(
                  getMessage("announcement", "event.field.start", settings),
                  event.start.asDiscordTimestamp(),
                  true
            )
            builder.addField(
                  getMessage("announcement", "event.field.end", settings),
                  event.end.asDiscordTimestamp(),
                  true
            )
            builder.addField(getMessage("announcement", "event.field.location", settings), event.location, false)

            builder.addField(
                  getMessage("announcement", "event.field.calendar", settings),
                  "${event.calendar.calendarNumber}",
                  true
            )
            builder.addField(getMessage("announcement", "event.field.event", settings), event.eventId, true)

            if (ann.info.isNotEmpty() || !ann.info.equals("None", true))
                builder.addField(getMessage("announcement", "event.field.info", settings), ann.info, false)

            if (event.image.isNotEmpty()) {
                builder.image(event.image)
            }

            builder.footer(getMessage("announcement", "event.footer", settings, ann.announcementId.toString()), null)

            builder.build()
        }
    }

    fun condensed(ann: Announcement, guild: Guild): Mono<EmbedCreateSpec> {
        return guild.getSettings().map { settings ->
            val builder = defaultBuilder(guild, settings)
                  .title(getMessage("announcement", "con.title", settings))
                  .addField(getMessage("announcement", "con.field.id", settings), ann.announcementId.toString(), false)
                  .addField(getMessage("announcement", "con.field.time", settings), condensedTime(ann), true)
                  .addField(getMessage("announcement", "con.field.enabled", settings), "${ann.enabled}", true)
                  .footer(getMessage("announcement", "con.footer", settings, ann.type.name, ann.modifier.name), null)

            if (ann.type == AnnouncementType.COLOR)
                builder.color(ann.eventColor.asColor())
            else
                builder.color(GlobalVal.discalColor)

            builder.build()
        }
    }

    fun view(ann: Announcement, guild: Guild): Mono<EmbedCreateSpec> {
        val channelMono = guild
              .getChannelById(Snowflake.of(ann.announcementChannelId))
              .map(GuildChannel::getName)


        return Mono.zip(guild.getSettings(), channelMono).map(TupleUtils.function { settings, channel ->
            val builder = defaultBuilder(guild, settings)
                  .title(getMessage("announcement", "view.title", settings))
                  .addField(getMessage("announcement", "view.field.type", settings), ann.type.name, true)
                  .addField(getMessage("announcement", "view.field.modifier", settings), ann.modifier.name, true)
                  .addField(getMessage("announcement", "view.field.channel", settings), channel, false)
                  .addField(getMessage("announcement", "view.field.hours", settings), "${ann.hoursBefore}", true)
                  .addField(getMessage("announcement", "view.field.minutes", settings), "${ann.minutesBefore}", true)

            if (ann.info.isNotEmpty() || !ann.info.equals("None", true)) {
                builder.addField(getMessage("announcement", "view.field.info", settings), ann.info, false)
            }

            builder.addField(getMessage("announcement", "view.field.calendar", settings), "${ann.calendarNumber}", true)
            if (ann.type == AnnouncementType.RECUR || ann.type == AnnouncementType.SPECIFIC)
                builder.addField(getMessage("announcement", "view.field.event", settings), ann.eventId, true)

            if (ann.type == AnnouncementType.COLOR) {
                builder.color(ann.eventColor.asColor())
                builder.addField(getMessage("announcement", "view.field.color", settings), ann.eventColor.name, true)
            } else
                builder.color(GlobalVal.discalColor)

            builder.addField(getMessage("announcement", "view.field.id", settings), ann.announcementId.toString(), false)
                  .addField(getMessage("announcement", "view.field.enabled", settings), "${ann.enabled}", true)
                  .addField(getMessage("announcement", "view.field.publish", settings), "${ann.publish}", true)
                  .build()
        })
    }

    private fun condensedTime(a: Announcement): String = "${a.hoursBefore}H${a.minutesBefore}m"
}
