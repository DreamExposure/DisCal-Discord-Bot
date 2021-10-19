package org.dreamexposure.discal.cam.json.google

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshData(
        @SerialName("access_token")
        val accessToken: String,

        @SerialName("expires_in")
        val expiresIn: Int
)
