package org.dreamexposure.discal.client

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.client.message.Messages
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component
import java.io.FileReader
import java.util.*
import kotlin.system.exitProcess

@Component
class DisCalClient {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //Get settings
            val p = Properties()
            p.load(FileReader("application.properties"))
            BotSettings.init(p)

            //Load lang files
            Messages.reloadLangs().subscribe()

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

