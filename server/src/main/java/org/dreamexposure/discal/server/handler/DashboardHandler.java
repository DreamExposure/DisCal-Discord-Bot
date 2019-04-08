package org.dreamexposure.discal.server.handler;

import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Calendar;
import discord4j.core.object.util.Snowflake;
import org.dreamexposure.discal.core.calendar.CalendarAuth;
import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.announcement.AnnouncementType;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.enums.network.CrossTalkReason;
import org.dreamexposure.discal.core.enums.network.DisCalRealm;
import org.dreamexposure.discal.core.logger.Logger;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.announcement.Announcement;
import org.dreamexposure.discal.core.object.calendar.CalendarData;
import org.dreamexposure.discal.core.object.web.WebCalendar;
import org.dreamexposure.discal.core.object.web.WebChannel;
import org.dreamexposure.discal.core.object.web.WebGuild;
import org.dreamexposure.discal.core.object.web.WebRole;
import org.dreamexposure.discal.core.utils.CalendarUtils;
import org.dreamexposure.discal.server.utils.ResponseUtils;
import org.dreamexposure.novautils.network.crosstalk.ServerSocketHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nova Fox on 12/19/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings({"unchecked", "unused", "Duplicates"})
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardHandler {
	@PostMapping(value = "/select/guild")
	public static String handleGuildSelect(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			String guildId = queryParams.get("guild");
			Map m = DiscordAccountHandler.getHandler().getAccount(request);

			//HANDLE OF GETTING THIS SHIT
			JSONObject body = new JSONObject();
			body.put("Reason", CrossTalkReason.GET.name());
			body.put("Realm", DisCalRealm.WEBSITE_DASHBOARD_GUILD);
			body.put("Guild-Id", guildId);
			body.put("Member-Id", m.get("id") + "");

			JSONObject data = ServerSocketHandler.sendAndReceive(body);

			//Don't worry about guild not existing, impossible to get here if it doesn't exist.
			WebGuild wg = new WebGuild().fromJson(data.getJSONObject("Guild"));

			wg.setDiscalRole(data.getBoolean("Sufficient-Role"));
			wg.setManageServer(data.getBoolean("Manager-Server"));

			if (!(wg.isManageServer() || wg.isDiscalRole())) {
				//Insufficient perms to edit that guild.
				response.sendRedirect("/dashboard");
				return "redirect:/dashboard";
			}
			m.remove("selected");

			m.put("selected", wg);

			m.remove("settings");

			m.remove("admin");

			DiscordAccountHandler.getHandler().appendAccount(m, request);

			response.sendRedirect("/dashboard/guild");
			return "redirect:/dashboard/guild";
		} catch (JSONException e) {
			Logger.getLogger().exception(null, "[WEB] JSON || Guild Select failed!", e, true, DashboardHandler.class);
			try {
				response.sendRedirect("/dashboard");
			} catch (Exception ignore) {

			}
			return "redirect:/dashboard";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Guild Select failed!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/select/settings")
	public static String handleSettingsSelect(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			String settings = queryParams.get("settings");

			Map m = new HashMap();
			m.put("settings", settings);

			DiscordAccountHandler.getHandler().appendAccount(m, request);

			if (settings.equalsIgnoreCase("calendar")) {
				response.sendRedirect("/dashboard/guild/calendar");
				return "redirect:/dashboard/guild/calendar";
			} else if (settings.equalsIgnoreCase("events")) {
				response.sendRedirect("/dashboard/guild/events");
				return "redirect:/dashboard/guild/events";
			} else if (settings.equalsIgnoreCase("announcements")) {
				response.sendRedirect("/dashboard/guild/announcements");
				return "redirect:/dashboard/guild/announcements";
			} else if (settings.equalsIgnoreCase("rsvp")) {
				response.sendRedirect("/dashboard/guild/rsvp");
				return "redirect:/dashboard/guild/rsvp";
			} else {
				response.sendRedirect("/dashboard/guild");
				return "redirect:/dashboard/guild";
			}
		} catch (JSONException e) {
			Logger.getLogger().exception(null, "[WEB] JSON || Settings Select failed!", e, true, DashboardHandler.class);

			try {
				response.sendRedirect("/dashboard");
			} catch (Exception ignore) {
			}
			return "redirect:/dashboard";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Settings Selected failed!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/update/settings")
	public static String handleSettingsUpdate(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			if (queryParams.containsKey("bot-nick")) {
				//Update bot nickname...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setBotNick(queryParams.get("bot-nick"));

					//Send to all clients. Correct client will handle the actual change.
					JSONObject data = new JSONObject();
					data.put("Reason", CrossTalkReason.UPDATE.name());
					data.put("Realm", DisCalRealm.BOT_SETTINGS);
					data.put("Guild-Id", g.getId());
					data.put("Bot-Nick", g.getBotNick());

					ServerSocketHandler.sendToAllClients(data, "SERVER", -1, -1);
				}
			} else if (queryParams.containsKey("prefix")) {
				//Update prefix...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Snowflake.of(g.getId())));
					g.getSettings().setPrefix(queryParams.get("prefix"));

					DatabaseManager.getManager().updateSettings(g.getSettings());
				}
			} else if (queryParams.containsKey("lang")) {
				//Update lang...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Snowflake.of(g.getId())));
					g.getSettings().setLang(queryParams.get("lang"));

					DatabaseManager.getManager().updateSettings(g.getSettings());
				}
			} else if (queryParams.containsKey("con-role")) {
				//Update control role...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Snowflake.of(g.getId())));


					WebRole role = g.getRole((Long.valueOf(queryParams.get("con-role"))));

					for (WebRole wr : g.getRoles()) {
						wr.setControlRole(false);
					}

					role.setControlRole(true);


					if (role.isEveryone())
						g.getSettings().setControlRole("everyone");
					else
						g.getSettings().setControlRole(role.getId() + "");

					DatabaseManager.getManager().updateSettings(g.getSettings());
				}
			} else if (queryParams.containsKey("discal-channel")) {
				//Update control role...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isDiscalRole()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Snowflake.of(g.getId())));


					for (WebChannel wc : g.getChannels()) {
						wc.setDiscalChannel(false);
					}

					g.getChannel(Long.valueOf(queryParams.get("discal-channel"))).setDiscalChannel(true);

					if (queryParams.get("discal-channel").equalsIgnoreCase("0"))
						g.getSettings().setDiscalChannel("all"); //All channels
					else
						g.getSettings().setDiscalChannel(queryParams.get("discal-channel"));

					DatabaseManager.getManager().updateSettings(g.getSettings());
				}
			} else if (queryParams.containsKey("branding")) {
				//Update branding...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Snowflake.of(g.getId())));

					if (g.getSettings().isPatronGuild()) {
						if (queryParams.containsKey("value"))
							g.getSettings().setBranded(true);
						else
							g.getSettings().setBranded(false);

						DatabaseManager.getManager().updateSettings(g.getSettings());
					}
				}
			} else if (queryParams.containsKey("simple-ann")) {
				//Update simple announcements...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				//Guess this one never checked for perms so...

				g.setSettings(DatabaseManager.getManager().getSettings(Snowflake.of(g.getId())));

				if (queryParams.containsKey("value"))
					g.getSettings().setSimpleAnnouncements(true);
				else
					g.getSettings().setSimpleAnnouncements(false);

				DatabaseManager.getManager().updateSettings(g.getSettings());

				response.sendRedirect("/dashboard/guild/announcements");
				return "redirect:/dashboard/guild/announcements";
			}

			//Finally redirect back to the dashboard
			response.sendRedirect("/dashboard/guild");
			return "redirect:/dashboard/guild";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Settings update failed!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/update/calendar")
	public static String handleCalendarUpdate(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			if (queryParams.containsKey("cal-name")) {
				//Update calendar name/summary...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isDiscalRole()) {
					try {
						Calendar cal = CalendarAuth.getCalendarService(g.getSettings()).calendars().get(g.getCalendar().getId()).execute();
						cal.setSummary(queryParams.get("cal-name"));
						CalendarAuth.getCalendarService(g.getSettings()).calendars().update(g.getCalendar().getId(), cal).execute();

						g.getCalendar().setName(queryParams.get("cal-name"));
					} catch (Exception e) {
						Logger.getLogger().exception(null, "[WEB] Failed to update calendar name", e, true, DashboardHandler.class);
					}
				}
			} else if (queryParams.containsKey("cal-desc")) {
				//Update calendar description...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isDiscalRole()) {
					try {
						Calendar cal = CalendarAuth.getCalendarService(g.getSettings()).calendars().get(g.getCalendar().getId()).execute();
						cal.setDescription(queryParams.get("cal-desc"));
						CalendarAuth.getCalendarService(g.getSettings()).calendars().update(g.getCalendar().getId(), cal).execute();

						g.getCalendar().setDescription(queryParams.get("cal-desc"));
					} catch (Exception e) {
						Logger.getLogger().exception(null, "[WEB] Failed to update calendar description", e, true, DashboardHandler.class);
					}
				}
			} else if (queryParams.containsKey("cal-tz")) {
				//Update calendar timezone
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isDiscalRole()) {
					try {
						Calendar cal = CalendarAuth.getCalendarService(g.getSettings()).calendars().get(g.getCalendar().getId()).execute();
						cal.setTimeZone(queryParams.get("cal-tz").replace("___", "/"));
						CalendarAuth.getCalendarService(g.getSettings()).calendars().update(g.getCalendar().getId(), cal).execute();

						g.getCalendar().setTimezone(queryParams.get("cal-tz"));
					} catch (Exception e) {
						Logger.getLogger().exception(null, "[WEB] Failed to update calendar timezone", e, true, DashboardHandler.class);
					}
				}
			}

			//Finally redirect back to the dashboard
			response.sendRedirect("/dashboard/guild/calendar");
			return "redirect:/dashboard/guild/calendar";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Calendar update failed!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/create/calendar")
	public static String handleCalendarCreate(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			String name = queryParams.get("cal-name");
			String desc = queryParams.get("cal-desc");
			String tz = queryParams.get("cal-tz");

			Map m = DiscordAccountHandler.getHandler().getAccount(request);
			WebGuild g = (WebGuild) m.get("selected");

			if (g.isDiscalRole()) {
				Calendar calendar = new Calendar();
				calendar.setSummary(name);
				calendar.setDescription(desc);
				calendar.setTimeZone(tz.replace("___", "/"));
				try {
					com.google.api.services.calendar.Calendar service = CalendarAuth.getCalendarService(g.getSettings());

					Calendar confirmed = service.calendars().insert(calendar).execute();
					AclRule rule = new AclRule();
					AclRule.Scope scope = new AclRule.Scope();
					scope.setType("default");
					rule.setScope(scope).setRole("reader");
					service.acl().insert(confirmed.getId(), rule).execute();
					CalendarData calendarData = new CalendarData(Snowflake.of(g.getId()), 1);
					calendarData.setCalendarId(confirmed.getId());
					calendarData.setCalendarAddress(confirmed.getId());
					DatabaseManager.getManager().updateCalendar(calendarData);

					//Refresh to display correct info...
					g.setCalendar(new WebCalendar().fromCalendar(calendarData, g.getSettings()));
				} catch (Exception ex) {
					Logger.getLogger().exception(null, "[WEB] Failed to confirm calendar.", ex, true, DashboardHandler.class);
				}
			}
			//Finally redirect back to the dashboard
			response.sendRedirect("/dashboard/guild/calendar");
			return "redirect:/dashboard/guild/calendar";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Calendar create failed!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/create/announcement")
	public static String handleAnnouncementCreate(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			String channelId = queryParams.get("channel");
			AnnouncementType type = AnnouncementType.fromValue(queryParams.get("type"));
			//Skip event ID or color here, only get it later if needed.
			String minutesRaw = queryParams.get("minutes");
			String hoursRaw = queryParams.get("hours");
			String info = queryParams.get("info");

			Map m = DiscordAccountHandler.getHandler().getAccount(request);
			WebGuild g = (WebGuild) m.get("selected");

			if (g.isDiscalRole()) {
				Announcement a = new Announcement(Snowflake.of(g.getId()));
				a.setAnnouncementChannelId(channelId);
				a.setMinutesBefore(Integer.valueOf(minutesRaw));
				a.setHoursBefore(Integer.valueOf(hoursRaw));
				a.setInfo(info);
				a.setAnnouncementType(type);

				if (type == AnnouncementType.COLOR) {
					a.setEventColor(EventColor.fromNameOrHexOrID(queryParams.get("color")));
				} else if (type == AnnouncementType.SPECIFIC) {
					a.setEventId(queryParams.get("event-id"));
				} else if (type == AnnouncementType.RECUR) {
					String value = queryParams.get("event-id");
					if (value.contains("_")) {
						String[] stuff = value.split("_");
						value = stuff[0];
					}
					a.setEventId(value);
				}

				//Create announcement
				DatabaseManager.getManager().updateAnnouncement(a);

				//Update WebGuild to display correctly...
				g.getAnnouncements().clear();
				g.getAnnouncements().addAll(DatabaseManager.getManager().getAnnouncements(Snowflake.of(g.getId())));
			}
			//Finally redirect back to the dashboard
			response.sendRedirect("/dashboard/guild/announcements");
			return "redirect:/dashboard/guild/announcements";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Announcement create failed!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/delete/calendar")
	public static String deleteCalendar(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			//TODO: Handle multiple calendars...
			//String calId = request.queryParams("calendar-id");

			Map m = DiscordAccountHandler.getHandler().getAccount(request);
			WebGuild g = (WebGuild) m.get("selected");

			if (g.isManageServer()) {
				CalendarData data = DatabaseManager.getManager().getMainCalendar(Snowflake.of(g.getId()));
				GuildSettings settings = DatabaseManager.getManager().getSettings(Snowflake.of(g.getId()));
				CalendarUtils.deleteCalendar(data, settings);

				g.setCalendar(new WebCalendar().fromCalendar(DatabaseManager.getManager().getMainCalendar(Snowflake.of(g.getId())), DatabaseManager.getManager().getSettings(Snowflake.of(g.getId()))));
				response.sendRedirect("/dashboard/guild/calendar");
				return "redirect:/dashboard/guild/calendar";
			} else {
				response.sendRedirect("/dashboard/guild/calendar");
				return "redirect:/dashboard/guild/calendar";
			}
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to delete/remove calendar!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/delete/announcement")
	public static String deleteAnnouncement(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			String announcementId = queryParams.get("id");

			Map m = DiscordAccountHandler.getHandler().getAccount(request);
			WebGuild g = (WebGuild) m.get("selected");

			if (g.isManageServer()) {
				DatabaseManager.getManager().deleteAnnouncement(announcementId);

				//Update announcements list to display correctly.
				g.getAnnouncements().clear();
				g.getAnnouncements().addAll(DatabaseManager.getManager().getAnnouncements(Snowflake.of(g.getId())));
			}
			response.sendRedirect("/dashboard/guild/announcements");
			return "redirect:/dashboard/guild/announcements";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to delete announcement!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}

	@PostMapping(value = "/update/announcement")
	public static String handleAnnouncementUpdate(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			String announcementId = queryParams.get("id");

			Map m = DiscordAccountHandler.getHandler().getAccount(request);
			WebGuild g = (WebGuild) m.get("selected");

			if (g.isManageServer()) {
				Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), Snowflake.of(g.getId()));

				a.setAnnouncementChannelId(queryParams.get("channel"));
				a.setAnnouncementType(AnnouncementType.fromValue(queryParams.get("type")));

				if (a.getAnnouncementType() == AnnouncementType.COLOR) {
					a.setEventColor(EventColor.fromNameOrHexOrID(queryParams.get("color")));
				} else if (a.getAnnouncementType() == AnnouncementType.SPECIFIC || a.getAnnouncementType() == AnnouncementType.RECUR) {
					String value = queryParams.get("event-id");
					if (value.contains("_")) {
						String[] stuff = value.split("_");
						value = stuff[0];
					}
					a.setEventId(value);
				}

				a.setMinutesBefore(Integer.valueOf(queryParams.get("minutes")));
				a.setHoursBefore(Integer.valueOf(queryParams.get("hours")));
				a.setInfo(queryParams.get("info"));

				DatabaseManager.getManager().updateAnnouncement(a);

				//Update announcements list to display correctly.
				g.getAnnouncements().clear();
				g.getAnnouncements().addAll(DatabaseManager.getManager().getAnnouncements(Snowflake.of(g.getId())));
			}
			response.sendRedirect("/dashboard/guild/announcements");
			return "redirect:/dashboard/guild/announcements";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to update/edit announcement!", e, true, DashboardHandler.class);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}