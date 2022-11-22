package org.dreamexposure.discal.client.config

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.dreamexposure.discal.core.`object`.BotSettings
import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.boot.web.server.ConfigurableWebServerFactory
import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebFluxConfig : WebFluxConfigurer, WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    override fun customize(factory: ConfigurableWebServerFactory?) {
        factory?.addErrorPages(ErrorPage(HttpStatus.NOT_FOUND, "/"))
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
    }

    @Bean(name = ["mysqlDatasource"])
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

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val codecs = configurer.defaultCodecs()
        codecs.kotlinSerializationJsonDecoder(KotlinSerializationJsonDecoder(GlobalVal.JSON_FORMAT))
        codecs.kotlinSerializationJsonEncoder(KotlinSerializationJsonEncoder(GlobalVal.JSON_FORMAT))
    }
}
