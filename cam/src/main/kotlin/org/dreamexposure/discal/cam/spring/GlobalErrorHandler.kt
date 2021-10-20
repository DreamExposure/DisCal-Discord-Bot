package org.dreamexposure.discal.cam.spring

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.`object`.rest.RestError
import org.dreamexposure.discal.core.exceptions.AccessRevokedException
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
@Order(-2)
class GlobalErrorHandler : ErrorWebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, throwable: Throwable): Mono<Void> {
        //Handle exceptions we have codes for
        val restError: RestError = when (throwable) {
            is AccessRevokedException -> {
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                RestError(RestError.Code.ACCESS_REVOKED)
            }
            is SerializationException -> {
                exchange.response.statusCode = HttpStatus.BAD_REQUEST
                RestError(RestError.Code.BAD_REQUEST)
            }
            else -> { // Something we have no special case for
                exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
                RestError(RestError.Code.INTERNAL_SERVER_ERROR)
            }
        }

        // Convert restError to json byte array
        val factory = exchange.response.bufferFactory()
        val buffer = factory.wrap(JSON_FORMAT.encodeToString(restError).encodeToByteArray())

        // Return response with body
        exchange.response.headers.contentType = MediaType.APPLICATION_JSON
        return exchange.response.writeWith(Mono.just(buffer))
    }
}
