package org.dreamexposure.discal.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import discord4j.common.JacksonResources
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor
import io.micrometer.observation.ObservationRegistry
import okhttp3.OkHttpClient
import org.dreamexposure.discal.core.serializers.SnowflakeMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping

@Configuration
class BeanConfig {
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        // Use d4j's object mapper
        return JacksonResources.create().objectMapper
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .registerModule(SnowflakeMapper())
    }

    @Bean
    fun handlerMapping(): RequestMappingHandlerMapping {
        return RequestMappingHandlerMapping()
    }

    @Bean
    fun httpClient(registry: ObservationRegistry): OkHttpClient {
        val interceptor = OkHttpObservationInterceptor.builder(registry, "okhttp.requests")
            // This can lead to tag cardinality explosion as it doesn't use uri patterns, should investigate options for that one day
            .uriMapper { it.url.encodedPath }
            .build()

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }
}
