package org.dreamexposure.discal.core.config

import io.r2dbc.spi.ConnectionFactory
import org.dreamexposure.discal.core.database.InstantConverters
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration

@Configuration
class R2dbcConfiguration(
    private val connectionFactory: ConnectionFactory,
) : AbstractR2dbcConfiguration() {
    override fun connectionFactory(): ConnectionFactory? = connectionFactory

    /*
    Custom converter is currently needed for mariadb driver to support Instants
    https://github.com/mariadb-corporation/mariadb-connector-r2dbc/issues/90

    Credit to @JohnNiang for the code https://github.com/mariadb-corporation/mariadb-connector-r2dbc/issues/90#issuecomment-3731589316
    */
    override fun getCustomConverters(): List<Any?>? {
        val converters = ArrayList<Any?>()
        converters.add(InstantConverters.MariaDBReadingConverter.INSTANCE)
        converters.add(InstantConverters.MariaDBWritingConverter.INSTANCE)
        return converters
    }
}