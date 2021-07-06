package org.dreamexposure.discal.core.utils

import discord4j.common.util.Snowflake
import discord4j.rest.util.Color
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.dreamexposure.discal.core.`object`.BotSettings
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PropertiesLoaderUtils
import java.io.IOException

object GlobalVal {
    @JvmStatic
    val version: String

    @JvmStatic
    val d4jVersion: String

    @JvmStatic
    var iconUrl: String = ""

    @JvmStatic
    val lineBreak: String = System.getProperty("line.separator")

    @JvmStatic
    val devUserIds = listOf(
            Snowflake.of(130510525770629121L), //NovaFox161
            Snowflake.of(135995653095555073L), //Dannie <3
    )

    @JvmStatic
    val discalColor: Color = Color.of(56, 138, 237)

    @JvmStatic
    val discalDashboardLink = "${BotSettings.BASE_URL.get()}/dashboard"

    const val discordApiUrl = "https://discord.com/api/v6"

    const val discordCdnUrl = "https://cdn.discordapp.com"

    @JvmStatic
    val JSON = "application/json; charset=utf-8".toMediaType()

    val JSON_FORMAT = Json { encodeDefaults = true }

    const val STATUS_SUCCESS = 200
    const val STATUS_BAD_REQUEST = 400
    const val STATUS_NOT_FOUND = 404
    const val STATUS_NOT_ALLOWED = 405
    const val STATUS_AUTHORIZATION_DENIED = 401
    const val STATUS_FORBIDDEN = 403
    const val STATUS_GONE = 410
    const val STATUS_TEAPOT = 418
    const val STATUS_PRECONDITION_REQUIRED = 428
    const val STATUS_RATE_LIMITED = 429
    const val STATUS_INTERNAL_ERROR = 500

    init {
        var version1: String
        var d4jVersion1: String
        try {
            val resource: Resource = ClassPathResource("/application.properties")
            val p = PropertiesLoaderUtils.loadProperties(resource)
            version1 = p.getProperty("application.version")
            d4jVersion1 = "Discord4J v" + p.getProperty("library.discord4j.version")
        } catch (e: IOException) {
            version1 = "Unknown"
            d4jVersion1 = "Unknown"
        }
        version = version1
        d4jVersion = d4jVersion1
    }
}
