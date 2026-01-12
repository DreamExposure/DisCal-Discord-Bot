package org.dreamexposure.discal.server.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/*
So... at some point or another it seems flyway just... stopped running? I don't know, it was fully autowired with spring
had the right configs and everything, it was great. But then one day when I went and wrote a migration... it never ran.
Even on verbose logging, there's no flyway errors, no mentions of bad configs or missing dependencies, nothing. So I am
attempting to configure flyway programmatically and hoping it actually starts running migrations again.
 */

@Configuration
class FlywayConfig(
    @Value($$"${spring.flyway.url}")
    private val databaseUrl: String,
    @Value($$"${spring.flyway.user}")
    private val user: String,
    @Value($$"${spring.flyway.password}")
    private val password: String,
    @Value($$"${spring.flyway.table}")
    private val schemaHistoryTable: String,
    @Value($$"${spring.flyway.placeholders.prefix}")
    private val placeholderPrefix: String,
    @Value($$"${spring.flyway.baseline-on-migrate}")
    private val baselineOnMigrate: Boolean,
    @Value($$"${spring.flyway.clean-on-validation-error}")
    private val cleanOnValidationError: Boolean,
) {

    @Bean(initMethod = "migrate")
    fun migrate(): Flyway {
        return Flyway.configure()
            .dataSource(databaseUrl, user, password)
            .locations("db/migration")
            .table(schemaHistoryTable)
            .placeholders(mapOf("prefix" to placeholderPrefix))
            .baselineOnMigrate(baselineOnMigrate)
            .cleanOnValidationError(cleanOnValidationError)
            .executeInTransaction(true)
            .load()
    }
}