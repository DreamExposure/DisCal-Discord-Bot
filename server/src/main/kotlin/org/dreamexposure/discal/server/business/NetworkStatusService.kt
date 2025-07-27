package org.dreamexposure.discal.server.business

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.NetworkStatusCache
import org.dreamexposure.discal.core.business.AnnouncementService
import org.dreamexposure.discal.core.business.CalendarService
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.new.model.discal.BotInstanceDataV3Model
import org.dreamexposure.discal.core.`object`.new.model.discal.InstanceDataV3Model
import org.dreamexposure.discal.core.`object`.new.model.discal.NetworkDataV3Model
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class NetworkStatusService(
    private val calendarService: CalendarService,
    private val announcementService: AnnouncementService,
    private val networkStatusCache: NetworkStatusCache,
) {
    suspend fun getNetworkStatus(): NetworkDataV3Model {
        var status = networkStatusCache.get(key = 0)
        if (status != null) return status

        LOGGER.info(STATUS, "Cached network state not stored, treating as lost state")

        status = NetworkDataV3Model(apiStatus = listOf(InstanceDataV3Model()))
        networkStatusCache.put(key = 0, value = status)

        return status
    }

    suspend fun updateNetworkStatus(status: NetworkDataV3Model) {
        networkStatusCache.put(key = 0, value = status)
    }

    suspend fun updateAndReturnStatus(): NetworkDataV3Model {
        // Get the things we need
        val status = getNetworkStatus()
        val thisInstance = status.apiStatus
            .find { it.instanceId == Application.instanceId.toString() }
            ?: InstanceDataV3Model()
        val totalCalendars = calendarService.getCalendarCount().toInt()
        val totalAnnouncements = announcementService.getAnnouncementCount().toInt()

        // Update that shit
        val updatedStatus = status.copy(
            apiStatus = status.apiStatus
                .filter { it.instanceId != thisInstance.instanceId } // Remove this instance's old data
                .plus(thisInstance.copy(lastHeartbeat = Instant.now(), uptime = Application.getUptime())),
            totalCalendars = totalCalendars,
            totalAnnouncements = totalAnnouncements,
        )

        updateNetworkStatus(updatedStatus)
        return updatedStatus
    }

    suspend fun handleWebsiteHeartbeat(data: InstanceDataV3Model) {
        LOGGER.debug("Web heartbeat incoming...")

        val networkStatus = getNetworkStatus()

        if (networkStatus.websiteStatus == null)
            LOGGER.info(STATUS, "Website now connected")
        else if (networkStatus.websiteStatus!!.instanceId != data.instanceId)
            LOGGER.info(STATUS, "Website instance ID changed")

        updateNetworkStatus(networkStatus.copy(websiteStatus = data))
    }

    suspend fun handleCamHeartbeat(data: InstanceDataV3Model) {
        LOGGER.debug("Cam heartbeat incoming...")

        val networkStatus = getNetworkStatus()
        val existing = networkStatus.camStatus.find { it.instanceId == data.instanceId }

        if (existing == null) LOGGER.info(STATUS, "CAM instance connected to network | Id: ${data.instanceId}")

        updateNetworkStatus(networkStatus.copy(
            camStatus = networkStatus.camStatus
                .filter { it.instanceId != data.instanceId } // Remove updated instance
                .plus(data)
        ))
    }

    suspend fun handleBotHeartbeat(data: BotInstanceDataV3Model) {

        LOGGER.debug("Bot heartbeat incoming...")

        val networkStatus = getNetworkStatus()
        val existing = networkStatus.botStatus.find { it.shardIndex == data.shardIndex }
        if (existing == null)
            LOGGER.info(STATUS, "Shard connected to network | Index ${data.shardIndex}")
        else if (existing.instanceData.instanceId != data.instanceData.instanceId)
            LOGGER.info(STATUS, "Shard instance ID changed | Index ${data.shardIndex}")

        updateNetworkStatus(networkStatus.copy(
            botStatus = networkStatus.botStatus
                .filter { it.shardIndex != data.shardIndex } // Remove updated instance
                .plus(data)
                .sortedWith(Comparator.comparingInt(BotInstanceDataV3Model::shardIndex))
        ))
    }

    suspend fun doNetworkStatusHealthCheck() {
        val status = updateAndReturnStatus()

        // I should do something to attempt to restart these, but that's not something I plan to implement any time soon
        val oldApiInstances = status.apiStatus
            .filter { Instant.now().isAfter(it.lastHeartbeat.plus(5, ChronoUnit.MINUTES)) }
            .onEach { LOGGER.warn(STATUS, "API instance disconnected from network | Id: ${it.instanceId}") }
        val oldCamInstances = status.camStatus
            .filter { Instant.now().isAfter(it.lastHeartbeat.plus(5, ChronoUnit.MINUTES)) }
            .onEach { LOGGER.warn(STATUS, "Cam disconnected from network | Id: ${it.instanceId}") }
        val oldBotInstances = status.botStatus
            .filter { Instant.now().isAfter(it.instanceData.lastHeartbeat.plus(5, ChronoUnit.MINUTES)) }
            .onEach { LOGGER.warn(STATUS, "Client disconnected from network | Index: ${it.shardIndex}") }

        if (oldApiInstances.isNotEmpty() || oldCamInstances.isNotEmpty() || oldBotInstances.isNotEmpty()) {
            updateNetworkStatus(status.copy(
                apiStatus = status.apiStatus.minus(oldApiInstances),
                camStatus = status.camStatus.minus(oldCamInstances),
                botStatus = status.botStatus.minus(oldBotInstances)
            ))
        }
    }

}