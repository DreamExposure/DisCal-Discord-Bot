package org.dreamexposure.discal.core.`object`.rest

import kotlinx.serialization.Serializable
import reactor.core.publisher.Mono

@Serializable
data class GenericResponse(
    val message: String,

    ) {

    fun asMono(): Mono<GenericResponse> = Mono.just(this)
}
