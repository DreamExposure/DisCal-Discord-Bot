package org.dreamexposure.discal.web.network.discal

import com.google.api.client.http.HttpStatusCodes
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.NetworkData
import org.dreamexposure.discal.core.utils.GlobalVal.JSON
import org.dreamexposure.discal.core.utils.GlobalVal.JSON_FORMAT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class StatusHandler(
    @Value("\${bot.url.api}")
    private val apiUrl: String,
) {
    fun getLatestStatusInfo(): Mono<NetworkData> {
        return Mono.fromCallable {
            val client = OkHttpClient()
            val body = "".toRequestBody(JSON)

            val request = Request.Builder()
                    .url("$apiUrl/v2/status/get")
                    .post(body)
                    .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                    .header("Content-Type", "application/json")
                    .build()

            return@fromCallable client.newCall(request).execute()
        }.map { response ->
            if (response.code == HttpStatusCodes.STATUS_CODE_OK) {
                val body = response.body?.string()
                response.body?.close()
                response.close()
                return@map JSON_FORMAT.decodeFromString(NetworkData.serializer(), body!!)
            } else {
                return@map NetworkData() //Just return an empty object, it's fine.
            }
        }.doOnError {
            LOGGER.error("[Status Request] Failed to get status", it)
        }.onErrorReturn(NetworkData()).defaultIfEmpty(NetworkData())
    }
}
