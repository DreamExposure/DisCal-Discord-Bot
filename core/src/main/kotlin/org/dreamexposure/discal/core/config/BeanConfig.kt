package org.dreamexposure.discal.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping

@Configuration
class BeanConfig {

    @Bean
    fun handlerMapping(): RequestMappingHandlerMapping {
        return RequestMappingHandlerMapping()
    }
}
