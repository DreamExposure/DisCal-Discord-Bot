package org.dreamexposure.discal.server.conf

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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.config.EnableWebFlux

@Configuration
@EnableWebFlux
class WebFluxConfig : WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    override fun customize(factory: ConfigurableWebServerFactory?) {
        factory?.addErrorPages(ErrorPage(HttpStatus.NOT_FOUND, "/"))
    }

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val rsc = RedisStandaloneConfiguration()
        rsc.hostName = BotSettings.REDIS_HOSTNAME.get()
        rsc.port = BotSettings.REDIS_PORT.get().toInt()
        if (BotSettings.REDIS_USE_PASSWORD.get().equals("true", true))
            rsc.password = RedisPassword.of(BotSettings.REDIS_PASSWORD.get())

        return LettuceConnectionFactory(rsc)
    }

    @Bean
    fun mysqlConnectionFactory(): ConnectionFactory {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "pool")
                .option(ConnectionFactoryOptions.PROTOCOL, "mysql")
                .option(ConnectionFactoryOptions.HOST, BotSettings.SQL_HOST.get())
                .option(ConnectionFactoryOptions.PORT, BotSettings.SQL_PORT.get().toInt())
                .option(ConnectionFactoryOptions.USER, BotSettings.SQL_USER.get())
                .option(ConnectionFactoryOptions.PASSWORD, BotSettings.SQL_PASS.get())
                .option(ConnectionFactoryOptions.DATABASE, BotSettings.SQL_DB.get())
                .build())
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val config = CorsConfiguration()

        config.maxAge = 8000L
        config.addAllowedOrigin("*")
        config.addAllowedMethod("GET")
        config.addAllowedMethod("POST")
        config.addAllowedHeader("Authorization")

        val source = UrlBasedCorsConfigurationSource()

        source.registerCorsConfiguration("/**", config)

        return CorsWebFilter(source)
    }
}
