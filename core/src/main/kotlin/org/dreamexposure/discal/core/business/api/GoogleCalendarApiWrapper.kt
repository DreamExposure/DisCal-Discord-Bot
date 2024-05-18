package org.dreamexposure.discal.core.business.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.exceptions.AccessRevokedException
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.ResponseModel
import org.dreamexposure.discal.core.`object`.new.model.google.OauthV4RefreshTokenResponse
import org.dreamexposure.discal.core.`object`.rest.ErrorResponse
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.stereotype.Component

@Component
class GoogleCalendarApiWrapper(
    private val httpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
) {
    suspend fun refreshAccessToken(refreshToken: String): ResponseModel<OauthV4RefreshTokenResponse> {
        val requestFormBody = FormBody.Builder()
            .addEncoded("client_id", Config.SECRET_GOOGLE_CLIENT_ID.getString())
            .addEncoded("client_secret", Config.SECRET_GOOGLE_CLIENT_SECRET.getString())
            .addEncoded("refresh_token", refreshToken)
            .addEncoded("grant_type", "refresh_token")
            .build()
        val request = Request.Builder()
            .url("https://www.googleapis.com/oauth2/v4/token")
            .post(requestFormBody)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val response = makeRequest(request, OauthV4RefreshTokenResponse::class.java)


        // TODO: Handling of this should be moved up higher in the impl?
        if (response.error?.error == "invalid_grant") {
            LOGGER.debug(DEFAULT, "Google Oauth invalid_grant for access token refresh")
            throw AccessRevokedException() // TODO: How should I handle this for external calendars? Right now we just delete everything
        } else if (response.error != null) {
            LOGGER.error(DEFAULT, "[Google] Error requesting new access token | ${response.code} | ${response.error.error}")
        }

        return response
    }

    private suspend fun <T> makeRequest(request: Request, valueType: Class<T>): ResponseModel<T> {
        var response: Response? = null

        try {
            response = httpClient.newCall(request).executeAsync()

            when (response.code) {
                200 -> {
                    val data = objectMapper.readValue(response.body!!.string(), valueType)
                    response.body?.close()
                    response.close()

                    return ResponseModel(data)
                }
                else -> {
                    val error = objectMapper.readValue<ErrorResponse>(response.body!!.string())
                    response.body?.close()
                    response.close()

                    return ResponseModel(error, response.code)
                }
            }

        } catch (ex: Exception) {
            LOGGER.error("Error making request host:${request.url.host} | uri:${request.url.encodedPath} | code:${response?.code}", ex)
            throw ex // Rethrow and let implementation decide proper handling for exception
        } finally {
            response?.body?.close()
            response?.close()
        }
    }
}
