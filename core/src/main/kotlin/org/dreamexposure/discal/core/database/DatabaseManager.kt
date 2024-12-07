@file:Suppress("DuplicatedCode")

package org.dreamexposure.discal.core.database

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.logger.LOGGER
import org.dreamexposure.discal.core.`object`.web.UserAPIAccount
import org.dreamexposure.discal.core.utils.GlobalVal.DEFAULT
import org.intellij.lang.annotations.Language
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.util.function.Function

object DatabaseManager {
    private val pool: ConnectionPool

    init {
        val factory = ConnectionFactories.get(
            builder()
                .option(DRIVER, "pool")
                .option(PROTOCOL, "mysql")
                .from(parse(Config.SQL_URL.getString()))
                .option(USER, Config.SQL_USERNAME.getString())
                .option(PASSWORD, Config.SQL_PASSWORD.getString())
                .build()
        )

        val conf = ConnectionPoolConfiguration.builder()
            .connectionFactory(factory)
            .maxLifeTime(Duration.ofHours(1))
            .build()

        pool = ConnectionPool(conf)
    }

    //FIXME: attempt to fix constant open/close of connections
    private fun <T> connect(connection: Function<Connection, Mono<T>>): Mono<T> {
        return Mono.usingWhen(pool.create(), connection::apply, Connection::close)
    }

    fun disconnectFromMySQL() = pool.dispose()

    fun getAPIAccount(APIKey: String): Mono<UserAPIAccount> {
        return connect { c ->
            Mono.from(
                c.createStatement(Queries.SELECT_API_KEY)
                    .bind(0, APIKey)
                    .execute()
            ).flatMapMany { res ->
                res.map { row, _ ->
                    UserAPIAccount(
                        row["USER_ID", String::class.java]!!,
                        APIKey,
                        row["BLOCKED", Boolean::class.java]!!,
                        row["TIME_ISSUED", Long::class.java]!!
                    )
                }
            }.next().retryWhen(Retry.max(3)
                .filter(IllegalStateException::class::isInstance)
                .filter { it.message != null && it.message!!.contains("Request queue was disposed") }
            ).doOnError {
                LOGGER.error(DEFAULT, "Failed to get API-key data", it)
            }.onErrorResume { Mono.empty() }
        }
    }
}

private object Queries {
    @Language("MySQL")
    val SELECT_API_KEY = """SELECT * FROM ${Tables.API}
        WHERE API_KEY = ?
        """.trimMargin()
}

private object Tables {
    /* The language annotations are there because IntelliJ is dumb and assumes this needs to be proper MySQL */

    @Language("Kotlin")
    const val API: String = "api"
}
