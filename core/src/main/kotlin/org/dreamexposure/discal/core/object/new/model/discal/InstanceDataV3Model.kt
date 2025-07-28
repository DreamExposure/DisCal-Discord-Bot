package org.dreamexposure.discal.core.`object`.new.model.discal

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.GitProperty
import org.dreamexposure.discal.core.extensions.getHumanReadable
import java.time.Duration
import java.time.Instant

data class InstanceDataV3Model(
    val instanceId: String = Application.instanceId.toString(),
    val version: String = GitProperty.DISCAL_VERSION.value,
    val d4jVersion: String = GitProperty.DISCAL_VERSION_D4J.value,
    val uptime: Duration = Application.getUptime(),
    // TODO: This really should just be instant, but my custom jackson mapper doesn't seem to be working
    val lastHeartbeat: String = Instant.now().toString(),
    val memory: Double = Application.getMemoryUsedInMb(),
) {
    val humanUptime: String
        get() = uptime.getHumanReadable()
}