package org.dreamexposure.discal.core.spring

import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.exceptions.AuthenticationException
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
    private val tempKeys: ConcurrentMap<String, Instant> = ConcurrentHashMap()
    private val readOnlyKeys: ConcurrentMap<String, Instant> = ConcurrentHashMap()

    init {
        Flux.interval(Duration.ofMinutes(30))
                .map { handleExpiredKeys() }
                .subscribe()
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return handlerMapping.getHandler(exchange).cast(HandlerMethod::class.java)
                .filter { it.hasMethodAnnotation(Authentication::class.java) }
                .switchIfEmpty(Mono.error(IllegalAccessException("No authentication annotation!")))
                .map { it.getMethodAnnotation(Authentication::class.java)!! }
                .map(Authentication::access)
                .filter { it != Authentication.AccessLevel.PUBLIC } //if public, we don't need to check headers
                .flatMap { requiredAccess ->
                    authenticate(exchange)
                            .filter { it < requiredAccess }
                            .then(Mono.error<Void>(AuthenticationException("Insufficient access level")))
                }.then(chain.filter(exchange))
    }

    fun saveTempKey(key: String, expireAt: Instant) {
        tempKeys[key] = expireAt
    }

    fun removeTempKey(key: String) {
        tempKeys.remove(key)
    }

    fun saveReadOnlyKey(key: String, expireAt: Instant) {
        readOnlyKeys[key] = expireAt
    }

    private fun handleExpiredKeys() {
        val allToRemove = mutableListOf<String>()

        tempKeys.forEach { if (Instant.now().isAfter(it.value)) allToRemove += it.key }
        readOnlyKeys.forEach { if (Instant.now().isAfter(it.value)) allToRemove += it.key }

        allToRemove.forEach { tempKeys.remove(it);readOnlyKeys.remove(it) }
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
            return@defer when { // I'm a teapot
                authHeader.equals("teapot", true) -> {
                    Mono.error(TeaPotException())
                }
                authHeader.equals(BotSettings.BOT_API_TOKEN.get()) -> { // This is from within discal network
                    Mono.just(Authentication.AccessLevel.ADMIN)
                }
                tempKeys.containsKey(authHeader) -> { // Temp write key for logged in user
                    Mono.just(Authentication.AccessLevel.WRITE)
                }
                readOnlyKeys.containsKey(authHeader) -> { // Read-only key granted for embed pages
                    Mono.just(Authentication.AccessLevel.READ)
                }
                else -> { // Check if key is in database...
                    DatabaseManager.getAPIAccount(authHeader).flatMap { acc ->
                        if (!acc.blocked) {
                            Mono.just(Authentication.AccessLevel.WRITE)
                        } else {
                            Mono.error(AuthenticationException("API key blocked"))
                        }
                    }.switchIfEmpty(Mono.error(AuthenticationException("API key not found")))
                }
            }
        }
    }
}
