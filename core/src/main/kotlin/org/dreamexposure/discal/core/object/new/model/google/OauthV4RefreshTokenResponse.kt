package org.dreamexposure.discal.core.`object`.new.model.google

import com.fasterxml.jackson.annotation.JsonProperty

data class OauthV4RefreshTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Int
)
