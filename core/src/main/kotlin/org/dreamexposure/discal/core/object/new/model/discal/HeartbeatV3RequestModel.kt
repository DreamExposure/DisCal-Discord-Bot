package org.dreamexposure.discal.core.`object`.new.model.discal

data class HeartbeatV3RequestModel(
    val type: Type,
    val instance: InstanceDataV3Model? = null,
    val bot: BotInstanceDataV3Model? = null,
) {
    enum class Type {
        BOT, WEBSITE, CAM,
    }
}