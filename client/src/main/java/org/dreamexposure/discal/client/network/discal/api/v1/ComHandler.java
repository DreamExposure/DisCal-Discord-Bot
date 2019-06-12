package org.dreamexposure.discal.client.network.discal.api.v1;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import org.dreamexposure.discal.client.utils.GuildFinder;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.core.utils.PermissionChecker;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"Duplicates"})
@RestController
@RequestMapping("/api/v1/com")
public class ComHandler {

	@PostMapping(value = "/website/dashboard/guild", produces = "application/json")
	public static String getWebsiteDashboardGuild(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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

				jsonResponse.put("guild", new WebGuild().fromGuild(guild.get()).toJson());

				response.setStatus(200);
				response.setContentType("application/json");
				return jsonResponse.toString();
			} else {
				jsonResponse.put("message", "Member not Found");
				response.setStatus(404);
				response.setContentType("application/json");
				return jsonResponse.toString();
			}
		} else {
			jsonResponse.put("message", "Guild not Found");
			response.setStatus(404);
			response.setContentType("application/json");
			return jsonResponse.toString();
		}
	}

	@PostMapping(value = "/website/dashboard/defaults", produces = "application/json")
	public static String getWebsiteDashboardDefaults(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
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
				webGuilds.put(wg.toJson());
			}
		}

		if (webGuilds.length() > 0) {
			jsonResponse.put("guilds", webGuilds);
			jsonResponse.put("count", webGuilds.length());
			response.setStatus(200);
			response.setContentType("application/json");
			return jsonResponse.toString();
		} else {
			jsonResponse.put("message", "Success, however, no listed guilds are connected.");
			response.setStatus(204);
			response.setContentType("application/json");
			return jsonResponse.toString();
		}
	}

	@PostMapping(value = "/website/embed/calendar", produces = "application/json")
	public static String getWebsiteEmbedCalendar(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) {
		//Get guild for calendar embed on the website.
		JSONObject jsonRequest = new JSONObject(requestBody);
		JSONObject jsonResponse = new JSONObject();

		Optional<Guild> guild = GuildFinder.findGuild(Snowflake.of(jsonRequest.getLong("guild_id")));

		if (guild.isPresent()) {
			jsonResponse.put("guild", new WebGuild().fromGuild(guild.get()).toJson());

			response.setStatus(200);
			response.setContentType("application/json");
			return jsonResponse.toString();
		} else {
			jsonResponse.put("message", "Guild not Found");
			response.setStatus(404);
			response.setContentType("application/json");
			return jsonResponse.toString();
		}
	}
}
