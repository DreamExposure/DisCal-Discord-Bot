package org.dreamexposure.discal.cam.security

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.cam.business.SecurityService
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.extensions.spring.writeJsonString
import org.dreamexposure.discal.core.`object`.rest.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class SecurityWebFilter(
    private val securityService: SecurityService,
    private val handlerMapping: RequestMappingHandlerMapping,
    private val objectMapper: ObjectMapper,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return mono {
            doSecurityFilter(exchange, chain)
        }.then(chain.filter(exchange))
    }

    suspend fun doSecurityFilter(exchange: ServerWebExchange, chain: WebFilterChain) {
        val handlerMethod = handlerMapping.getHandler(exchange)
            .cast(HandlerMethod::class.java)
            .onErrorResume { Mono.empty() }
            .awaitFirstOrNull() ?: return

        if (!handlerMethod.hasMethodAnnotation(SecurityRequirement::class.java)) {
            throw IllegalStateException("No SecurityRequirement annotation!")
        }

        val authAnnotation = handlerMethod.getMethodAnnotation(SecurityRequirement::class.java)!!
        val authHeader = exchange.request.headers.getOrEmpty("Authorization").firstOrNull()


        if (authAnnotation.disableSecurity) return

        if (authHeader == null) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            exchange.response.writeJsonString(
                objectMapper.writeValueAsString(ErrorResponse("Missing Authorization header"))
            ).awaitFirstOrNull()
            return
        }

        if (authHeader.equals("teapot", ignoreCase = true)) {
            exchange.response.statusCode = HttpStatus.I_AM_A_TEAPOT
            exchange.response.writeJsonString(
                objectMapper.writeValueAsString(ErrorResponse("I'm a teapot"))
            ).awaitFirstOrNull()
            return
        }

        if (!securityService.authenticateToken(authHeader)) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            exchange.response.writeJsonString(
                objectMapper.writeValueAsString(ErrorResponse("Unauthenticated"))
            ).awaitFirstOrNull()
            return
        }

        if (!securityService.validateTokenSchema(authHeader, authAnnotation.schemas.toList())) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            exchange.response.writeJsonString(
                objectMapper.writeValueAsString(ErrorResponse("Unsupported schema"))
            ).awaitFirstOrNull()
            return
        }

        if (!securityService.authorizeToken(authHeader, authAnnotation.scopes.toList())) {
            exchange.response.statusCode = HttpStatus.FORBIDDEN
            exchange.response.writeJsonString(
                objectMapper.writeValueAsString(ErrorResponse("Access denied"))
            ).awaitFirstOrNull()
            return
        }

        // If we made it to the end, everything is good to go.
    }
}
