package org.dreamexposure.discal.core.extensions.spring

import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.publisher.Mono

fun ServerHttpResponse.writeJsonString(json: String): Mono<Void> {
    val factory = bufferFactory()
    val buffer = factory.wrap(json.toByteArray())

    headers.contentType = MediaType.APPLICATION_JSON
    return writeWith(Mono.just(buffer))
}
