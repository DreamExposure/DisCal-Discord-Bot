package org.dreamexposure.discal.server.utils

import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.web.AuthenticationState
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.concurrent.timerTask

object Authentication {
    //TODO: Don't use timer, use flux interval instead eventually
    private var timer: Timer? = null

    //TODO: Switch to atomic containing immutable map eventually
    private val tempKeys: ConcurrentMap<String, Long> = ConcurrentHashMap()
    private val readOnlyKeys: ConcurrentMap<String, Long> = ConcurrentHashMap()

    fun authenticate(swe: ServerWebExchange): Mono<AuthenticationState> {
        //Check if correct method
        if (!swe.request.methodValue.equals("POST", true) || swe.request.methodValue.equals("GET", true)) {
            LogFeed.log(LogObject.forDebug("Denied access", "Method ${swe.request.methodValue}"))
            return Mono.just(AuthenticationState(false)
                    .status(GlobalVal.STATUS_NOT_ALLOWED)
                    .reason("Method not allowed")
            )
        }

        //Requires auth header
        if (swe.request.headers.getOrEmpty("Authorization").isNotEmpty()) {
            val authKey = swe.request.headers["Authorization"]!![0]

            return when {
                authKey == BotSettings.BOT_API_TOKEN.get() -> { //This is from within discal network
                    Mono.just(AuthenticationState(true)
                            .status(GlobalVal.STATUS_SUCCESS)
                            .reason("Success")
                            .keyUsed(authKey)
                            .fromDisCalNetwork(true)
                    )
                }
                tempKeys.containsKey(authKey) -> { //Temp key granted for logged in user
                    Mono.just(AuthenticationState(true)
                            .status(GlobalVal.STATUS_SUCCESS)
                            .reason("Success")
                            .keyUsed(authKey)
                            .fromDisCalNetwork(false)
                            .readOnly(false)
                    )
                }
                readOnlyKeys.containsKey(authKey) -> { //Read-only key granted for embed pages
                    Mono.just(AuthenticationState(true)
                            .status(GlobalVal.STATUS_SUCCESS)
                            .reason("Success")
                            .keyUsed(authKey)
                            .fromDisCalNetwork(false)
                            .readOnly(true)
                    )
                }
                authKey == "teapot" -> { //I'm a teapot
                    Mono.just(AuthenticationState(false)
                            .status(GlobalVal.STATUS_TEAPOT)
                            .reason("I'm a teapot")
                            .keyUsed(authKey)
                    )
                }
                else -> { //Check if key is in database...
                    DatabaseManager.getAPIAccount(authKey).map { acc ->
                        if (!acc.blocked) {
                            return@map AuthenticationState(true)
                                    .status(GlobalVal.STATUS_SUCCESS)
                                    .reason("Success")
                                    .keyUsed(authKey)
                                    .fromDisCalNetwork(false)
                        } else {
                            return@map AuthenticationState(false)
                                    .status(GlobalVal.STATUS_AUTHORIZATION_DENIED)
                                    .reason("Authorization Denied. API key blocked")
                        }
                    }.defaultIfEmpty(AuthenticationState(false)
                            .status(GlobalVal.STATUS_AUTHORIZATION_DENIED)
                            .reason("Authorization Denied")
                    )
                }
            }
        } else {
            LogFeed.log(
                    LogObject.forDebug("Attempted to use API without auth",
                            "IP: ${swe.request.localAddress} | ${swe.request.remoteAddress}")
            )

            return Mono.just(AuthenticationState(false)
                    .status(GlobalVal.STATUS_BAD_REQUEST)
                    .reason("Bad Request")
            )
        }
    }

    fun saveTempKey(key: String) {
        if (!tempKeys.containsKey(key))
            tempKeys[key] = System.currentTimeMillis() + Duration.ofDays(1).toMillis()
    }

    fun removeTempKey(key: String) {
        tempKeys.remove(key)
    }

    fun saveReadOnlyKey(key: String) {
        if (!readOnlyKeys.containsKey(key))
            readOnlyKeys[key] = System.currentTimeMillis() + Duration.ofHours(1).toMillis()
    }

    fun init() {
        timer = Timer(true)

        timer?.schedule(timerTask {
            handleExpiredKeys()
        }, Duration.ofHours(1).toMillis())
    }

    fun shutdown() {
        if (timer != null) timer?.cancel()
    }

    private fun handleExpiredKeys() {
        val allToRemove = mutableListOf<String>()

        tempKeys.forEach { if (it.value >= System.currentTimeMillis()) allToRemove += it.key }
        readOnlyKeys.forEach { if (it.value >= System.currentTimeMillis()) allToRemove += it.key }
        allToRemove.forEach { tempKeys.remove(it);readOnlyKeys.remove(it) }
    }
}
