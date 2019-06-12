package org.dreamexposure.discal.client.conf;

import org.dreamexposure.discal.core.object.BotSettings;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
@EnableAutoConfiguration
public class ServletConfig implements
	WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

	public void customize(ConfigurableServletWebServerFactory factory) {
		factory.setPort(Integer.valueOf(BotSettings.PORT.get()));
		factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/"));
	}
}