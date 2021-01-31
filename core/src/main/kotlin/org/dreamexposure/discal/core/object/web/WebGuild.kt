package org.dreamexposure.discal.core.`object`.web

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.discordjson.json.GuildUpdateData
import discord4j.discordjson.json.MemberData
import discord4j.discordjson.possible.Possible
import discord4j.rest.entity.RestGuild
import discord4j.rest.util.Image
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.GuildSettings
import org.dreamexposure.discal.core.`object`.announcement.Announcement
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.exceptions.BotNotInGuildException
import org.dreamexposure.discal.core.utils.GuildUtils
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.justOrEmpty
import reactor.function.TupleUtils

@Serializable
data class WebGuild(
        @Serializable(with = LongAsStringSerializer::class)
        val id: Long,
        val name: String,
        @SerialName("icon_url")
        val iconUrl: String? = null,

        val settings: GuildSettings,

        @SerialName("bot_nick")
        val botNick: String? = null,

        @SerialName("manage_server")
        var manageServer: Boolean = false,
        @SerialName("discal_role")
        var discalRole: Boolean = false,

        val calendar: WebCalendar
) {
    val roles: MutableList<WebRole> = mutableListOf()
    val channels: MutableList<WebChannel> = mutableListOf()
    val announcements: MutableList<Announcement> = mutableListOf()

    @SerialName("available_langs")
    val availableLangs: MutableList<String> = mutableListOf()

    val shard = GuildUtils.findShard(Snowflake.of(this.id))

    companion object {
        @JvmStatic
        @Throws(BotNotInGuildException::class)
        fun fromGuild(g: RestGuild): Mono<WebGuild> {
            return g.data.flatMap { data: GuildUpdateData ->
                val id = Snowflake.of(data.id())
                val name = data.name()
                val ico = data.icon().orElse("")

                val botNick = g.member(Snowflake.of(BotSettings.ID.get()))
                        .data
                        .map(MemberData::nick)
                        .map { Possible.flatOpt(it) }
                        .flatMap { justOrEmpty(it) }
                        .defaultIfEmpty("DisCal")

                val settings = DatabaseManager.getSettings(id).cache()

                val roles = settings.flatMapMany { s ->
                    g.roles.map { role -> WebRole.fromRole(role, s) }
                }.collectList()

                val webChannels = settings.flatMapMany { s ->
                    g.channels.ofType(GuildMessageChannel::class.java)
                            .map { channel -> WebChannel.fromChannel(channel, s) }
                }.collectList()

                val announcements = DatabaseManager.getAnnouncements(id)

                val calendar = settings.flatMap { s ->
                    DatabaseManager.getMainCalendar(id)
                            .flatMap { d -> WebCalendar.fromCalendar(d, s) }
                }.defaultIfEmpty(WebCalendar.empty())


                Mono.zip(botNick, settings, roles, webChannels, announcements, calendar)
                        .map(TupleUtils.function { bn, s, r, wc, a, c ->
                            WebGuild(id.asLong(), name, ico, s, bn, manageServer = false, discalRole = false, c).apply {
                                this.roles.addAll(r)
                                this.channels.add(WebChannel.all(s))
                                this.channels.addAll(wc)
                                this.announcements.addAll(a)
                            }
                        })
            }.switchIfEmpty(Mono.error(BotNotInGuildException()))
        }

        @JvmStatic
        fun fromGuild(g: Guild): Mono<WebGuild> {
            val id = g.id.asLong()
            val name = g.name
            val icon = g.getIconUrl(Image.Format.PNG).orElse(null)

            val botNick = g.getMemberById(Snowflake.of(BotSettings.ID.get()))
                    .map(Member::getNickname)
                    .flatMap { justOrEmpty(it) }
                    .defaultIfEmpty("DisCal")

            val settings = DatabaseManager.getSettings(g.id).cache()

            val roles = settings.flatMapMany { s ->
                g.roles.map { role -> WebRole.fromRole(role, s) }
            }.collectList()

            val channels = settings.flatMapMany { s ->
                g.channels
                        .ofType(GuildMessageChannel::class.java)
                        .map { channel -> WebChannel.fromChannel(channel, s) }
            }.collectList()

            val announcements = DatabaseManager.getAnnouncements(g.id)

            val calendar = settings.flatMap { s ->
                DatabaseManager.getMainCalendar(Snowflake.of(id))
                        .flatMap { d -> WebCalendar.fromCalendar(d, s) }
            }

            return Mono.zip(botNick, settings, roles, channels, announcements, calendar)
                    .map(TupleUtils.function { bn, s, r, wc, a, c ->
                        WebGuild(id, name, icon, s, bn, manageServer = false, discalRole = false, c).apply {
                            this.roles.addAll(r)
                            this.channels.add(WebChannel.all(s))
                            this.channels.addAll(wc)
                            this.announcements.addAll(a)
                        }
                    })
        }
    }
}
