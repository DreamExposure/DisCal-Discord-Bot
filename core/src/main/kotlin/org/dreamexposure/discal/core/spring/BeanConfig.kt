package org.dreamexposure.discal.core.spring

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
