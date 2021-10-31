package org.dreamexposure.discal.core.spring

import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.exceptions.AuthenticationException
import org.dreamexposure.discal.core.exceptions.TeaPotException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.HandlerMapping
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
class SecurityWebFilter : WebFilter {
    private val tempKeys: ConcurrentMap<String, Instant> = ConcurrentHashMap()
    private val readOnlyKeys: ConcurrentMap<String, Instant> = ConcurrentHashMap()

    init {
        Flux.interval(Duration.ofMinutes(30))
                .map { handleExpiredKeys() }
                .subscribe()
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        //FIXME: Seems to be null
        val handler = exchange.attributes[HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE] as HandlerMethod

        if (handler.hasMethodAnnotation(Authentication::class.java)) {
            val annotation = handler.getMethodAnnotation(Authentication::class.java)!!

            return authenticate(exchange).flatMap { grantedLevel ->
                if (grantedLevel < annotation.access)
                    Mono.error(AuthenticationException("Insufficient permissions to access this resource."))
                else chain.filter(exchange)
            }
        }

        return chain.filter(exchange)
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
