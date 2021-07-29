package org.dreamexposure.discal.web.network.discal

import com.google.api.client.http.HttpStatusCodes
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.NetworkInfo
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.json.JSONObject
import reactor.core.publisher.Mono

object StatusHandler {
    fun getLatestStatusInfo(): Mono<NetworkInfo> {
        return Mono.fromCallable {
            val client = OkHttpClient()
            val body = "".toRequestBody(GlobalVal.JSON)

            val request = Request.Builder()
                    .url("${BotSettings.API_URL.get()}/v2/status/get")
                    .post(body)
                    .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                    .header("Content-Type", "application/json")
                    .build()

            return@fromCallable client.newCall(request).execute()
        }.map { response ->
            if (response.code == HttpStatusCodes.STATUS_CODE_OK) {
                val body = response.body?.string()
                response.body?.close()
                return@map NetworkInfo().fromJson(JSONObject(body))
            } else {
                return@map NetworkInfo() //Just return an empty object, it's fine.
            }
        }.doOnError {
            LOGGER.error("[Status Request] Failed to get status", it)
        }.onErrorReturn(NetworkInfo()).defaultIfEmpty(NetworkInfo())
    }
}
