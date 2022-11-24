package org.dreamexposure.discal.cam.discord

import okhttp3.FormBody
import okhttp3.Request
import org.dreamexposure.discal.cam.json.discord.AccessTokenResponse
import org.dreamexposure.discal.cam.json.discord.AuthorizationInfo
import org.dreamexposure.discal.core.exceptions.AuthenticationException
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.HTTP_CLIENT
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class DiscordOauthHandler(
    @Value("\${bot.url.discord.redirect}")
    private val redirectUrl: String,
    @Value("\${bot.discord-app-id}")
    private val clientId: String,
    @Value("\${bot.secret.client-secret}")
    private val clientSecret: String,
) {
    private val CDN_URL = "https://cdn.discordapp.com"

    fun doTokenExchange(code: String): Mono<AccessTokenResponse> {
        return Mono.fromCallable {
            val body = FormBody.Builder()
                .addEncoded("client_id", clientId)
                .addEncoded("client_secret", clientSecret)
                .addEncoded("grant_type", "authorization_code")
                .addEncoded("code", code)
                .addEncoded("redirect_uri", redirectUrl)
                .build()

            val tokenExchangeRequest = Request.Builder()
                .url("${GlobalVal.discordApiUrl}/oauth2/token")
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            HTTP_CLIENT.newCall(tokenExchangeRequest).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
            if (response.isSuccessful) {
                //Transform body into our object
                val responseBody = JSON_FORMAT.decodeFromString(AccessTokenResponse.serializer(),
                    response.body!!.string())
                // Close body to avoid mem leak
                response.body?.close()

                Mono.just(responseBody)
            } else {
                Mono.error(AuthenticationException("Discord authorization grant error"))
            }
        }
    }

    fun doTokenRefresh(refreshToken: String): Mono<AccessTokenResponse> {
        return Mono.fromCallable {
            val body = FormBody.Builder()
                .addEncoded("client_id", clientId)
                .addEncoded("client_secret", clientSecret)
                .addEncoded("grant_type", "refresh_token")
                .addEncoded("refresh_token", refreshToken)
                .build()

            val tokenExchangeRequest = Request.Builder()
                .url("${GlobalVal.discordApiUrl}/oauth2/token")
                .post(body)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            HTTP_CLIENT.newCall(tokenExchangeRequest).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
            if (response.isSuccessful) {
                //Transform body into our object
                val responseBody = JSON_FORMAT.decodeFromString(AccessTokenResponse.serializer(),
                    response.body!!.string())
                // Close body to avoid mem leak
                response.body?.close()

                Mono.just(responseBody)
            } else {
                Mono.error(AuthenticationException("Discord refresh token error"))
            }
        }
    }

    fun getOauthInfo(accessToken: String): Mono<AuthorizationInfo> {
        return Mono.fromCallable {
            val request = Request.Builder()
                .url("${GlobalVal.discordApiUrl}/oauth2/@me")
                .get()
                .header("Authorization", "Bearer $accessToken")
                .build()

            HTTP_CLIENT.newCall(request).execute()
        }.subscribeOn(Schedulers.boundedElastic()).flatMap { response ->
            if (response.isSuccessful) {
                //Transform body into our object
                var responseBody = JSON_FORMAT.decodeFromString(AuthorizationInfo.serializer(),
                    response.body!!.string())

                //Convert avatar hash to full URL
                val avatar = if (responseBody.user!!.avatar != null) {
                    val userId = responseBody.user!!.id.asString()
                    val avatarHash = responseBody.user!!.avatar
                    "$CDN_URL/avatars/$userId/$avatarHash.png"
                } else {
                    // No avatar present, get discord's default user avatar
                    val discrim = responseBody.user!!.discriminator
                    "$CDN_URL/embed/avatars/${discrim.toInt() % 5}.png"
                }
                responseBody = responseBody.copy(user = responseBody.user!!.copy(avatar = avatar))

                Mono.just(responseBody)
            } else {
                Mono.error(AuthenticationException("Discord auth info error"))
            }
        }
    }
}
