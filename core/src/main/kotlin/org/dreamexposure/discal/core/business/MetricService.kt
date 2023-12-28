package org.dreamexposure.discal.core.business

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.dreamexposure.discal.core.`object`.StaticMessage
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class MetricService(
    private val meterRegistry: MeterRegistry,
) {
    fun recordInteractionDuration(handler: String, type: String, duration: Long) {
        meterRegistry.timer(
            "bot.interaction.duration",
            listOf(Tag.of("handler", handler), Tag.of("type", type))
        ).record(Duration.ofMillis(duration))
    }

    fun recordAnnouncementTaskDuration(scope: String, duration: Long) {
        meterRegistry.timer(
            "bot.discal.announcement.task.duration",
            listOf(Tag.of("scope", scope)),
        ).record(Duration.ofMillis(duration))
    }

    fun incrementAnnouncementPosted() {
        meterRegistry.counter(
            "bot.discal.announcement.posted",
        ).increment()
    }

    fun incrementStaticMessagesUpdated(type: StaticMessage.Type) {
        meterRegistry.counter(
            "bot.discal.static-messages.updated",
            listOf(Tag.of("type", type.name.lowercase())),
        ).increment()
    }
}
