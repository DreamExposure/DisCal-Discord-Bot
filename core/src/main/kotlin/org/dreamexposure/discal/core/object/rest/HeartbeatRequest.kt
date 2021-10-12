package org.dreamexposure.discal.core.`object`.rest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.`object`.network.discal.BotInstanceData
import org.dreamexposure.discal.core.`object`.network.discal.InstanceData

@Serializable
data class HeartbeatRequest(
        val type: HeartbeatType,

        @SerialName("instance")
        val instanceData: InstanceData? = null,

        @SerialName("bot_instance")
        val botInstanceData: BotInstanceData? = null
)

enum class HeartbeatType {
    BOT, WEBSITE
}
