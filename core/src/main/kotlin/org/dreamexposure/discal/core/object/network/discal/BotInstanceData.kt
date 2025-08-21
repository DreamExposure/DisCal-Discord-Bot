package org.dreamexposure.discal.core.`object`.network.discal

import com.fasterxml.jackson.annotation.JsonProperty
import discord4j.core.GatewayDiscordClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.Application
import reactor.core.publisher.Mono

@Deprecated("Prefer to use new v3 impl")
@Suppress("DataClassPrivateConstructor")
@Serializable
data class BotInstanceData constructor(
        @SerialName("instance")
        @JsonProperty("instance")
        val instanceData: InstanceData,

        @SerialName("shard_index")
        @JsonProperty("shard_index")
        val shardIndex: Int,

        @SerialName("shard_count")
        @JsonProperty("shard_count")
        val shardCount: Int,

        val guilds: Int = 0,
) {
    companion object {
        fun load(client: GatewayDiscordClient?): Mono<BotInstanceData> {
            return Mono.justOrEmpty(client)
                    .flatMap { it.guilds.count() }
                    .map(Long::toInt)
                    .defaultIfEmpty(0)
                    .map { guildCount ->
                        BotInstanceData(
                                instanceData = InstanceData(),
                                shardIndex = Application.getShardIndex(),
                                shardCount = Application.getShardCount(),
                                guilds = guildCount
                        )
                    }
        }
    }
}
