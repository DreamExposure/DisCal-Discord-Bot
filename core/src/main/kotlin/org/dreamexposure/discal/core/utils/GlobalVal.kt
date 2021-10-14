package org.dreamexposure.discal.core.utils

import discord4j.common.util.Snowflake
import discord4j.rest.util.Color
import io.github.furstenheim.CodeBlockStyle
import io.github.furstenheim.CopyDown
import io.github.furstenheim.OptionsBuilder
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.jsoup.safety.Whitelist
import org.slf4j.Marker
import org.slf4j.MarkerFactory

object GlobalVal {
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

    const val discordApiUrl = "https://discord.com/api/v6"

    const val discordCdnUrl = "https://cdn.discordapp.com"

    val JSON = "application/json; charset=utf-8".toMediaType()

    val HTTP_CLIENT: OkHttpClient = OkHttpClient()

    val JSON_FORMAT = Json { encodeDefaults = true }

    val HTML_WHITELIST: Whitelist
        get() {
            return Whitelist.basic()
                    .preserveRelativeLinks(false)
                    .removeAttributes("sub", "sup", "small")
        }

    val MARKDOWN_CONVERTER: CopyDown
        get() {
            return CopyDown(OptionsBuilder.anOptions()
                    .withStrongDelimiter("**")
                    .withEmDelimiter("*")
                    .withFence("```")
                    .withHr("---")
                    .withCodeBlockStyle(CodeBlockStyle.FENCED)
                    .build()
            )
        }

    @JvmStatic
    val DEFAULT: Marker = MarkerFactory.getMarker("DISCAL_WEBHOOK_DEFAULT")

    val STATUS: Marker = MarkerFactory.getMarker("DISCAL_WEBHOOK_STATUS")

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
}
