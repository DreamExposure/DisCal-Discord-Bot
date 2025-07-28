package org.dreamexposure.discal.core.`object`.new.model.discal

data class NetworkDataV3Model(
    val totalCalendars: Int = 0,
    val totalAnnouncements: Int = 0,

    val apiStatus: List<InstanceDataV3Model> = emptyList(),
    val camStatus: List<InstanceDataV3Model> = emptyList(),
    val websiteStatus: InstanceDataV3Model? = null,
    val botStatus: List<BotInstanceDataV3Model> = emptyList(),
) {
    val expectedShardCount: Int
        get() = botStatus.getOrNull(0)?.shardCount ?: 0
    val totalGuildsCount: Int
        get() = botStatus.sumOf(BotInstanceDataV3Model::guilds)
}