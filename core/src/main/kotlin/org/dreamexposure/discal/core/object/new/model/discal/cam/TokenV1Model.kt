package org.dreamexposure.discal.core.`object`.new.model.discal.cam

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TokenV1Model(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("valid_until")
    val validUntil: Instant,
)
