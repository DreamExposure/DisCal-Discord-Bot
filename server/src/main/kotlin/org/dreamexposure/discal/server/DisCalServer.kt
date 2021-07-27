package org.dreamexposure.discal.server

import org.dreamexposure.discal.Application
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.`object`.network.discal.NetworkInfo
import org.dreamexposure.discal.core.database.DatabaseManager
import org.dreamexposure.discal.core.logger.LogFeed
import org.dreamexposure.discal.core.logger.`object`.LogObject
import org.dreamexposure.discal.server.network.google.GoogleInternalAuthHandler
import org.dreamexposure.discal.server.utils.Authentication
import org.dreamexposure.novautils.database.DatabaseSettings
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.exception.FlywayValidateException
import org.flywaydb.core.internal.command.DbMigrate
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.system.ApplicationPid
import org.springframework.stereotype.Component
import java.io.FileReader
import java.util.*
import javax.annotation.PreDestroy
import kotlin.system.exitProcess
import org.dreamexposure.novautils.database.DatabaseManager as NovaUtilsDatabaseManager


@Component
class DisCalServer(val networkInfo: NetworkInfo) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        //Handle the rest of the bullshit
        Authentication.init()

        //Save pid
        networkInfo.pid = ApplicationPid().toString()
        LogFeed.log(LogObject.forStatus("Started Server/API", "Server and API are now online!"))
    }

    @PreDestroy
    fun onShutdown() {
        LogFeed.log(LogObject.forStatus("API shutting down", "Server/API shutting down..."))
        Authentication.shutdown()
        DatabaseManager.disconnectFromMySQL()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            //Get settings
            val p = Properties()
            p.load(FileReader("settings.properties"))
            BotSettings.init(p)

            //Handle database migrations
            handleMigrations(args.isNotEmpty() && args[0].equals("--repair", true))

            //Handle generating new google auth credentials for discal accounts
            if (args.size > 1 && args[0].equals("-forceNewAuth", true)) {
                //This will automatically kill this instance once finished
                GoogleInternalAuthHandler.requestCode(args[1].toInt()).subscribe()
            }

            //Start up spring
            try {
                SpringApplicationBuilder(Application::class.java)
                        .profiles(BotSettings.PROFILE.get())
                        .build()
                        .run(*args)
            } catch (e: Exception) {
                e.printStackTrace()
                LogFeed.log(LogObject.forException("Spring error", "by 'PANIC! AT THE API'", e, DisCalServer::class.java))
                exitProcess(4)
            }
        }
    }
}

private fun handleMigrations(repair: Boolean) {
    val placeholders: Map<String, String> = mapOf(Pair("prefix", BotSettings.SQL_PREFIX.get()))

    val databaseSettings = DatabaseSettings(
            BotSettings.SQL_HOST.get(),
            BotSettings.SQL_PORT.get(),
            BotSettings.SQL_DB.get(),
            BotSettings.SQL_USER.get(),
            BotSettings.SQL_PASS.get(),
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
