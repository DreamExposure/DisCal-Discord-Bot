package org.dreamexposure.discal.core.logger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed.*
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import org.dreamexposure.discal.GitProperty
import org.dreamexposure.discal.core.extensions.embedDescriptionSafe
import org.dreamexposure.discal.core.extensions.embedFieldSafe
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import java.io.FileReader
import java.time.Instant
import java.util.*

class DiscordWebhookAppender : AppenderBase<ILoggingEvent>() {
    private val defaultHook: WebhookClient?
    private val statusHook: WebhookClient?
    private val useWebhooks: Boolean
    private val allErrorsWebhook: Boolean
    private val appName: String

    init {
        val appProps = Properties()
        appProps.load(FileReader("application.properties"))

        useWebhooks = appProps.getProperty("bot.logging.webhooks.use", "false").toBoolean()
        appName = appProps.getProperty("spring.application.name")

        if (useWebhooks) {
            defaultHook = WebhookClient.withUrl(appProps.getProperty("bot.secret.default-webhook"))
            statusHook = WebhookClient.withUrl(appProps.getProperty("bot.secret.status-webhook"))
            allErrorsWebhook = appProps.getProperty("bot.logging.webhooks.all-errors", "false").toBoolean()
        } else {
            defaultHook = null
            statusHook = null
            allErrorsWebhook = false
        }
    }

    override fun append(eventObject: ILoggingEvent) {
        if (!useWebhooks) return

        when {
            eventObject.level.equals(Level.ERROR) && allErrorsWebhook -> {
                executeDefault(eventObject)
                return
            }

            eventObject.marker.equals(STATUS) -> {
                executeStatus(eventObject)
                return
            }

            eventObject.marker.equals(DEFAULT) -> {
                executeDefault(eventObject)
                return
            }
        }
    }

    private fun executeStatus(event: ILoggingEvent) {
        val content = WebhookEmbedBuilder()
            .setTitle(EmbedTitle("Status", null))
            .addField(EmbedField(true, "Application", appName))
            //TODO: Shard index
            .addField(EmbedField(true, "Time", "<t:${event.timeStamp / 1000}:f>"))
            .addField(EmbedField(false, "Logger", event.loggerName.embedFieldSafe()))
            .addField(EmbedField(true, "Level", event.level.levelStr))
            .addField(EmbedField(true, "Thread", event.threadName.embedFieldSafe()))
            .setDescription(event.formattedMessage.embedDescriptionSafe())
            .setColor(getEmbedColor(event))
            .setFooter(EmbedFooter("v${GitProperty.DISCAL_VERSION.value}", null))
            .setTimestamp(Instant.now())

        if (event.throwableProxy != null) {
            content.addField(EmbedField(false, "Error Message", event.throwableProxy.message.embedFieldSafe()))
            content.addField(EmbedField(false, "Stacktrace", "Stacktrace can be found in exceptions log file"))
        }

        this.statusHook?.send(content.build())
    }

    private fun executeDefault(event: ILoggingEvent) {
        val content = WebhookEmbedBuilder()
            .setTitle(EmbedTitle(event.level.levelStr, null))
            .addField(EmbedField(true, "Application", appName))
            //TODO: Shard index
            .addField(EmbedField(true, "Time", "<t:${event.timeStamp / 1000}:f>"))
            .addField(EmbedField(false, "Logger", event.loggerName.embedFieldSafe()))
            .addField(EmbedField(true, "Level", event.level.levelStr))
            .addField(EmbedField(true, "Thread", event.threadName.embedFieldSafe()))
            .setDescription(event.formattedMessage.embedDescriptionSafe())
            .setColor(getEmbedColor(event))
            .setFooter(EmbedFooter("v${GitProperty.DISCAL_VERSION.value}", null))
            .setTimestamp(Instant.now())

        if (event.throwableProxy != null) {
            content.addField(EmbedField(false, "Error Message", event.throwableProxy.message.embedFieldSafe()))
            content.addField(EmbedField(false, "Stacktrace", "Stacktrace can be found in exceptions log file"))
        }

        this.defaultHook?.send(content.build())
    }


    private fun getEmbedColor(event: ILoggingEvent): Int {
        return if (event.level.equals(Level.ERROR) || event.throwableProxy != null) GlobalVal.errorColor.rgb
        else if (event.level.equals(Level.WARN)) GlobalVal.warnColor.rgb
        else GlobalVal.discalColor.rgb
    }
}
