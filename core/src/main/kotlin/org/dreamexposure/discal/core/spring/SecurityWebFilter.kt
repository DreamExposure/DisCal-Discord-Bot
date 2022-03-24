package org.dreamexposure.discal.core.spring

import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.exceptions.AuthenticationException
import org.dreamexposure.discal.core.exceptions.EmptyNotAllowedException
import org.dreamexposure.discal.core.exceptions.TeaPotException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
@ConditionalOnProperty(name = ["discal.security.enabled"], havingValue = "true")
class SecurityWebFilter(val handlerMapping: RequestMappingHandlerMapping) : WebFilter {
    private val readOnlyKeys: ConcurrentMap<String, Instant> = ConcurrentHashMap()

    init {
        Flux.interval(Duration.ofMinutes(30))
            .map { handleExpiredKeys() }
            .subscribe()
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return handlerMapping.getHandler(exchange).cast(HandlerMethod::class.java)
            .onErrorResume { Mono.error(EmptyNotAllowedException()) }
            .switchIfEmpty(Mono.error(EmptyNotAllowedException())) // Don't apply custom filter if this is not on a method
            .filter { it.hasMethodAnnotation(Authentication::class.java) }
            .switchIfEmpty(Mono.error(IllegalAccessException("No authentication annotation!")))
            .map { it.getMethodAnnotation(Authentication::class.java)!! }
            .map(Authentication::access)
            .filter { it != Authentication.AccessLevel.PUBLIC } //if public, we don't need to check headers
            .flatMap { requiredAccess ->
                authenticate(exchange)
                    .filter { it < requiredAccess }
                    .flatMap {
                        Mono.error<Void>(AuthenticationException("Insufficient access level!"))
                    }
            }.onErrorResume(EmptyNotAllowedException::class.java) { Mono.empty() }
            .then(chain.filter(exchange))
    }

    fun saveReadOnlyKey(key: String, expireAt: Instant) {
        readOnlyKeys[key] = expireAt
    }

    private fun handleExpiredKeys() {
        val allToRemove = mutableListOf<String>()

        readOnlyKeys.forEach { if (Instant.now().isAfter(it.value)) allToRemove += it.key }

        allToRemove.forEach { readOnlyKeys.remove(it) }
    }

    /**
     * Returns the highest permission this user has, or throws an error
     */
    private fun authenticate(exchange: ServerWebExchange): Mono<Authentication.AccessLevel> {
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
                authHeader.equals(BotSettings.BOT_API_TOKEN.get()) -> { // This is from within discal network
                    Mono.just(Authentication.AccessLevel.ADMIN)
                }
                readOnlyKeys.containsKey(authHeader) -> { // Read-only key granted for embed pages
                    Mono.just(Authentication.AccessLevel.READ)
                }
                authHeader.startsWith("Bearer ") -> {
                    DatabaseManager.getSessionData(authHeader.substringAfter("Bearer ")).flatMap { session ->
                        if (session.expiresAt.isAfter(Instant.now())) {
                            Mono.just(Authentication.AccessLevel.WRITE)
                        } else {
                            Mono.error(AuthenticationException("Session expired"))
                        }
                    }.switchIfEmpty(Mono.error(AuthenticationException("API key not found")))
                }
                else -> {
                    // Check if this is an API key
                    DatabaseManager.getAPIAccount(authHeader).flatMap { acc ->
                        if (!acc.blocked) {
                            Mono.just(Authentication.AccessLevel.WRITE)
                        } else {
                            Mono.error(AuthenticationException("API key blocked"))
                        }
                    }.switchIfEmpty(Mono.error(AuthenticationException("API key not found")))
                }
            }
        }.switchIfEmpty(Mono.error(IllegalStateException("uh oh")))
    }
}
