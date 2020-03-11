package org.dreamexposure.discal.client.network.discal.api.v1;

import org.dreamexposure.discal.client.message.MessageManager;
import org.dreamexposure.discal.client.utils.GuildFinder;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.BotSettings;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.core.utils.GuildUtils;
import org.dreamexposure.discal.core.utils.JsonUtils;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

@SuppressWarnings({"Duplicates", "OptionalGetWithoutIsPresent"})
@RestController
@RequestMapping("/api/v1/com")
public class ComHandler {

	@PostMapping(value = "/website/dashboard/guild", produces = "application/json")
	public String getWebsiteDashboardGuild(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authorization
		if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").equals(BotSettings.BOT_API_TOKEN.get())) {
			//Not authorized....
			response.setContentType("application/json");
			response.setStatus(401);
			return JsonUtils.getJsonResponseMessage("Unauthorized. Only official DisCal network can use this!");
		}

		//Requires us to grab data for guild and return a response containing the WebGuild with needed info
		JSONObject jsonRequest = new JSONObject(requestBody);
		JSONObject jsonResponse = new JSONObject();

		Optional<Guild> guild = GuildFinder.findGuild(Snowflake.of(jsonRequest.getLong("guild_id")));

		if (guild.isPresent()) {
			Member member = guild.get().getMemberById(Snowflake.of(jsonRequest.getLong("member_id"))).block();
			if (member != null) {
				WebGuild wg = new WebGuild().fromGuild(guild.get());
				wg.setDiscalRole(PermissionChecker.hasSufficientRole(guild.get(), member));
				wg.setManageServer(PermissionChecker.hasManageServerRole(member));

				jsonResponse.put("guild", new WebGuild().fromGuild(guild.get()).toJson(false));

				response.setStatus(200);
			} else {
				jsonResponse.put("message", "Member not Found");
				response.setStatus(404);
			}
		} else {
			jsonResponse.put("message", "Guild not Found");
			response.setStatus(404);
		}
		response.setContentType("application/json");
		return jsonResponse.toString();
	}

	@PostMapping(value = "/website/dashboard/defaults", produces = "application/json")
	public String getWebsiteDashboardDefaults(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authorization
		if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").equals(BotSettings.BOT_API_TOKEN.get())) {
			//Not authorized....
			response.setContentType("application/json");
			response.setStatus(401);
			return JsonUtils.getJsonResponseMessage("Unauthorized. Only official DisCal network can use this!");
		}

		//Get guilds the user is in.
		JSONObject jsonRequest = new JSONObject(requestBody);
		JSONObject jsonResponse = new JSONObject();

		Snowflake memId = Snowflake.of(jsonRequest.getLong("member_id"));
		JSONArray guildIds = jsonRequest.getJSONArray("guilds");
		List<Optional<Guild>> guilds = new ArrayList<>();

		for (int i = 0; i < guildIds.length(); i++) {
			guilds.add(GuildFinder.findGuild(Snowflake.of(guildIds.getLong(i))));
		}

		JSONArray webGuilds = new JSONArray();

		for (Optional<Guild> g : guilds) {
			if (g.isPresent()) {
				WebGuild wg = new WebGuild().fromGuild(g.get());
				Member mem = g.get().getMemberById(memId).block();
				if (mem != null) {
					wg.setManageServer(PermissionChecker.hasManageServerRole(mem));
					wg.setDiscalRole(PermissionChecker.hasSufficientRole(g.get(), mem));
				}
				webGuilds.put(wg.toJson(false));
			}
		}

		if (webGuilds.length() > 0) {
			jsonResponse.put("guilds", webGuilds);
			jsonResponse.put("count", webGuilds.length());
			response.setStatus(200);
		} else {
			jsonResponse.put("message", "Success, however, no listed guilds are connected.");
			response.setStatus(204);
		}
		response.setContentType("application/json");
		return jsonResponse.toString();
	}

	@PostMapping(value = "/website/embed/calendar", produces = "application/json")
	public String getWebsiteEmbedCalendar(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authorization
		if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").equals(BotSettings.BOT_API_TOKEN.get())) {
			//Not authorized....
			response.setContentType("application/json");
			response.setStatus(401);
			return JsonUtils.getJsonResponseMessage("Unauthorized. Only official DisCal network can use this!");
		}

		//Get guild for calendar embed on the website.
		JSONObject jsonRequest = new JSONObject(requestBody);
		JSONObject jsonResponse = new JSONObject();

		Optional<Guild> guild = GuildFinder.findGuild(Snowflake.of(jsonRequest.getLong("guild_id")));

		if (guild.isPresent()) {
			jsonResponse.put("guild", new WebGuild().fromGuild(guild.get()).toJson(false));

			response.setStatus(200);
		} else {
			jsonResponse.put("message", "Guild not Found");
			response.setStatus(404);
		}
		response.setContentType("application/json");
		return jsonResponse.toString();
	}

	@PostMapping(value = "/bot/action/update", produces = "application/json")
	public String handleActionUpdate(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authorization
		if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").equals(BotSettings.BOT_API_TOKEN.get())) {
			//Not authorized....
			response.setContentType("application/json");
			response.setStatus(401);
			return JsonUtils.getJsonResponseMessage("Unauthorized. Only official DisCal network can use this!");
		}

		//Handle action request....
		try {
			JSONObject body = new JSONObject(requestBody);

			//Guild ID must be present for this endpoint...
			Snowflake guildId = Snowflake.of(body.getLong("guild_id"));

			//Check if guild exists on shard, if not, just ignore this as we can't process it
			if (GuildUtils.findShard(guildId) == Integer.parseInt(BotSettings.SHARD_INDEX.get())) {
				if (body.has("bot_nick")) {
					GuildFinder.getGuild(guildId)
							.get()
							.changeSelfNickname(body.getString("bot_nick"))
							.onErrorResume(e -> Mono.empty())
							.subscribe();
				}

				//Send success response to close the connection
				response.setContentType("application/json");
				response.setStatus(200);
				return JsonUtils.getJsonResponseMessage("Success");
			} else {
				response.setContentType("application/json");
				response.setStatus(404);
				return JsonUtils.getJsonResponseMessage("Guild not found on this shard!");
			}

		} catch (JSONException e) {
			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[COM-API-v1] Failed to handle bot action update", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Exception");
		}
	}

	@PostMapping(value = "/bot/action/handle", produces = "application/json")
	public String handleActionHandle(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Authorization
		if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").equals(BotSettings.BOT_API_TOKEN.get())) {
			//Not authorized....
			response.setContentType("application/json");
			response.setStatus(401);
			return JsonUtils.getJsonResponseMessage("Unauthorized. Only official DisCal network can use this!");
		}

		//Handle action request....
		try {
			JSONObject body = new JSONObject(requestBody);

			//First, we handle anything that doesn't  require a specific guild to be present
			DisCalRealm realm = DisCalRealm.valueOf(body.getString("realm"));

			if (realm.equals(DisCalRealm.BOT_LANGS)) {
				MessageManager.reloadLangs();
			} else if (realm.equals(DisCalRealm.BOT_INVALIDATE_CACHES)) {
				DatabaseManager.getManager().clearCache();
			} else if (body.has("guild_id")) {
				//Okay, handle everything with guild required...
				Snowflake guildId = Snowflake.of(body.getLong("guild_id"));
				if (GuildUtils.findShard(guildId) == Integer.parseInt(BotSettings.SHARD_INDEX.get())) {
					if (realm.equals(DisCalRealm.GUILD_LEAVE)) {
						GuildFinder.getGuild(guildId)
								.get()
								.leave()
								.onErrorResume(e -> Mono.empty())
								.subscribe();
					} else if (realm.equals(DisCalRealm.GUILD_MAX_CALENDARS)) {
						GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
						settings.setMaxCalendars(body.getInt("max_calendars"));
						DatabaseManager.getManager().updateSettings(settings);
					} else if (realm.equals(DisCalRealm.GUILD_IS_DEV)) {
						GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
						settings.setDevGuild(!settings.isDevGuild());
						DatabaseManager.getManager().updateSettings(settings);
					} else if (realm.equals(DisCalRealm.GUILD_IS_PATRON)) {
						GuildSettings settings = DatabaseManager.getManager().getSettings(guildId);
						settings.setPatronGuild(!settings.isPatronGuild());
						DatabaseManager.getManager().updateSettings(settings);
					} else {
						//Guild not on this shard...
						response.setContentType("application/json");
						response.setStatus(400);
						return JsonUtils.getJsonResponseMessage("Realm not supported on this endpoint!");
					}
				} else {
					//Guild not on this shard...
					response.setContentType("application/json");
					response.setStatus(404);
					return JsonUtils.getJsonResponseMessage("Guild not found on this shard!");
				}
			}
			//Send success response to close the connection
			response.setContentType("application/json");
			response.setStatus(200);
			return JsonUtils.getJsonResponseMessage("Success");
		} catch (JSONException e) {
			response.setContentType("application/json");
			response.setStatus(400);
			return JsonUtils.getJsonResponseMessage("Bad Request");
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[COM-API-v1] Failed to handle bot action update", e, true, this.getClass());

			response.setContentType("application/json");
			response.setStatus(500);
			return JsonUtils.getJsonResponseMessage("Internal Server Exception");
		}
	}
}
