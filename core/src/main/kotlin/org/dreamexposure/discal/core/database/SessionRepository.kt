package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

interface SessionRepository : R2dbcRepository<SessionData, String> {
    fun findByToken(token: String): Mono<SessionData>

    fun findAllByUserId(userId: Long): Flux<SessionData>

    fun deleteByToken(token: String): Mono<Void>

    fun deleteAllByUserId(userId: Long): Mono<Long>

    fun deleteAllByExpiresAtIsLessThan(expiresAt: Instant): Mono<Long>
}
