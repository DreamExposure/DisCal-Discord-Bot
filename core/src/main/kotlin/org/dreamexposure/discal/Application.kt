package org.dreamexposure.discal

import org.dreamexposure.discal.core.config.Config
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import java.lang.management.ManagementFactory
import java.time.Duration
import java.util.*
import kotlin.math.roundToInt

@SpringBootApplication(exclude = [SessionAutoConfiguration::class])
class Application {
    companion object {
        val instanceId: UUID = UUID.randomUUID()

        fun getShardIndex(): Int {
            val k8sPodIndex = System.getenv("KUBERNETES_POD_INDEX")
            return k8sPodIndex?.toInt() ?: // Fall back to config
            Config.SHARD_INDEX.getInt()
        }

        fun getShardCount(): Int {
            val shardCount = System.getenv("SHARD_COUNT")
            return shardCount?.toInt() ?: //Fall back to config
            Config.SHARD_COUNT.getInt()
        }

        fun getUptime(): Duration {
            val mxBean = ManagementFactory.getRuntimeMXBean()

            val rawDuration = System.currentTimeMillis() - mxBean.startTime
            return Duration.ofMillis(rawDuration)
        }

        fun getMemoryUsedInMb(): Double {
            val totalMemory = Runtime.getRuntime().totalMemory()
            val freeMemory = Runtime.getRuntime().freeMemory()

            val raw = (totalMemory - freeMemory) / (1024 * 1024).toDouble()

            return (raw * 100).roundToInt().toDouble() / 100
        }
    }
}
