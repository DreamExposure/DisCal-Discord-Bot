package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface CredentialsRepository : R2dbcRepository<CredentialData, Int> {

    fun findByCredentialNumber(credentialNumber: Int): Mono<CredentialData>

    // TODO: Finish impl???
}
