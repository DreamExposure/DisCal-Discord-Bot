package com.cloudcraftgaming.discal.conf;

import com.cloudcraftgaming.discal.api.object.BotSettings;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class ServletConfig {
	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
		return (container -> container.setPort(Integer.valueOf(BotSettings.PORT.get())));
	}
}