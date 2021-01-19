package org.dreamexposure.discal.core.`object`.web

import discord4j.core.`object`.entity.Role
import discord4j.discordjson.json.RoleData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import org.dreamexposure.discal.core.`object`.GuildSettings

@Suppress("DataClassPrivateConstructor")
@Serializable
data class WebRole private constructor(
        @Serializable(with = LongAsStringSerializer::class)
        val id: Long,
        val name: String,

        val managed: Boolean,
        @SerialName("control_role")
        val controlRole: Boolean,

        val everyone: Boolean,
) {
    companion object {
        fun fromRole(r: Role, settings: GuildSettings): WebRole {
            val controlRole =
                    if (r.isEveryone && settings.controlRole.equals("everyone", true)) true
                    else settings.controlRole == r.id.asString()

            return WebRole(r.id.asLong(), r.name, r.isManaged, controlRole, r.isEveryone)
        }

        fun fromRole(r: RoleData, settings: GuildSettings): WebRole {
            val everyone = r.id() == settings.guildID.asString()
            val controlRole =
                    if (everyone && settings.controlRole.equals("everyone", true)) true
                    else settings.controlRole == r.id()

            return WebRole(r.id().toLong(), r.name(), r.managed(), controlRole, everyone)
        }
    }
}
