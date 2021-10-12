package org.dreamexposure.discal.core.`object`.network.discal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.GitProperty
import org.dreamexposure.discal.core.extensions.getHumanReadable
import org.dreamexposure.discal.core.serializers.DurationAsStringSerializer
import org.dreamexposure.discal.core.serializers.InstantAsStringSerializer
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Suppress("DataClassPrivateConstructor")
@Serializable
data class InstanceData(
    @SerialName("instance_id")
    val instanceId: String = Application.instanceId.toString(),

    val version: String = GitProperty.DISCAL_VERSION.value,

    @SerialName("d4j_version")
    val d4jVersion: String = GitProperty.DISCAL_VERSION_D4J.value,

    @Serializable(with = DurationAsStringSerializer::class)
    val uptime: Duration = Application.getUptime(),

    @SerialName("last_heartbeat")
    @Serializable(with = InstantAsStringSerializer::class)
    val lastHeartbeat: Instant = Instant.now(),

    val memory: Double = Application.getMemoryUsedInMb(),
) {
    @Suppress("unused") //Used in thymeleaf status page
    fun getHumanReadableHeartbeat(): String {
        val formatter = DateTimeFormatter
            .ofPattern("yyyy/MM/dd HH:mm:ss")
            .withZone(ZoneId.of("UTC"))

        return "${formatter.format(lastHeartbeat)} UTC"
    }

    @Suppress("unused") //Used in thymeleaf status page
    fun getHumanReadableUptime(): String = uptime.getHumanReadable()
}
