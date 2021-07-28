package org.dreamexposure.discal.core.`object`.rest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.UUIDasStringSerializer
import java.util.*

@Serializable
data class HeartbeatRequest(
        @SerialName("instance_id")
        @Serializable(with = UUIDasStringSerializer::class)
        val instanceId: UUID,

        @SerialName("client_index")
        val clientIndex: String,

        @SerialName("expected_clients")
        val expectedClients: Int,

        @SerialName("guilds")
        val guildCount: Int,

        val memory: Double,

        val uptime: String,

        val version: String,

        @SerialName("d4j_version")
        val d4jVersion: String,
)
