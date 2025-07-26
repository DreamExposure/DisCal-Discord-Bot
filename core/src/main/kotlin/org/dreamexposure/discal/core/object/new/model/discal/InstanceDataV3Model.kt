package org.dreamexposure.discal.core.`object`.new.model.discal

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.GitProperty
import java.time.Duration
import java.time.Instant

data class InstanceDataV3Model(
    val instanceId: String = Application.instanceId.toString(),
    val version: String = GitProperty.DISCAL_VERSION.value,
    val d4jVersion: String = GitProperty.DISCAL_VERSION_D4J.value,
    val uptime: Duration = Application.getUptime(),
    val lastHeartbeat: Instant = Instant.now(),
    val memory: Double = Application.getMemoryUsedInMb(),
)