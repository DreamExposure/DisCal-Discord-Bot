package org.dreamexposure.discal.client.conf

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.dreamexposure.discal.core.`object`.BotSettings
import org.springframework.boot.web.server.ConfigurableWebServerFactory
import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebFluxConfig : WebFluxConfigurer, WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    override fun customize(factory: ConfigurableWebServerFactory?) {
        factory?.setPort(BotSettings.PORT.get().toInt())
        factory?.addErrorPages(ErrorPage(HttpStatus.NOT_FOUND, "/"))
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
    }

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val rsc = RedisStandaloneConfiguration()
        rsc.hostName = BotSettings.REDIS_HOSTNAME.get()
        rsc.port = BotSettings.REDIS_PORT.get().toInt()
        rsc.password = RedisPassword.of(BotSettings.REDIS_PASSWORD.get())

        return LettuceConnectionFactory(rsc)
    }

    @Bean
    fun mysqlMasterConnectionFactory(): ConnectionFactory {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, "mysql")
                .option(ConnectionFactoryOptions.HOST, BotSettings.SQL_MASTER_HOST.get())
                .option(ConnectionFactoryOptions.PORT, BotSettings.SQL_MASTER_PORT.get().toInt())
                .option(ConnectionFactoryOptions.USER, BotSettings.SQL_MASTER_USER.get())
                .option(ConnectionFactoryOptions.PASSWORD, BotSettings.SQL_MASTER_PASS.get())
                .option(ConnectionFactoryOptions.DATABASE, BotSettings.SQL_DB.get())
                .option(ConnectionFactoryOptions.SSL, false)
                .build())
    }

    @Bean
    fun mysqlSlaveConnectionFactory(): ConnectionFactory {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, "mysql")
                .option(ConnectionFactoryOptions.HOST, BotSettings.SQL_SLAVE_HOST.get())
                .option(ConnectionFactoryOptions.PORT, BotSettings.SQL_SLAVE_PORT.get().toInt())
                .option(ConnectionFactoryOptions.USER, BotSettings.SQL_SLAVE_USER.get())
                .option(ConnectionFactoryOptions.PASSWORD, BotSettings.SQL_SLAVE_PASS.get())
                .option(ConnectionFactoryOptions.DATABASE, BotSettings.SQL_DB.get())
                .option(ConnectionFactoryOptions.SSL, false)
                .build())
    }
}
