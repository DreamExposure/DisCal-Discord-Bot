package org.dreamexposure.discal.core.`object`.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import org.dreamexposure.discal.Application.Companion.getShardCount
import org.dreamexposure.discal.core.`object`.GuildSettings

@Deprecated("Yeah, this is a disaster, but now its slightly less of a disaster as the disater has been moved")
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
}
