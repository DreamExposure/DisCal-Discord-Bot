package org.dreamexposure.discal.client.listeners.discord;

import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.core.logger.LogFeed;
import org.dreamexposure.discal.core.logger.object.LogObject;
import org.dreamexposure.discal.core.utils.GlobalConst;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.rest.util.Image;
import reactor.core.publisher.Mono;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings({"OptionalGetWithoutIsPresent", "ConstantConditions"})
public class ReadyEventListener {
	public static Mono<Void> handle(ReadyEvent event) {
		return event.getClient().getApplicationInfo()
				.doOnNext(info -> GlobalConst.iconUrl = info.getIconUrl(Image.Format.PNG).get())
				.doOnNext(info -> MessageManager.reloadLangs())
				.doOnNext(info ->
						LogFeed.log(LogObject.forDebug("[ReadyEvent]",
								"Connection success! Session ID: " + event.getSessionId()))
				)
				.doOnNext(info -> LogFeed.log(LogObject.forStatus("Ready Event Success!")))
				.onErrorResume(e -> {
					LogFeed.log(LogObject.forException("BAD!!!!!1!!", e, ReadyEventListener.class));
					return Mono.empty();
				})
				.then();
	}
}