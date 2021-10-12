package org.dreamexposure.discal.web.network.discord

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.web.WebPartialGuild
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.enums.time.GoodTimezone
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.web.handler.DiscordAccountHandler
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.function.TupleUtils
import java.util.*

@Controller
class DiscordLoginHandler(private val accountHandler: DiscordAccountHandler) {
    @GetMapping("/account/login")
    fun handleDiscordCode(swe: ServerWebExchange, @RequestParam("code") code: String): Mono<String> {
        val client = OkHttpClient()

        return Mono.fromCallable {
            val body = FormBody.Builder()
                    .addEncoded("client_id", BotSettings.ID.get())
                    .addEncoded("client_secret", BotSettings.SECRET.get())
                    .addEncoded("grant_type", "authorization_code")
                    .addEncoded("code", code)
                    .addEncoded("redirect_uri", BotSettings.REDIR_URI.get())
                    .build()

            val tokenRequest = Request.Builder()
                    .url("${GlobalVal.discordApiUrl}/oauth2/token")
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build()

            return@fromCallable client.newCall(tokenRequest).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap {
            Mono.fromCallable {
                val json = JSONObject(it.body?.string())
                it.body?.close()
                it.close()

                json
            }
        }.flatMap { info ->
            if (info.has("access_token")) {
                //GET request for user info
                val userDataRequest = Request.Builder()
                        .get()
                        .url("${GlobalVal.discordApiUrl}/users/@me")
                        .header("Authorization", "Bearer ${info.getString("access_token")}")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .build()

                val userGuildsRequest = Request.Builder()
                        .get()
                        .url("${GlobalVal.discordApiUrl}/users/@me/guilds")
                        .header("Authorization", "Bearer ${info.getString("access_token")}")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .build()

                val dataResMono = Mono.fromCallable(client.newCall(userDataRequest)::execute)
                        .subscribeOn(Schedulers.boundedElastic())
                val guildResMono = Mono.fromCallable(client.newCall(userGuildsRequest)::execute)
                        .subscribeOn(Schedulers.boundedElastic())

                Mono.zip(dataResMono, guildResMono)
                        .flatMap(TupleUtils.function { userDataResponse, userGuildsResponse ->
                            val userInfo = JSONObject(userDataResponse.body?.string())
                            userDataResponse.body?.close()
                            userDataResponse.close()
                            val guildsInfo = JSONArray(userGuildsResponse.body?.string())
                            userGuildsResponse.body?.close()
                            userGuildsResponse.close()

                            //Saving session and access info into memory...
                            val model = accountHandler.createDefaultModel()
                            model["logged_in"] = true
                            model["good_timezones"] = GoodTimezone.values()
                            model["announcement_types"] = AnnouncementType.values()
                            model["event_colors"] = EventColor.values()

                            //User info
                            model["id"] = userInfo.getString("id")
                            model["username"] = userInfo.getString("username")
                            model["discrim"] = userInfo.getString("discriminator")
                            if (userInfo.has("avatar") && !userInfo.isNull("avatar")) {
                                model["pfp"] = """${GlobalVal.discordCdnUrl}/avatars/
                                            |${userInfo.getString("id")}/
                                            |${userInfo.getString("avatar")}
                                            |.png""".trimMargin()
                            } else model["pfp"] = "/assets/img/default/pfp.png"

                            //Guild stuffs
                            val guilds = mutableListOf<WebPartialGuild>()
                            for (i in 0 until guildsInfo.length()) {
                                val jGuild = guildsInfo.getJSONObject(i)

                                val id = jGuild.getLong("id")
                                val name = jGuild.getString("name")
                                val icon: String = if (jGuild.has("icon") && !jGuild.isNull("icon")) {
                                    "${GlobalVal.discordCdnUrl}/icons/$id/${jGuild.getString("icon")}.png"
                                } else {
                                    "/assets/img/default/guild-icon.png"
                                }

                                guilds.add(WebPartialGuild(id, name, icon))
                            }
                            model["guilds"] = guilds

                            //Do temp API key request...
                            val keyGrantRequestBody = "".toRequestBody(GlobalVal.JSON)
                            val keyGrantRequest = Request.Builder()
                                    .url("${BotSettings.API_URL.get()}/v2/account/login")
                                    .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                                    .post(keyGrantRequestBody)
                                    .build()
                            val keyGrantResponseMono = Mono
                                    .fromCallable(client.newCall(keyGrantRequest)::execute)
                                    .subscribeOn(Schedulers.boundedElastic())


                            return@function Mono.zip(swe.session, keyGrantResponseMono)
                                    .flatMap(TupleUtils.function { session, keyGrantResponse ->
                                        //Generate new session ID
                                        val newSessionId = UUID.randomUUID().toString()
                                        session.attributes["account"] = newSessionId

                                        //Handle response
                                        if (keyGrantResponse.isSuccessful) {
                                            val keyGrantResponseBody = JSONObject(keyGrantResponse.body?.string())
                                            keyGrantResponse.body?.close()
                                            keyGrantResponse.close()
                                            //API Key received
                                            model["key"] = keyGrantResponseBody.getString("key")

                                            accountHandler.addAccount(model, swe)
                                                    .thenReturn("redirect:/dashboard")
                                        } else {
                                            //Something didn't work, just redirect back to login page
                                            LOGGER.debug("login issue | ${keyGrantResponse.body?.string()}")
                                            keyGrantResponse.body?.close()
                                            keyGrantResponse.close()

                                            Mono.just("redirect:/login")
                                        }
                                    })
                        })
            } else {
                //Token not provided. Auth denied or error. Redirect to login page
                return@flatMap Mono.just("redirect:/login")
            }
        }.doOnError {
            LOGGER.error("[Discord-login] Discord login failed", it)
        }
    }

    @GetMapping("/logout")
    fun handleLogout(swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe).flatMap { model ->
            if (!model.containsKey("key")) {
                //User isn't logged in, just redirect to home page
                return@flatMap Mono.just("redirect:/")
            } else {
                //Tell the API server the user has logged out and to delete the temp key
                val client = OkHttpClient()

                val requestBody = "".toRequestBody(GlobalVal.JSON)

                val logoutRequest = Request.Builder()
                        .url("${BotSettings.API_URL.get()}/v2/account/logout")
                        .header("Authorization", model["key"] as String)
                        .post(requestBody)
                        .build()

                return@flatMap Mono.fromCallable(client.newCall(logoutRequest)::execute)
                        .subscribeOn(Schedulers.boundedElastic())
                        .then(accountHandler.removeAccount(swe))
                        .thenReturn("redirect:/")
                        .doOnError {
                            LOGGER.error("[Web] Discord logout fail", it)
                        }.onErrorReturn("redirect:/")
            }
        }
    }
}
