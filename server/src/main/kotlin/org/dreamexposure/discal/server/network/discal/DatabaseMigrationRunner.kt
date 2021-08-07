package org.dreamexposure.discal.server.network.discal

import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.dreamexposure.novautils.database.DatabaseManager
import org.dreamexposure.novautils.database.DatabaseSettings
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.exception.FlywayValidateException
import org.flywaydb.core.internal.command.DbMigrate
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class DatabaseMigrationRunner : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val placeholders: Map<String, String> = mapOf(Pair("prefix", BotSettings.SQL_PREFIX.get()))

        val databaseSettings = DatabaseSettings(
              BotSettings.SQL_HOST.get(),
              BotSettings.SQL_PORT.get(),
              BotSettings.SQL_DB.get(),
              BotSettings.SQL_USER.get(),
              BotSettings.SQL_PASS.get(),
              BotSettings.SQL_PREFIX.get(),
        )
        val databaseInfo = DatabaseManager.connectToMySQL(databaseSettings)

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
                LOGGER.warn("Invalid Migration: ${result.filepath}")
                LOGGER.debug("Version: ${result.version}")
                LOGGER.debug("Description: ${result.description}")
                LOGGER.debug("Details: ${result.errorDetails.errorMessage}")
            }

            var sm = 0
            if (args!!.containsOption("--repair"))
                flyway.repair()
            else
                sm = flyway.migrate().migrationsExecuted

            DatabaseManager.disconnectFromMySQL(databaseInfo)
            LOGGER.info(DEFAULT, "Migrations successful | $sm migrations applied!")
        } catch (e: FlywayValidateException) {
            LOGGER.error(DEFAULT, "Migrations failure (validate)", e)
            exitProcess(3)
        } catch (e: DbMigrate.FlywayMigrateException) {
            LOGGER.error(DEFAULT, "Migrations failure", e)
            exitProcess(3)
        } catch (e: Exception) {
            LOGGER.error(DEFAULT, "Migrations failures", e)
            exitProcess(2)
        }
    }
}
