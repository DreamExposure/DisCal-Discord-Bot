package org.dreamexposure.discal.web

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.network.google.Authorization
import org.dreamexposure.discal.web.handler.DiscordAccountHandler
import org.springframework.boot.builder.SpringApplicationBuilder
import java.io.FileReader
import java.util.*
import javax.annotation.PreDestroy
import kotlin.system.exitProcess

//@Component
class DisCalWeb {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //Get settings
            val p = Properties()
            p.load(FileReader("settings.properties"))
            BotSettings.init(p)

            //Start Google auth daemon
            Authorization.getAuth().init()

            //Start up spring
            try {
                DiscordAccountHandler.init()
                SpringApplicationBuilder(Application::class.java)
                        .properties("spring.config.name:web")
                        .profiles(BotSettings.PROFILE.get())
                        .build()
                        .run(*args)
            } catch (e: Exception) {
                e.printStackTrace()
                LogFeed.log(LogObject.forException("Spring error", "by 'PANIC! AT THE WEBSITE'", e, DisCalWeb::class.java))
                exitProcess(4)
            }

            LogFeed.log(LogObject.forStatus("Started", "Website is now online!"))
        }
    }

    @PreDestroy
    fun onShutdown() {
        LogFeed.log(LogObject.forStatus("Website shutting down", "Website shutting down..."))
        DiscordAccountHandler.shutdown()
    }
}
