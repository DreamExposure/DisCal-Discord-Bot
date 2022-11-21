package org.dreamexposure.discal.server

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.discal.core.utils.GlobalVal.STATUS
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component
import java.io.FileReader
import java.util.*
import javax.annotation.PreDestroy
import kotlin.system.exitProcess


@Component
class DisCalServer {

    @PreDestroy
    fun onShutdown() {
        LOGGER.info(STATUS, "API shutting down.")
        DatabaseManager.disconnectFromMySQL()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //Get settings
            val p = Properties()
            p.load(FileReader("application.properties"))
            BotSettings.init(p)

            //Start up spring
            try {
                SpringApplicationBuilder(Application::class.java)
                        .build()
                        .run(*args)
                LOGGER.info(STATUS, "API is now online")
            } catch (e: Exception) {
                LOGGER.error(DEFAULT, "'Spring error' by PANIC! at the API")
                exitProcess(4)
            }
        }
    }
}
