package org.dreamexposure.discal.core.business.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.coroutines.executeAsync
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.ResponseModel
import org.dreamexposure.discal.core.`object`.rest.ErrorResponse
import org.springframework.stereotype.Component

@Component
class ApiWrapperClient(
    private val httpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    internal suspend fun <T> makeRequest(request: Request, valueType: Class<T>): ResponseModel<T> {
        var response: Response? = null

        try {
            response = httpClient.newCall(request).executeAsync()

            when (response.code) {
                200 -> {
                    val data = objectMapper.readValue(response.body.string(), valueType)
                    response.body.close()
                    response.close()

                    return ResponseModel(data)
                }
                else -> {
                    val error = objectMapper.readValue<ErrorResponse>(response.body.string())
                    response.body.close()
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
