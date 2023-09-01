package org.dreamexposure.discal.cam.discord

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.reactor.awaitSingle
import okhttp3.FormBody
import okhttp3.Request
import org.dreamexposure.discal.cam.json.discord.AccessTokenResponse
import org.dreamexposure.discal.cam.json.discord.AuthorizationInfo
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.exceptions.AuthenticationException
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.HTTP_CLIENT
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class DiscordOauthHandler(
    private val objectMapper: ObjectMapper,
) {
    private val cdnUrl = "https://cdn.discordapp.com"
    private val redirectUrl = Config.URL_DISCORD_REDIRECT.getString()
    private val clientId = Config.DISCORD_APP_ID.getString()
    private val clientSecret = Config.SECRET_CLIENT_SECRET.getString()

    suspend fun doTokenExchange(code: String): AccessTokenResponse {
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

        val response = Mono.fromCallable(HTTP_CLIENT.newCall(tokenExchangeRequest)::execute)
            .subscribeOn(Schedulers.boundedElastic())
            .awaitSingle()

        if (response.isSuccessful) {
            val responseBody = objectMapper.readValue<AccessTokenResponse>(response.body!!.string())
            response.close()

            return  responseBody
        } else {
            throw AuthenticationException("Discord authorization grant error")
        }
    }

    suspend fun doTokenRefresh(refreshToken: String): AccessTokenResponse {
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

        val response = Mono.fromCallable(HTTP_CLIENT.newCall(tokenExchangeRequest)::execute)
            .subscribeOn(Schedulers.boundedElastic())
            .awaitSingle()

        if (response.isSuccessful) {
            val responseBody = objectMapper.readValue<AccessTokenResponse>(response.body!!.string())
            response.close()

            return  responseBody
        } else {
            throw AuthenticationException("Discord refresh token error")
        }
    }


    suspend fun getOauthInfo(accessToken: String): AuthorizationInfo {
        val request = Request.Builder()
            .url("${GlobalVal.discordApiUrl}/oauth2/@me")
            .get()
            .header("Authorization", "Bearer $accessToken")
            .build()

        val response = Mono.fromCallable(HTTP_CLIENT.newCall(request)::execute)
            .subscribeOn(Schedulers.boundedElastic())
            .awaitSingle()

        if (response.isSuccessful) {
            var responseBody = objectMapper.readValue<AuthorizationInfo>(response.body!!.string())
            response.close()

            //Convert avatar hash to full URL
            val avatar = if (responseBody.user!!.avatar != null) {
                val userId = responseBody.user!!.id.asString()
                val avatarHash = responseBody.user!!.avatar
                "$cdnUrl/avatars/$userId/$avatarHash.png"
            } else {
                // No avatar present, get discord's default user avatar
                val discrim = responseBody.user!!.discriminator
                "$cdnUrl/embed/avatars/${discrim.toInt() % 5}.png"
            }
            responseBody = responseBody.copy(user = responseBody.user!!.copy(avatar = avatar))

            return responseBody
        } else {
            throw AuthenticationException("Discord auth info error")
        }
    }
}
