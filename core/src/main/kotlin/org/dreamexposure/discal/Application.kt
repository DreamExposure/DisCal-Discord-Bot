package org.dreamexposure.discal

import org.dreamexposure.discal.core.`object`.BotSettings
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration

@SpringBootApplication(exclude = [SessionAutoConfiguration::class, R2dbcAutoConfiguration::class])
class Application {
    companion object {
        @JvmStatic
        fun getShardIndex(): String {
            /*
            This fucking sucks. So k8s doesn't expose the pod ordinal for a pod in a stateful set
            https://github.com/kubernetes/kubernetes/pull/68719
            This has been an open issue and PR for over 3 years now, and has gone stale as of March 3rd 2021.
            So, in order to get the pod ordinal since its not directly exposed, we have to get the hostname, and parse
            the ordinal out of that.
            To make sure we don't use this when running anywhere but inside of k8s, we are mapping the hostname to an env
            variable SHARD_POD_NAME and if that is present, parsing it for the pod ordinal to tell the bot its shard index.
            This will be removed when/if they add this feature directly and SHARD_INDEX will be an env variable...
            */

            //Check if we are running in k8s or not...
            val shardPodName = System.getenv("SHARD_POD_NAME")
            return if (shardPodName != null) {
                //In k8s, parse this shit
                val parts = shardPodName.split("-")
                parts[parts.size -1]
            } else {
                //Fall back to config value
                BotSettings.SHARD_INDEX.get()
            }
        }

        @JvmStatic
        fun getShardCount(): Int {
            val shardCount = System.getenv("SHARD_COUNT")
            return shardCount?.toInt() ?: //Fall back to config
            BotSettings.SHARD_COUNT.get().toInt()
        }
    }
}
