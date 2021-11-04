package org.dreamexposure.discal.core.`object`.network.discal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

@Serializable
data class NetworkData(
        @SerialName("total_calendars")
        var totalCalendars: Int = 0,

        @SerialName("total_announcements")
        var totalAnnouncements: Int = 0,

        @SerialName("api_status")
        var apiStatus: InstanceData = InstanceData(),

        @SerialName("website_status")
        var websiteStatus: InstanceData? = null,

        @SerialName("cam_status")
        var camStatus: InstanceData? = null,

        @SerialName("bot_status")
        val botStatus: MutableList<BotInstanceData> = CopyOnWriteArrayList()
) {
    @SerialName("expected_shard_count")
    val expectedShardCount: Int
    get() {
        return if (botStatus.isNotEmpty()) botStatus[0].shardCount
        else -1
    }

    @SerialName("total_guilds")
    val totalGuilds: Int
    get() {
        var guilds = 0
        botStatus.forEach { guilds += it.guilds }
        return guilds
    }

    @Suppress("unused") //Used in thymeleaf status page
    fun getCurrentShardCount() = botStatus.size

    @Suppress("unused") //Used in thymeleaf status page
    fun getTotalMemoryInGb(): Double {
        var totalMemMb = 0.0

        totalMemMb += apiStatus.memory
        totalMemMb += websiteStatus?.memory ?: 0.0
        totalMemMb += camStatus?.memory ?: 0.0

        botStatus.forEach { totalMemMb+= it.instanceData.memory }

        return ((totalMemMb / 1024) * 100).roundToInt().toDouble() / 100
    }
}
