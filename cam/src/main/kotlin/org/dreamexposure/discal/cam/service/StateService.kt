package org.dreamexposure.discal.cam.service

import org.dreamexposure.discal.core.crypto.KeyGenerator
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

@Component
class StateService {

    private val states: MutableMap<String, Instant> = ConcurrentHashMap()

    init {
        // occasionally remove expired/unused states
        Flux.interval(Duration.ofHours(1))
            .doOnNext {
                val toRemove = mutableListOf<String>()

                states.forEach { (state, expires) ->
                    if (expires.isBefore(Instant.now()))
                        toRemove.add(state)
                }

                toRemove.forEach(states::remove)
            }.subscribe()
    }

    fun generateState(): String {
        val state = KeyGenerator.csRandomAlphaNumericString(64)
        states[state] = Instant.now().plus(5, ChronoUnit.MINUTES)

        return state
    }

    fun validateState(state: String): Boolean {
        val expiresAt = states[state]
        states.remove(state) // Remove state immediately to prevent replay attacks

        return expiresAt != null && expiresAt.isAfter(Instant.now())
    }
}
