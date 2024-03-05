package org.dreamexposure.discal.core.business

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.dreamexposure.discal.core.`object`.new.StaticMessage
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

    fun recordTaskDuration(task: String, tags: List<Tag> = listOf(), duration: Long) {
        meterRegistry.timer(
            "bot.task.duration",
            tags.plus(Tag.of("task", task))
        ).record(Duration.ofMillis(duration))
    }

    fun recordAnnouncementTaskDuration(scope: String, duration: Long) {
        recordTaskDuration("announcement", listOf(Tag.of("scope", scope)), duration)
    }

    fun incrementAnnouncementPosted() {
        meterRegistry.counter("bot.discal.announcement.posted").increment()
    }

    fun recordStaticMessageTaskDuration(scope: String, duration: Long) {
        recordTaskDuration("static_message", listOf(Tag.of("scope", scope)), duration)
    }

    fun incrementStaticMessagesUpdated(type: StaticMessage.Type) {
        meterRegistry.counter(
            "bot.discal.static-messages.updated",
            listOf(Tag.of("type", type.name.lowercase())),
        ).increment()
    }
}
