package org.dreamexposure.discal.core.`object`.network.discal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkData(
    @SerialName("api_version")
    val apiVersion: String,

    val clients: List<ConnectedClient>,
)
