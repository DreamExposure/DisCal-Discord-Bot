package com.cloudcraftgaming.discal.conf;

import com.cloudcraftgaming.discal.api.object.BotSettings;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
@EnableAutoConfiguration
public class ServletConfig {
	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return (container -> {
			container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/"));
			container.setPort(Integer.valueOf(BotSettings.PORT.get()));
		});
	}
}