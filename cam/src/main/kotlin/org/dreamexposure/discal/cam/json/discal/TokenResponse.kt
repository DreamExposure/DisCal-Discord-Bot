package org.dreamexposure.discal.cam.json.discal

import org.dreamexposure.discal.cam.json.discord.SimpleUserData
import java.time.Instant

data class TokenResponse(
    val token: String,
    val expires: Instant,
    val user: SimpleUserData,
)
