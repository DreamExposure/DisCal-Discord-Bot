package org.dreamexposure.discal.client.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import discord4j.common.JacksonResources
import org.dreamexposure.discal.core.serializers.SnowflakeMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

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
}