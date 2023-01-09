package org.dreamexposure.discal.server.network.discal

import com.zaxxer.hikari.HikariDataSource
import org.dreamexposure.discal.core.config.Config.*
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
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
        val placeholders: Map<String, String> = mapOf(Pair("prefix", SQL_PREFIX.getString()))
        try {
            val source = HikariDataSource()
            source.jdbcUrl = "jdbc:mysql://${SQL_HOST.getString()}:${SQL_PORT.getString()}/${SQL_DB.getString()}"
            source.username = SQL_USER.getString()
            source.password = SQL_PASS.getString()

            val flyway = Flyway.configure()
                .dataSource(source)
                .cleanDisabled(true)
                .baselineOnMigrate(true)
                .table("${SQL_PREFIX.getString()}schema_history")
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

            source.close()
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
