package org.dreamexposure.discal.core.`object`.new.model.discal.cam

import java.time.Instant

data class TokenV1Model(
    val accessToken: String,
    val validUntil: Instant,
)
