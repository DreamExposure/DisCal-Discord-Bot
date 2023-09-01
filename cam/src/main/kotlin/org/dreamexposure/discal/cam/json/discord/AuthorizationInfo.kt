package org.dreamexposure.discal.cam.json.discord

import java.time.Instant

data class AuthorizationInfo(
    val scopes: List<String>,
    val expires: Instant,
    val user: SimpleUserData?,
)
