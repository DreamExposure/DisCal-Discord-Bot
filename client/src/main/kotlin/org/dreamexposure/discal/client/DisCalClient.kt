package org.dreamexposure.discal.client

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class DisCalClient {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.init()

            //Start Spring
            try {
                SpringApplicationBuilder(Application::class.java).run(*args)
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "Spring error!", e)
                exitProcess(4)
            }
        }
    }
}

