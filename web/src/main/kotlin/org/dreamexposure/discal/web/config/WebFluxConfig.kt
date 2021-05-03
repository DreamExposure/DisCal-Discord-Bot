package org.dreamexposure.discal.web.config

import org.dreamexposure.discal.core.`object`.BotSettings
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.web.server.ConfigurableWebServerFactory
import org.springframework.boot.web.server.ErrorPage
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ViewResolverRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.thymeleaf.spring5.ISpringWebFluxTemplateEngine
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver
import org.thymeleaf.templatemode.TemplateMode

@Configuration
@EnableWebFlux
@EnableAutoConfiguration
class WebFluxConfig : WebServerFactoryCustomizer<ConfigurableWebServerFactory>,
        ApplicationContextAware, WebFluxConfigurer {

    private var ctx: ApplicationContext? = null

    override fun setApplicationContext(context: ApplicationContext?) {
        ctx = context
    }

    override fun customize(factory: ConfigurableWebServerFactory?) {
        factory?.setPort(BotSettings.PORT.get().toInt())
        factory?.addErrorPages(
                ErrorPage(HttpStatus.BAD_REQUEST, "/400"),
                ErrorPage(HttpStatus.NOT_FOUND, "/404"),
                ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500"),
        )
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
    fun staticResourceRouter(): RouterFunction<ServerResponse> {
        return RouterFunctions.resources("/**", ClassPathResource("static/"))
    }

    @Bean
    fun thymeleafTemplateResolver(): SpringResourceTemplateResolver {
        val res = SpringResourceTemplateResolver()

        res.setApplicationContext(this.ctx!!)
        res.prefix = "classpath:/templates/"
        res.suffix = ".html"
        res.templateMode = TemplateMode.HTML
        res.isCacheable = false
        res.checkExistence = false

        return res
    }

    @Bean
    fun thymeleafTemplateEngine(): ISpringWebFluxTemplateEngine {
        val templateEngine = SpringWebFluxTemplateEngine()

        templateEngine.addTemplateResolver(thymeleafTemplateResolver())

        return templateEngine
    }

    @Bean
    fun thymeleafChunkedAndDataDrivenResolver(): ThymeleafReactiveViewResolver {
        val viewResolver = ThymeleafReactiveViewResolver()

        viewResolver.templateEngine = thymeleafTemplateEngine()
        viewResolver.responseMaxChunkSizeBytes = 8192

        return viewResolver
    }

    override fun configureViewResolvers(registry: ViewResolverRegistry) {
        registry.viewResolver(thymeleafChunkedAndDataDrivenResolver())
    }
}
