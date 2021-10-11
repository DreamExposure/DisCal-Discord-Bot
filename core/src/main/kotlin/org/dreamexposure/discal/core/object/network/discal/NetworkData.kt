package org.dreamexposure.discal.core.`object`.network.discal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.CopyOnWriteArrayList

@Serializable
data class NetworkData(
        @SerialName("total_calendars")
        var totalCalendars: Int = 0,

        @SerialName("total_announcements")
        var totalAnnouncements: Int = 0,

        @SerialName("api_status")
        var apiStatus: InstanceData? = null,

        @SerialName("website_status")
        var websiteStatus: InstanceData? = null,

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
}
