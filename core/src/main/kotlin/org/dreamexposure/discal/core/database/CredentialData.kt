package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table

@Table("credentials")
data class CredentialData(
    val credentialNumber: Int,
    val refreshToken: String,
    val accessToken: String,
    val expiresAt: Long,
)
