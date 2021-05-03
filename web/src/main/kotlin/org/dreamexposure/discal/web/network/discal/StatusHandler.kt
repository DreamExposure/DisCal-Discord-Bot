package org.dreamexposure.discal.web.network.discal

import com.google.api.client.http.HttpStatusCodes
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.NetworkInfo
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.utils.GlobalConst
import org.json.JSONObject
import reactor.core.publisher.Mono

object StatusHandler {
    fun getLatestStatusInfo(): Mono<NetworkInfo> {
        return Mono.fromCallable {
            val client = OkHttpClient()
            val body = RequestBody.create(GlobalConst.JSON, "")

            val request = Request.Builder()
                    .url("${BotSettings.API_URL_INTERNAL.get()}/v2/status/get")
                    .post(body)
                    .header("Authorization", BotSettings.BOT_API_TOKEN.get())
                    .header("Content-Type", "application/json")
                    .build()

            return@fromCallable client.newCall(request).execute()
        }.map { response ->
            if (response.code() == HttpStatusCodes.STATUS_CODE_OK) {
                return@map NetworkInfo().fromJson(JSONObject(response.body()?.string()))
            } else {
                return@map NetworkInfo() //Just return an empty object, its fine.
            }
        }.doOnError {
            LogFeed.log(LogObject.forException("[Status Request] Failed to get status", it, this.javaClass))
        }.onErrorReturn(NetworkInfo()).defaultIfEmpty(NetworkInfo())
    }
}
