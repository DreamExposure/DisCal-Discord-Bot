package org.dreamexposure.discal.cam

import jakarta.annotation.PreDestroy
import org.dreamexposure.discal.Application
import org.dreamexposure.discal.cam.google.GoogleInternalAuthHandler
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class Cam {

    @PreDestroy
    fun onShutdown() {
        LOGGER.info(GlobalVal.STATUS, "CAM shutting down.")
        DatabaseManager.disconnectFromMySQL()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Config.init()

            //Handle generating new google auth credentials for discal accounts
            if (args.size > 1 && args[0].equals("-forceNewGoogleAuth", true)) {
                //This will automatically kill this instance once finished
                GoogleInternalAuthHandler.requestCode(args[1].toInt()).subscribe()
            }

            //Start up spring
            try {
                SpringApplicationBuilder(Application::class.java)
                        .build()
                        .run(*args)
                LOGGER.info(GlobalVal.STATUS, "CAM is now online")
            } catch (e: Exception) {
                LOGGER.error(GlobalVal.DEFAULT, "'Spring error' by PANIC! at the CAM")
                exitProcess(4)
            }
        }
    }
}
