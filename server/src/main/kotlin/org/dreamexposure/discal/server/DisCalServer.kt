package org.dreamexposure.discal.server

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.NetworkInfo
import org.dreamexposure.discal.core.calendar.CalendarAuth
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.core.network.google.Authorization
import org.dreamexposure.discal.server.network.dbotsgg.UpdateDBotsData
import org.dreamexposure.discal.server.network.discal.NetworkMediator
import org.dreamexposure.discal.server.network.topgg.UpdateTopStats
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.novautils.database.DatabaseSettings
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.exception.FlywayValidateException
import org.flywaydb.core.internal.command.DbMigrate
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import org.springframework.boot.system.ApplicationPid
import java.io.FileReader
import java.util.*
import javax.annotation.PreDestroy
import kotlin.system.exitProcess
import org.dreamexposure.novautils.database.DatabaseManager as NovaUtilsDatabaseManager

@SpringBootApplication(exclude = [SessionAutoConfiguration::class])
class DisCalServer {
    companion object {
        val networkInfo = NetworkInfo()

        lateinit var client: DiscordClient
            internal set

        @JvmStatic
        fun main(args: Array<String>) {
            //Get settings
            val p = Properties()
            p.load(FileReader("settings.properties"))
            BotSettings.init(p)

            //Handle generating new google auth credentials for discal accounts
            if (args.size > 1 && args[0].equals("-forceNewAuth", true)) {
                CalendarAuth.getCalendarService(args[1].toInt()).block() //Block until auth completes

                //Kill the running instance as this is only meant for generating new credentials, IllegalState
                exitProcess(100)
            }

            //Handle database migrations
            handleMigrations(args.isNotEmpty() && args[0].equals("--repair", true))

            //Start Google Authorization daemon
            Authorization.getAuth().init()

            client = DiscordClientBuilder.create(BotSettings.TOKEN.get()).build()

            //Start up spring
            try {
                val app = SpringApplication(DisCalServer::class.java)
                app.setAdditionalProfiles(BotSettings.PROFILE.get())
                app.run(*args)
            } catch (e: Exception) {
                e.printStackTrace()
                LogFeed.log(LogObject.forException("Spring error", "by 'PANIC! AT THE API'", e, DisCalServer::class.java))
                exitProcess(4)
            }

            //Start network monitoring
            NetworkMediator.init()

            //Handle the rest of the bullshit
            UpdateTopStats.init()
            UpdateDBotsData.init()
            Authentication.init()

            //Save pid
            networkInfo.pid = ApplicationPid().toString()

            LogFeed.log(LogObject.forStatus("Started Server/API", "Server and API are now online!"))
        }
    }

    @PreDestroy
    fun onShutdown() {
        LogFeed.log(LogObject.forStatus("API shutting down", "Server/API shutting down..."))
        Authentication.shutdown()
        NetworkMediator.shutdown()
        UpdateTopStats.shutdown()
        UpdateDBotsData.shutdown()
        DatabaseManager.disconnectFromMySQL()
    }
}

private fun handleMigrations(repair: Boolean) {
    val placeholders: Map<String, String> = mapOf(Pair("prefix", BotSettings.SQL_PREFIX.get()))

    val databaseSettings = DatabaseSettings(
            BotSettings.SQL_MASTER_HOST.get(),
            BotSettings.SQL_MASTER_PORT.get(),
            BotSettings.SQL_DB.get(),
            BotSettings.SQL_MASTER_USER.get(),
            BotSettings.SQL_MASTER_PASS.get(),
            BotSettings.SQL_PREFIX.get(),
    )
    val databaseInfo = NovaUtilsDatabaseManager.connectToMySQL(databaseSettings)

    try {
        val flyway = Flyway.configure()
                .dataSource(databaseInfo.source)
                .cleanDisabled(true)
                .baselineOnMigrate(true)
                .table("${BotSettings.SQL_PREFIX.get()}schema_history")
                .placeholders(placeholders)
                .load()

        //Validate?
        flyway.validateWithResult().invalidMigrations.forEach { result ->
            println("Invalid Migration: ${result.filepath}")
            println("Version: ${result.version}")
            println("Description: ${result.description}")
            println("Details: ${result.errorDetails.errorMessage}")
        }

        var sm = 0
        if (repair)
            flyway.repair()
        else
            sm = flyway.migrate().migrationsExecuted

        NovaUtilsDatabaseManager.disconnectFromMySQL(databaseInfo)
        LogFeed.log(LogObject.forDebug("Migrations Successful", "$sm migrations applied!"))
    } catch (e: FlywayValidateException) {
        LogFeed.log(LogObject.forException("Migrations failure (validate)", e, DisCalServer::class.java))
        e.printStackTrace()
        println("Migration Validate Error Message: ${e.errorCode}")
        exitProcess(3)
    } catch (e: DbMigrate.FlywayMigrateException) {
        LogFeed.log(LogObject.forException("Migrations failure", e, DisCalServer::class.java))
        e.printStackTrace()
        println("Migration Error Message: ${e.migration.validate().errorMessage}")
        exitProcess(3)
    } catch (e: Exception) {
        LogFeed.log(LogObject.forException("Migrations failure", e, DisCalServer::class.java))
        e.printStackTrace()
        exitProcess(2)
    }
}
