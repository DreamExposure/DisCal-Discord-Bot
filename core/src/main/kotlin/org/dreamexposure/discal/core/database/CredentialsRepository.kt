package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface CredentialsRepository : R2dbcRepository<CredentialData, Int> {

    fun findByCredentialNumber(credentialNumber: Int): Mono<CredentialData>

    @Query("""
        UPDATE credentials
        SET refresh_token = :refreshToken,
            access_token = :accessToken,
            expires_at = :expiresAt
        WHERE credential_number = :credentialNumber
    """)
    fun updateByCredentialNumber(
        credentialNumber: Int,
        refreshToken: String,
        accessToken: String,
        expiresAt: Long,
    ): Mono<Int>
}
