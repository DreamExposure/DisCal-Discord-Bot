package org.dreamexposure.discal.core.spring

import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.annotations.Authentication.AccessLevel
import org.dreamexposure.discal.core.annotations.Authentication.TokenType
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.exceptions.AuthenticationException
import org.dreamexposure.discal.core.exceptions.EmptyNotAllowedException
import org.dreamexposure.discal.core.exceptions.TeaPotException
import org.dreamexposure.discal.core.`object`.BotSettings
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Instant

@Component
@ConditionalOnProperty(name = ["discal.security.enabled"], havingValue = "true")
class SecurityWebFilter(val handlerMapping: RequestMappingHandlerMapping) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return handlerMapping.getHandler(exchange).cast(HandlerMethod::class.java)
            .onErrorResume { Mono.error(EmptyNotAllowedException()) }
            // Don't apply custom filter if this is not on a method
            .switchIfEmpty(Mono.error(EmptyNotAllowedException()))
            .filter { it.hasMethodAnnotation(Authentication::class.java) }
            .switchIfEmpty(Mono.error(IllegalAccessException("No authentication annotation!")))
            .map { it.getMethodAnnotation(Authentication::class.java)!! }
            // Do token type validation
            .filter { validateTokenType(exchange, it.tokenType) }
            .switchIfEmpty(Mono.error(AuthenticationException("Invalid token type")))
            //if public & no auth provided, no need to authenticate
            .filter { it.access != AccessLevel.PUBLIC && !exchange.request.headers.containsKey("Authorization") }
            // do access level validation
            .flatMap { requirements ->
                authenticate(exchange)
                    .filter { it < requirements.access }
                    .flatMap { Mono.error<Void>(AuthenticationException("Insufficient access level!")) }
            }.onErrorResume(EmptyNotAllowedException::class.java) { Mono.empty() }
            .then(chain.filter(exchange))
    }

    /**
     * Returns true if token type is valid for the endpoint's requirements
     */
    private fun validateTokenType(exchange: ServerWebExchange, requiredTokenType: TokenType): Boolean {
        // If no auth header is present, token type validation is not needed
        val authHeader = exchange.request.headers["Authorization"]?.get(0) ?: return true

        return when (requiredTokenType) {
            TokenType.BEARER -> authHeader.startsWith("Bearer ")
            TokenType.APPLICATION -> authHeader.startsWith("App ")
            TokenType.ANY -> true
        }
    }

    /**
     * Returns the highest permission this user has, or throws an error
     */
    private fun authenticate(exchange: ServerWebExchange): Mono<AccessLevel> {
        return Mono.defer {
            // Make sure auth header is present
            if (!exchange.request.headers.containsKey("Authorization")) {
                return@defer Mono.error(AuthenticationException("No authorization header present"))
            }

            val authHeader = exchange.request.headers["Authorization"]!![0]
            return@defer when {
                // I'm a teapot
                authHeader.equals("teapot", true) -> {
                    Mono.error(TeaPotException())
                }
                // This is from within discal network
                authHeader.equals(BotSettings.BOT_API_TOKEN.get()) -> {
                    Mono.just(AccessLevel.ADMIN)
                }
                // Authenticate bearer
                authHeader.startsWith("Bearer ") -> {
                    DatabaseManager.getSessionData(authHeader.substringAfter("Bearer ")).flatMap { session ->
                        if (session.expiresAt.isAfter(Instant.now())) {
                            Mono.just(AccessLevel.WRITE)
                        } else {
                            Mono.error(AuthenticationException("Session expired"))
                          }
                    }.switchIfEmpty(Mono.error(AuthenticationException("API key not found")))
                }
                // Authenticate app
                authHeader.startsWith("App ") -> {
                    // API key
                    DatabaseManager.getAPIAccount(authHeader.substringAfter("App ")).flatMap { acc ->
                        if (!acc.blocked) {
                            Mono.just(AccessLevel.WRITE)
                        } else {
                            Mono.error(AuthenticationException("API key blocked"))
                        }
                    }.switchIfEmpty(Mono.error(AuthenticationException("API key not found")))
                }
                else -> Mono.error(AuthenticationException("No valid token type provided"))
            }
        }.switchIfEmpty(Mono.error(IllegalStateException("uh oh")))
    }
}
