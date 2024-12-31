package org.dreamexposure.discal.core.spring

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.api.CamApiWrapper
import org.dreamexposure.discal.core.extensions.spring.writeJsonString
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.SecurityValidateV1Request
import org.dreamexposure.discal.core.`object`.rest.ErrorResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@ConditionalOnProperty(name = ["discal.security.enabled"], havingValue = "true")
class SecurityWebFilter(
    private val camApiWrapper: CamApiWrapper,
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

        // Use CAM to validate token
        val requestBody = SecurityValidateV1Request(authHeader, authAnnotation.schemas.toList(), authAnnotation.scopes.toList())

        val response = camApiWrapper.validateToken(requestBody)

        if (response.code != 200) {
            throw IllegalStateException("Failed to validate token | ${response.code} | ${response.error?.error}")
        }

        if (!response.entity!!.valid) {
            exchange.response.statusCode = response.entity.code
            exchange.response.writeJsonString(
                objectMapper.writeValueAsString(ErrorResponse(response.entity.message))
            ).awaitFirstOrNull()
            return
        }

        // If we made it here, everything is good to go.
    }
}
