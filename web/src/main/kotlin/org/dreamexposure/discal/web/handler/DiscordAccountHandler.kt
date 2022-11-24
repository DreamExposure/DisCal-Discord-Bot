package org.dreamexposure.discal.web.handler

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.utils.GlobalVal
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URLEncoder
import java.nio.charset.Charset
import java.time.Duration
import java.time.LocalDate
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class DiscordAccountHandler(
    @Value("\${bot.discord-app-id}")
    private val clientId: String,
    @Value("\${bot.url.discord.redirect}")
    private val redirectUrl: String,
    @Value("\${bot.url.api}")
    private val apiUrl: String,
): ApplicationRunner {
    private val discordAccounts: ConcurrentMap<String, MutableMap<String, Any>> = ConcurrentHashMap()

    fun hasAccount(swe: ServerWebExchange): Mono<Boolean> {
        return swe.session.map { session ->
            val acc = session.getAttribute("account") as? String

            if (acc != null) discordAccounts.containsKey(acc)
            else false
        }
    }

    //Get accounts
    fun getAccount(swe: ServerWebExchange): Mono<MutableMap<String, Any>> {
        return swe.session.map { session ->
            val acc = session.getAttribute("account") as? String

            if (acc != null && discordAccounts.containsKey(acc)) {
                val model = discordAccounts[acc]!!

                model.remove("last_use")
                model["last_use"] = System.currentTimeMillis()

                //Remove this in case it exists. A new one is generated when using the embed page anyway.
                model.remove("embed_key")

                return@map model
            } else {
                //Not logged in
                return@map createDefaultModel()
            }
        }
    }

    fun getEmbedAccount(swe: ServerWebExchange): Mono<MutableMap<String, Any>> {
        return swe.session.flatMap { session ->
            val acc = session.getAttribute("account") as? String

            if (acc != null) {
                val model = discordAccounts[acc]!!

                model.remove("last_use")
                model["last_use"] = System.currentTimeMillis()

                if (!model.containsKey("embed_key")) {
                    //Get and add read-only API key for embed page. Only good for one hour.
                    return@flatMap getReadOnlyApikey()
                            .doOnNext { model["embed_key"] = it }
                            .thenReturn(model)
                } else return@flatMap Mono.just(model)
            } else {
                //Lot logged in
                val model: MutableMap<String, Any> = createDefaultModel()

                //Get and add read-only API key for embed page. Only good for one hour.
                return@flatMap getReadOnlyApikey()
                        .doOnNext { model["embed_key"] = it }
                        .thenReturn(model)
            }
        }
    }

    //Functions
    fun addAccount(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<Void> {
        model.remove("last_use")
        model["last_use"] = System.currentTimeMillis()
        return removeAccount(swe).then(swe.session
                .flatMap { session ->
                    session.attributes["account"] = UUID.randomUUID().toString()
                    discordAccounts[session.getRequiredAttribute("account") as String] = model
                    Mono.empty()
                }
        )
    }

    fun removeAccount(swe: ServerWebExchange): Mono<Void> {
        return hasAccount(swe).flatMap { has ->
            if (has) {
                swe.session.map {
                    this.discordAccounts.remove(it.getRequiredAttribute("account"))
                    Mono.justOrEmpty(it.attributes.remove("account"))
                }.then()
            } else Mono.empty()
        }
    }

    private fun removeTimedOutAccounts() {
        val limit = BotSettings.TIME_OUT.get().toLong()
        val toRemove = mutableListOf<String>()

        for (id in discordAccounts.keys) {
            val model = discordAccounts[id]!!
            val lastUse = model["last_use"] as Long

            if (System.currentTimeMillis() - lastUse > limit)
                toRemove.add(id) //Timed out, remove account info and require sign in
        }

        for (id in toRemove) {
            discordAccounts.remove(id)
        }
    }

    internal fun createDefaultModel(): MutableMap<String, Any> {
        val model: MutableMap<String, Any> = mutableMapOf()
        model["logged_in"] = false
        model["bot_id"] = clientId
        model["year"] = LocalDate.now().year
        model["redirect_uri"] = URLEncoder.encode(redirectUrl, Charset.defaultCharset())
        model["bot_invite"] = BotSettings.INVITE_URL.get()
        model["support_invite"] = BotSettings.SUPPORT_INVITE.get()
        model["api_url"] = apiUrl

        return model
    }

    private fun getReadOnlyApikey(): Mono<String> {
        return Mono.fromCallable {
            val client = OkHttpClient()
            val keyGrantRequestBody = "".toRequestBody(GlobalVal.JSON)
            val keyGrantRequest = Request.Builder()
                    .url("$apiUrl/v2/account/key/readonly/get")
                    .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                    .post(keyGrantRequestBody)
                    .build()

            client.newCall(keyGrantRequest).execute()
        }.subscribeOn(Schedulers.boundedElastic())
                .map { response ->
                    if (response.isSuccessful) {
                        //TODO: Change to use kotlin serialization
                        val body = JSONObject(response.body?.string())
                        response.body?.close()
                        response.close()

                        return@map body.optString("key", "internal_error")
                    } else {
                        //Something didn't work, log and add "key" embed page knows how to handle
                        LOGGER.debug("Embed key fail ${response.body?.string()}")
                        response.body?.close()
                        response.close()
                        return@map "internal_error"
                    }
                }
                .doOnError {
                    //Something didn't work, log and add "key" embed page knows how to handle
                    LOGGER.error("Embed key get failure", it)
                }
                .onErrorReturn("internal_error")
    }

    override fun run(args: ApplicationArguments?) {
        Flux.interval(Duration.ofMinutes(30))
            .map { removeTimedOutAccounts() }
            .subscribe()
    }
}
