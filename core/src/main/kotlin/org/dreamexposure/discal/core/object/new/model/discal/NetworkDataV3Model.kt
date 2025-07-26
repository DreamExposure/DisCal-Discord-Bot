package org.dreamexposure.discal.core.`object`.new.model.discal

data class NetworkDataV3Model(
    val totalCalendars: Int = 0,
    val totalAnnouncements: Int = 0,

    val apiStatus: List<InstanceDataV3Model> = emptyList(),
    val camStatus: List<InstanceDataV3Model> = emptyList(),
    val websiteStatus: InstanceDataV3Model? = null,
    val botStatus: List<BotInstanceDataV3Model> = emptyList(),

    val expectedShardCount: Int = botStatus.getOrNull(0)?.shardCount ?: 0,
    val currentShardCount: Int = botStatus.size,
    val totalGuildsCount: Int = botStatus.sumOf(BotInstanceDataV3Model::guilds),
    val totalMemoryInGb: Double =
        apiStatus.map(InstanceDataV3Model::memory)
            .plus(camStatus.map(InstanceDataV3Model::memory))
            .plus(websiteStatus?.memory ?: 0.0)
            .plus(botStatus.map { it.instanceData.memory })
            .sum(),
)
