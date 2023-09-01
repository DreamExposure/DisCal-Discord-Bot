package org.dreamexposure.discal.core.`object`.rest

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.`object`.network.discal.BotInstanceData
import org.dreamexposure.discal.core.`object`.network.discal.InstanceData

@Serializable
data class HeartbeatRequest(
        val type: HeartbeatType,

        @JsonProperty("instance")
        @SerialName("instance")
        val instanceData: InstanceData? = null,

        @JsonProperty("bot_instance")
        @SerialName("bot_instance")
        val botInstanceData: BotInstanceData? = null
)

enum class HeartbeatType {
    BOT, WEBSITE, CAM,
}
