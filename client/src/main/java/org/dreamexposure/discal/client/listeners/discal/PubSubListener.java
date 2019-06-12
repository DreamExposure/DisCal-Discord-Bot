package org.dreamexposure.discal.client.listeners.discal;

import com.google.common.eventbus.Subscribe;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.client.utils.GuildFinder;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.enums.network.PubSubReason;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.novautils.events.network.pubsub.PubSubReceiveEvent;

import java.util.Optional;

/**
 * @author NovaFox161
 * Date Created: 9/8/2018
 * For Project: DisCal-Discord-Bot
 * Author Website: https://www.novamaday.com
 * Company Website: https://www.dreamexposure.org
 * Contact: nova@dreamexposure.org
 */
@SuppressWarnings({"UnstableApiUsage", "OptionalGetWithoutIsPresent"})
public class PubSubListener {
	@Subscribe
	public void handle(PubSubReceiveEvent event) {
		Optional<Guild> g = Optional.empty();
		//Check if this even applies to us!
		if (event.getData().has("Guild-Id")) {
			g = GuildFinder.findGuild(Snowflake.of(event.getData().getString("Guild-Id")));
			if (!g.isPresent()) return; //Guild not connected to this client, correct client will handle this.
		}

		PubSubReason reason = PubSubReason.valueOf(event.getData().getString("Reason"));
		DisCalRealm realm = DisCalRealm.valueOf(event.getData().getString("Realm"));

		if (event.getChannelName().equalsIgnoreCase("DisCal/ToClient/All")) {
			switch (reason) {
				case UPDATE:
					if (realm.equals(DisCalRealm.BOT_SETTINGS)) {
						if (event.getData().has("Bot-Nick"))
							g.get().changeSelfNickname(event.getData().getString("Bot-Nick")).subscribe();
					}
					break;
				case HANDLE:
					if (realm.equals(DisCalRealm.BOT_LANGS)) {
						//Reload the lang files as they could have changed.
						MessageManager.reloadLangs();
					} else if (realm.equals(DisCalRealm.BOT_INVALIDATE_CACHES)) {
						//Invalidate the caches, such as the database cache.
						DatabaseManager.getManager().clearCache();
					} else if (realm.equals(DisCalRealm.GUILD_LEAVE)) {
						//Leave guild as requested.
						g.get().leave().subscribe();
					} else if (realm.equals(DisCalRealm.GUILD_MAX_CALENDARS)) {
						//Change the max calendar settings for this guild...
						GuildSettings settings = DatabaseManager.getManager().getSettings(g.get().getId());

						settings.setMaxCalendars(event.getData().getInt("Max-Calendars"));
						DatabaseManager.getManager().updateSettings(settings);
					} else if (realm.equals(DisCalRealm.GUILD_IS_DEV)) {
						//Change if the guild is a dev guild or not.
						GuildSettings settings = DatabaseManager.getManager().getSettings(g.get().getId());

						settings.setDevGuild(!settings.isDevGuild());
						DatabaseManager.getManager().updateSettings(settings);
					} else if (realm.equals(DisCalRealm.GUILD_IS_PATRON)) {
						//Change if the guild is a patron guild or not.
						GuildSettings settings = DatabaseManager.getManager().getSettings(g.get().getId());

						settings.setPatronGuild(!settings.isPatronGuild());
						DatabaseManager.getManager().updateSettings(settings);
					}
					break;
				default:
					//Action not supported in this channel.
					Logger.getLogger().debug("Received unsupported action in NoResponse Channel", true);
					break;

			}
		}
	}
}