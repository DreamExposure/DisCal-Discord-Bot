package org.dreamexposure.discal.core.`object`.web

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import discord4j.discordjson.json.GuildUpdateData
import discord4j.discordjson.json.MemberData
import discord4j.discordjson.possible.Possible
import discord4j.rest.entity.RestGuild
import discord4j.rest.http.client.ClientException
import discord4j.rest.util.Image
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import org.dreamexposure.discal.Application.Companion.getShardCount
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.exceptions.BotNotInGuildException
import org.dreamexposure.discal.core.extensions.discord4j.getMainCalendar
import org.dreamexposure.discal.core.`object`.GuildSettings
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.justOrEmpty
import reactor.function.TupleUtils

@Deprecated("Yeah, this is a disaster")
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

      @SerialName("elevated_access")
      var elevatedAccess: Boolean = false,
      @SerialName("discal_role")
      var discalRole: Boolean = false,

      //TODO: Support multi-cal
      val calendar: WebCalendar
) {
    val roles: MutableList<WebRole> = mutableListOf()
    val channels: MutableList<WebChannel> = mutableListOf()
    val announcements: List<String> = emptyList()

    @SerialName("available_langs")
    val availableLangs: MutableList<String> = mutableListOf()

    val shard = (id shr 22) % getShardCount()

    companion object {
        @Throws(BotNotInGuildException::class)
        fun fromGuild(g: RestGuild): Mono<WebGuild> {
            return g.data.flatMap { data: GuildUpdateData ->
                val id = Snowflake.of(data.id())
                val name = data.name()
                val ico = data.icon().orElse("")

                val botNick = g.selfMember
                      .map(MemberData::nick)
                      .map { Possible.flatOpt(it) }
                      .flatMap { justOrEmpty(it) }
                      .defaultIfEmpty("DisCal")

                val settings = DatabaseManager.getSettings(id).cache()

                val roles = settings.flatMapMany { s ->
                    g.roles.map { role -> WebRole.fromRole(role, s) }
                }.collectList()

                val webChannels = g.channels.ofType(GuildMessageChannel::class.java)
                      .map { channel -> WebChannel.fromChannel(channel) }
                      .collectList()

                //TODO: Support multi-cal
                val calendar = g.getMainCalendar()
                    .map { it.toWebCalendar() }
                    .defaultIfEmpty(WebCalendar.empty())


                Mono.zip(botNick, settings, roles, webChannels, calendar)
                      .map(TupleUtils.function { bn, s, r, wc, c ->
                          WebGuild(id.asLong(), name, ico, s, bn, elevatedAccess = false, discalRole = false, c).apply {
                              this.roles.addAll(r)
                              this.channels.addAll(wc)
                          }
                      })
            }.onErrorResume(ClientException::class.java) {
                Mono.error(BotNotInGuildException())
            }.switchIfEmpty(Mono.error(BotNotInGuildException()))
        }

        fun fromGuild(g: Guild): Mono<WebGuild> {
            val id = g.id.asLong()
            val name = g.name
            val icon = g.getIconUrl(Image.Format.PNG).orElse(null)

            val botNick = g.selfMember
                  .map(Member::getNickname)
                  .flatMap { justOrEmpty(it) }
                  .defaultIfEmpty("DisCal")

            val settings = DatabaseManager.getSettings(g.id).cache()

            val roles = settings.flatMapMany { s ->
                g.roles.map { role -> WebRole.fromRole(role, s) }
            }.collectList()

            val channels = g.channels
                  .ofType(GuildMessageChannel::class.java)
                  .map { channel -> WebChannel.fromChannel(channel) }
                  .collectList()

            //TODO: Support multi-cal
            val calendar = g.getMainCalendar()
                .map { it.toWebCalendar() }
                .defaultIfEmpty(WebCalendar.empty())

            return Mono.zip(botNick, settings, roles, channels, calendar)
                  .map(TupleUtils.function { bn, s, r, wc, c ->
                      WebGuild(id, name, icon, s, bn, elevatedAccess = false, discalRole = false, c).apply {
                          this.roles.addAll(r)
                          this.channels.addAll(wc)
                      }
                  })
        }
    }
}
