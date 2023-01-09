package org.dreamexposure.discal.server.config

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.dreamexposure.discal.core.config.Config
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
class WebFluxConfig : WebServerFactoryCustomizer<ConfigurableWebServerFactory>, WebFluxConfigurer {

    override fun customize(factory: ConfigurableWebServerFactory?) {
        factory?.addErrorPages(ErrorPage(HttpStatus.NOT_FOUND, "/"))
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .maxAge(600)
    }

    @Bean(name = ["mysqlDatasource"])
    fun mysqlConnectionFactory(): ConnectionFactory {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "pool")
            .option(ConnectionFactoryOptions.PROTOCOL, "mysql")
            .option(ConnectionFactoryOptions.HOST, Config.SQL_HOST.getString())
            .option(ConnectionFactoryOptions.PORT, Config.SQL_PORT.getInt())
            .option(ConnectionFactoryOptions.USER, Config.SQL_USER.getString())
            .option(ConnectionFactoryOptions.PASSWORD, Config.SQL_PASS.getString())
            .option(ConnectionFactoryOptions.DATABASE, Config.SQL_DB.getString())
                .build())
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val codecs = configurer.defaultCodecs()
        codecs.kotlinSerializationJsonDecoder(KotlinSerializationJsonDecoder(GlobalVal.JSON_FORMAT))
        codecs.kotlinSerializationJsonEncoder(KotlinSerializationJsonEncoder(GlobalVal.JSON_FORMAT))
    }
}
