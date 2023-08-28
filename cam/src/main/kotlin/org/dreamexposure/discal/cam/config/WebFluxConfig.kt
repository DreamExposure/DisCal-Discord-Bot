package org.dreamexposure.discal.cam.config

import org.dreamexposure.discal.core.utils.GlobalVal
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebFluxConfig: WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val codecs = configurer.defaultCodecs()
        codecs.kotlinSerializationJsonDecoder(KotlinSerializationJsonDecoder(GlobalVal.JSON_FORMAT))
        codecs.kotlinSerializationJsonEncoder(KotlinSerializationJsonEncoder(GlobalVal.JSON_FORMAT))
    }
}
