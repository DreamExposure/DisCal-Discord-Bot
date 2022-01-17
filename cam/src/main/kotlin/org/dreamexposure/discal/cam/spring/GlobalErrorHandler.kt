package org.dreamexposure.discal.cam.spring

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import org.dreamexposure.discal.core.`object`.rest.RestError
import org.dreamexposure.discal.core.exceptions.AccessRevokedException
import org.dreamexposure.discal.core.exceptions.AuthenticationException
import org.dreamexposure.discal.core.exceptions.NotFoundException
import org.dreamexposure.discal.core.exceptions.TeaPotException
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import org.springframework.beans.TypeMismatchException
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
@Order(-2)
class GlobalErrorHandler : ErrorWebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, throwable: Throwable): Mono<Void> {
        //Handle exceptions we have codes for
        val restError: RestError = when (throwable) {
            is ResponseStatusException -> {
                when (throwable.status) {
                    HttpStatus.NOT_FOUND -> {
                        exchange.response.statusCode = HttpStatus.NOT_FOUND
                        LOGGER.trace("404 exchange | Path: ${exchange.request.path}")
                        RestError.NOT_FOUND
                    }
                    HttpStatus.BAD_REQUEST -> {
                        exchange.response.statusCode = HttpStatus.BAD_REQUEST
                        RestError.BAD_REQUEST
                    }
                    else -> {
                        LOGGER.error(DEFAULT, "[GlobalErrorHandler] Unhandled ResponseStatusException", throwable)
                        exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
                        RestError.INTERNAL_SERVER_ERROR
                    }
                }
            }
            is TeaPotException -> {
                exchange.response.statusCode = HttpStatus.I_AM_A_TEAPOT
                RestError.TEAPOT
            }
            is AuthenticationException -> {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                RestError.UNAUTHORIZED
            }
            is AccessRevokedException -> {
                exchange.response.statusCode = HttpStatus.FORBIDDEN
                RestError.ACCESS_REVOKED
            }
            is TypeMismatchException,
            is SerializationException -> {
                exchange.response.statusCode = HttpStatus.BAD_REQUEST
                RestError.BAD_REQUEST
            }
            is HttpClientErrorException.NotFound,
            is NotFoundException -> {
                exchange.response.statusCode = HttpStatus.NOT_FOUND
                RestError.NOT_FOUND
            }
            else -> { // Something we have no special case for
                LOGGER.error(DEFAULT, "[GlobalErrorHandler] Unhandled exception", throwable)
                exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
                RestError.INTERNAL_SERVER_ERROR
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
