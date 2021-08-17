package org.dreamexposure.discal.client.message.embed

import discord4j.core.`object`.entity.Guild
import discord4j.core.spec.EmbedCreateSpec
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.extensions.discord4j.getSettings
import org.dreamexposure.discal.core.utils.GlobalVal
import reactor.core.publisher.Mono
import reactor.function.TupleUtils

object DiscalEmbed : EmbedMaker {

    fun info(guild: Guild): Mono<EmbedCreateSpec> {
        val gMono = guild.client.guilds.count().map(Long::toInt)
        val cMono = DatabaseManager.getCalendarCount()
        val aMono = DatabaseManager.getAnnouncementCount()

        return Mono.zip(gMono, cMono, aMono, guild.getSettings())
              .map(TupleUtils.function { guilds, cal, ann, settings ->
                  defaultBuilder(guild, settings)
                        .color(GlobalVal.discalColor)
                        .title(getMessage("discal", "info.title", settings))
                        .addField(getMessage("discal", "info.field.dev", settings), "DreamExposure", true)
                        .addField(getMessage("discal", "info.field.version", settings), GlobalVal.version, true)
                        .addField(getMessage("discal", "info.field.library", settings), GlobalVal.d4jVersion, false)
                        .addField(getMessage("discal", "info.field.shard", settings), formattedIndex(), true)
                        .addField(getMessage("discal", "info.field.guilds", settings), "$guilds", true)
                        .addField(
                              getMessage("discord", "info.field.uptime", settings),
                              Application.getHumanReadableUptime(),
                              false
                        ).addField(getMessage("discal", "info.field.calendars", settings), "$cal", true)
                        .addField(getMessage("discal", "info.field.announcements", settings), "$ann", true)
                        .addField(getMessage("discal", "info.field.links", settings),
                              getMessage("discal",
                                    "info.field.links.value",
                                    settings,
                                    "${BotSettings.BASE_URL.get()}/commands",
                                    BotSettings.INVITE_URL.get(),
                                    "https://www.patreon.com/Novafox"
                              ),
                              false
                        ).footer(getMessage("discal", "info.footer", settings), null)
                        .build()
              })
    }

    private fun formattedIndex() = "${Application.getShardIndex()}/${Application.getShardCount()}"
}
