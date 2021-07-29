package org.dreamexposure.discal.web

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import org.dreamexposure.discal.web.handler.DiscordAccountHandler
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component
import java.io.FileReader
import java.util.*
import javax.annotation.PreDestroy
import kotlin.system.exitProcess

@Component
class DisCalWeb {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //Get settings
            val p = Properties()
            p.load(FileReader("settings.properties"))
            BotSettings.init(p)

            //Start up spring
            try {
                DiscordAccountHandler.init()
                SpringApplicationBuilder(Application::class.java)
                        .profiles(BotSettings.PROFILE.get())
                        .build()
                        .run(*args)
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "`Spring error` by PANIC! at the Website", e)
                exitProcess(4)
            }

            LOGGER.info(STATUS, "Website is not online")
        }
    }

    @PreDestroy
    fun onShutdown() {
        LOGGER.info(STATUS, "Website shutting down")
        DiscordAccountHandler.shutdown()
    }
}
