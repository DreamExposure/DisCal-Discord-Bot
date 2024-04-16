package org.dreamexposure.discal.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import discord4j.common.JacksonResources
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener
import okhttp3.OkHttpClient
import org.dreamexposure.discal.core.serializers.DurationMapper
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
            .registerModule(DurationMapper())
    }

    @Bean
    fun handlerMapping(): RequestMappingHandlerMapping {
        return RequestMappingHandlerMapping()
    }

    @Bean
    fun httpClient(registry: MeterRegistry): OkHttpClient {
        return OkHttpClient.Builder()
            .eventListener(
                OkHttpMetricsEventListener.builder(registry, "okhttp.requests")
                    // This does allow for cardinality explosion... unsure how I want to deal with that?
                    .uriMapper { it.url.encodedPath }
                    .build()
            ).build()
    }
}
