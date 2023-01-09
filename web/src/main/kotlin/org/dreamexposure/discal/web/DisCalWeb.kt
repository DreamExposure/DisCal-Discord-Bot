package org.dreamexposure.discal.web

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy
import kotlin.system.exitProcess

@Component
class DisCalWeb {
    @PreDestroy
    fun onShutdown() {
        LOGGER.info(STATUS, "Website shutting down")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.init()

            //Start up spring
            try {
                SpringApplicationBuilder(Application::class.java)
                        .build()
                        .run(*args)
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "`Spring error` by PANIC! at the Website", e)
                exitProcess(4)
            }

            LOGGER.info(STATUS, "Website is now online")
        }
    }
}
