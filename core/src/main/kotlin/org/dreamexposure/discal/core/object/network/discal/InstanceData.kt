package org.dreamexposure.discal.core.`object`.network.discal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstanceData(
    @SerialName("api_version")
    val apiVersion: String,

    @SerialName("d4j_version")
    val d4jVersion: String,

    val uptime: String,

    val instanceId: String,
)
