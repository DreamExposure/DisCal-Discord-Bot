package org.dreamexposure.discal.server.conf;

import org.dreamexposure.discal.core.object.BotSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@EnableRedisHttpSession
public class LettuceConfig {
	@Bean
	public LettuceConnectionFactory lettuceConnectionFactory() {
		final LettuceConnectionFactory factory = new LettuceConnectionFactory();

		factory.setHostName(BotSettings.REDIS_HOSTNAME.get());
		factory.setPort(Integer.valueOf(BotSettings.REDIS_PORT.get()));
		factory.setPassword(BotSettings.REDIS_PASSWORD.get());

		return factory;
	}
}
