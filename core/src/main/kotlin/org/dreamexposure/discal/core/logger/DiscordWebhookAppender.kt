package org.dreamexposure.discal.core.logger

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed.*
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.utils.GlobalVal
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import java.text.SimpleDateFormat

class DiscordWebhookAppender : AppenderBase<ILoggingEvent>() {
    private val defaultHook: WebhookClient?
    private val statusHook: WebhookClient?

    init {
        if (BotSettings.USE_WEBHOOKS.get().equals("true", true)) {
            defaultHook = WebhookClient.withUrl(BotSettings.DEFAULT_WEBHOOK.get())
            statusHook = WebhookClient.withUrl(BotSettings.STATUS_WEBHOOK.get())
        } else {
            defaultHook = null
            statusHook = null
        }
    }

    override fun append(eventObject: ILoggingEvent) {
        if (BotSettings.USE_WEBHOOKS.get().equals("true", true)) {
            when {
                eventObject.marker.equals(DEFAULT) -> executeDefault(eventObject)
                eventObject.marker.equals(STATUS) -> executeStatus(eventObject)
            }
        }
    }

    private fun executeStatus(event: ILoggingEvent) {
        val content = WebhookEmbedBuilder()
                .setTitle(EmbedTitle("Status", null))
                .addField(EmbedField(true, "Shard Index", Application.getShardIndex()))
                .addField(EmbedField(false, "Time", SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(event.timeStamp)))
                .addField(EmbedField(false, "Thread", event.threadName))
                .addField(EmbedField(true, "Level", event.level.levelStr))
                .addField(EmbedField(true, "Logger", event.loggerName))
                .setDescription(event.formattedMessage)
                .setColor(GlobalVal.discalColor.rgb)
                .setFooter(EmbedFooter("v${GlobalVal.version}", null))

        if (event.throwableProxy != null) {
            content.addField(EmbedField(false, "Error Message", event.throwableProxy.message))
            content.addField(EmbedField(false, "Stacktrace", "Stacktrace can be found in exceptions log file"))
        }

        this.statusHook?.send(content.build())
    }

    private fun executeDefault(event: ILoggingEvent) {
        val content = WebhookEmbedBuilder()
                .setTitle(EmbedTitle(event.level.levelStr, null))
                .addField(EmbedField(true, "Shard Index", Application.getShardIndex()))
                .addField(EmbedField(false, "Time", SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(event.timeStamp)))
                .addField(EmbedField(false, "Thread", event.threadName))
                .addField(EmbedField(true, "Level", event.level.levelStr))
                .addField(EmbedField(true, "Logger", event.loggerName))
                .setDescription(event.formattedMessage)
                .setColor(GlobalVal.discalColor.rgb)
                .setFooter(EmbedFooter("v${GlobalVal.version}", null))

        if (event.throwableProxy != null) {
            content.addField(EmbedField(false, "Error Message", event.throwableProxy.message))
            content.addField(EmbedField(false, "Stacktrace", "Stacktrace can be found in exceptions log file"))
        }

        this.defaultHook?.send(content.build())
    }


}
