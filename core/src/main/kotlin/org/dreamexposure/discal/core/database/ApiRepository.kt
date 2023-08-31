package org.dreamexposure.discal.core.database

import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface ApiRepository: R2dbcRepository<ApiData, String> {
    fun getByApiKey(apiKey: String): Mono<ApiData>
}
