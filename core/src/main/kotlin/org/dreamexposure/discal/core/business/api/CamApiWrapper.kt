package org.dreamexposure.discal.core.business.api

import com.fasterxml.jackson.databind.ObjectMapper
import discord4j.common.util.Snowflake
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.enums.calendar.CalendarHost
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.ResponseModel
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.SecurityValidateV1Request
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.SecurityValidateV1Response
import org.dreamexposure.discal.core.`object`.new.model.discal.cam.TokenV1Model
import org.dreamexposure.discal.core.utils.GlobalVal.JSON
import org.springframework.stereotype.Component

@Component
class CamApiWrapper(
    private val apiWrapperClient: ApiWrapperClient,
    private val objectMapper: ObjectMapper,
) {
    private final val CAM_URL = Config.URL_CAM.getString()
    private final val AUTH_HEADER = "Int ${Config.SECRET_DISCAL_API_KEY.getString()}"

    suspend fun validateToken(requestBody: SecurityValidateV1Request): ResponseModel<SecurityValidateV1Response> {
        val request = Request.Builder()
            .url("${Config.URL_CAM.getString()}/v1/security/validate")
            .post(objectMapper.writeValueAsString(requestBody).toRequestBody(JSON))
            .header("Authorization", AUTH_HEADER)
            .header("Content-Type", "application/json")
            .build()

        return apiWrapperClient.makeRequest(request, SecurityValidateV1Response::class.java)
    }

    suspend fun getCalendarToken(credentialId: Int): ResponseModel<TokenV1Model> {
        LOGGER.debug("Getting calendar token for credential:$credentialId")

        val url = "$CAM_URL/v1/token".toHttpUrl().newBuilder()
            .addQueryParameter("host", CalendarHost.GOOGLE.name)
            .addQueryParameter("id", credentialId.toString())
            .build()

        val request = Request.Builder().get()
            .header("Authorization", AUTH_HEADER)
            .url(url)
            .build()

        return apiWrapperClient.makeRequest(request, TokenV1Model::class.java)
    }

    suspend fun getCalendarToken(guildId: Snowflake, calNumber: Int, host: CalendarHost): ResponseModel<TokenV1Model> {
        LOGGER.debug("Getting calendar token for guild:{} | host:{} | calendarId:{} ", guildId.asLong(), host.name, calNumber)

        val url = "$CAM_URL/v1/token".toHttpUrl().newBuilder()
            .addQueryParameter("host", host.name)
            .addQueryParameter("guild", guildId.asString())
            .addQueryParameter("id", calNumber.toString())
            .build()

        val request = Request.Builder().get()
            .header("Authorization",  AUTH_HEADER)
            .url(url)
            .build()

        return apiWrapperClient.makeRequest(request, TokenV1Model::class.java)
    }
}
