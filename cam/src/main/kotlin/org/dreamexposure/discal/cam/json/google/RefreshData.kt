package org.dreamexposure.discal.cam.json.google

import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshData(
        @JsonProperty("access_token")
        val accessToken: String,

        @JsonProperty("expires_in")
        val expiresIn: Int
)
