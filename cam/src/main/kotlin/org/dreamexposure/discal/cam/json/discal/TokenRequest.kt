package org.dreamexposure.discal.cam.json.discal

import kotlinx.serialization.Serializable

@Serializable
data class TokenRequest(
    val state: String,

    val code: String,
)
