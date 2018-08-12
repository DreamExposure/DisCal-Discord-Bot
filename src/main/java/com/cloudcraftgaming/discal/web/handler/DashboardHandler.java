package com.cloudcraftgaming.discal.web.handler;

import com.cloudcraftgaming.discal.api.DisCalAPI;
import com.cloudcraftgaming.discal.api.calendar.CalendarAuth;
import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.enums.announcement.AnnouncementType;
import com.cloudcraftgaming.discal.api.enums.event.EventColor;
import com.cloudcraftgaming.discal.api.object.GuildSettings;
import com.cloudcraftgaming.discal.api.object.announcement.Announcement;
import com.cloudcraftgaming.discal.api.object.calendar.CalendarData;
import com.cloudcraftgaming.discal.api.object.web.WebCalendar;
import com.cloudcraftgaming.discal.api.object.web.WebChannel;
import com.cloudcraftgaming.discal.api.object.web.WebGuild;
import com.cloudcraftgaming.discal.api.object.web.WebRole;
import com.cloudcraftgaming.discal.api.utils.CalendarUtils;
import com.cloudcraftgaming.discal.api.utils.PermissionChecker;
import com.cloudcraftgaming.discal.logger.Logger;
import com.cloudcraftgaming.discal.web.utils.ResponseUtils;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.Calendar;
import org.json.JSONException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

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
@SuppressWarnings({"unchecked", "ThrowableNotThrown", "unused"})
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardHandler {
	@PostMapping(value = "/select/guild")
	public static String handleGuildSelect(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, String> queryParams) {
		try {
			String guildId = queryParams.get("guild");

			IGuild g = DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(guildId));
			WebGuild wg = new WebGuild().fromGuild(g);

			Map m = DiscordAccountHandler.getHandler().getAccount(request);

			IUser u = g.getUserByID(Long.valueOf((String) m.get("id")));

			wg.setDiscalRole(PermissionChecker.hasSufficientRole(g, u));
			wg.setManageServer(PermissionChecker.hasManageServerRole(g, u));

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
			Logger.getLogger().exception(null, "[WEB] JSON || Guild Select failed!", e, DashboardHandler.class, true);
			try {
				response.sendRedirect("/dashboard");
			} catch (Exception ignore) {

			}
			return "redirect:/dashboard";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Guild Select failed!", e, DashboardHandler.class, true);

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
			Logger.getLogger().exception(null, "[WEB] JSON || Settings Select failed!", e, DashboardHandler.class, true);

			try {
				response.sendRedirect("/dashboard");
			} catch (Exception ignore) {
			}
			return "redirect:/dashboard";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Settings Selected failed!", e, DashboardHandler.class, true);

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

					IGuild guild = DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(g.getId()));

					guild.setUserNickname(DisCalAPI.getAPI().getClient().getOurUser(), g.getBotNick());
				}
			} else if (queryParams.containsKey("prefix")) {
				//Update prefix...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
					g.getSettings().setPrefix(queryParams.get("prefix"));

					DatabaseManager.getManager().updateSettings(g.getSettings());
				}
			} else if (queryParams.containsKey("lang")) {
				//Update lang...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));
					g.getSettings().setLang(queryParams.get("lang"));

					DatabaseManager.getManager().updateSettings(g.getSettings());
				}
			} else if (queryParams.containsKey("con-role")) {
				//Update control role...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));

					IGuild guild = DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(g.getId()));
					IRole role = guild.getRoleByID(Long.valueOf(queryParams.get("con-role")));

					if (role.isEveryoneRole())
						g.getSettings().setControlRole("everyone");
					else
						g.getSettings().setControlRole(role.getStringID());

					DatabaseManager.getManager().updateSettings(g.getSettings());

					//Update role list to display properly...
					g.getRoles().clear();

					for (IRole r : guild.getRoles()) {
						g.getRoles().add(new WebRole().fromRole(r, g.getSettings()));
					}
				}
			} else if (queryParams.containsKey("discal-channel")) {
				//Update control role...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isDiscalRole()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));

					IGuild guild = DisCalAPI.getAPI().getClient().getGuildByID(Long.valueOf(g.getId()));

					if (queryParams.get("discal-channel").equalsIgnoreCase("0"))
						g.getSettings().setDiscalChannel("all"); //All channels
					else
						g.getSettings().setDiscalChannel(queryParams.get("discal-channel"));

					DatabaseManager.getManager().updateSettings(g.getSettings());

					//Update channel list to display properly...
					g.getChannels().clear();

					WebChannel all = new WebChannel();
					all.setId(0);
					all.setName("All Channels");
					all.setDiscalChannel(g.getSettings().getDiscalChannel().equalsIgnoreCase("all"));
					g.getChannels().add(all);
					for (IChannel c : guild.getChannels()) {
						g.getChannels().add(new WebChannel().fromChannel(c, g.getSettings()));
					}
				}
			} else if (queryParams.containsKey("branding")) {
				//Update bot nickname...
				Map m = DiscordAccountHandler.getHandler().getAccount(request);
				WebGuild g = (WebGuild) m.get("selected");

				if (g.isManageServer()) {
					g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));

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

				g.setSettings(DatabaseManager.getManager().getSettings(Long.valueOf(g.getId())));

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
			Logger.getLogger().exception(null, "[WEB] Settings update failed!", e, DashboardHandler.class, true);

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
						Logger.getLogger().exception(null, "[WEB] Failed to update calendar name", e, DashboardHandler.class, true);
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
						Logger.getLogger().exception(null, "[WEB] Failed to update calendar description", e, DashboardHandler.class, true);
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
						Logger.getLogger().exception(null, "[WEB] Failed to update calendar timezone", e, DashboardHandler.class, true);
					}
				}
			}

			//Finally redirect back to the dashboard
			response.sendRedirect("/dashboard/guild/calendar");
			return "redirect:/dashboard/guild/calendar";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Calendar update failed!", e, DashboardHandler.class, true);

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
					CalendarData calendarData = new CalendarData(Long.valueOf(g.getId()), 1);
					calendarData.setCalendarId(confirmed.getId());
					calendarData.setCalendarAddress(confirmed.getId());
					DatabaseManager.getManager().updateCalendar(calendarData);

					//Refresh to display correct info...
					g.setCalendar(new WebCalendar().fromCalendar(calendarData, g.getSettings()));
				} catch (Exception ex) {
					Logger.getLogger().exception(null, "[WEB] Failed to confirm calendar.", ex, DashboardHandler.class, true);
				}
			}
			//Finally redirect back to the dashboard
			response.sendRedirect("/dashboard/guild/calendar");
			return "redirect:/dashboard/guild/calendar";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Calendar create failed!", e, DashboardHandler.class, true);

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
				Announcement a = new Announcement(Long.valueOf(g.getId()));
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
				g.getAnnouncements().addAll(DatabaseManager.getManager().getAnnouncements(Long.valueOf(g.getId())));
			}
			//Finally redirect back to the dashboard
			response.sendRedirect("/dashboard/guild/announcements");
			return "redirect:/dashboard/guild/announcements";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Announcement create failed!", e, DashboardHandler.class, true);

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
				CalendarData data = DatabaseManager.getManager().getMainCalendar(Long.valueOf(g.getId()));
				GuildSettings settings = DatabaseManager.getManager().getSettings(Long.valueOf(g.getId()));
				CalendarUtils.deleteCalendar(data, settings);

				g.setCalendar(new WebCalendar().fromCalendar(DatabaseManager.getManager().getMainCalendar(Long.valueOf(g.getId())), DatabaseManager.getManager().getSettings(Long.valueOf(g.getId()))));
				response.sendRedirect("/dashboard/guild/calendar");
				return "redirect:/dashboard/guild/calendar";
			} else {
				response.sendRedirect("/dashboard/guild/calendar");
				return "redirect:/dashboard/guild/calendar";
			}
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to delete/remove calendar!", e, DashboardHandler.class, true);

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
				g.getAnnouncements().addAll(DatabaseManager.getManager().getAnnouncements(Long.valueOf(g.getId())));
			}
			response.sendRedirect("/dashboard/guild/announcements");
			return "redirect:/dashboard/guild/announcements";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to delete announcement!", e, DashboardHandler.class, true);

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
				Announcement a = DatabaseManager.getManager().getAnnouncement(UUID.fromString(announcementId), Long.valueOf(g.getId()));

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
				g.getAnnouncements().addAll(DatabaseManager.getManager().getAnnouncements(Long.valueOf(g.getId())));
			}
			response.sendRedirect("/dashboard/guild/announcements");
			return "redirect:/dashboard/guild/announcements";
		} catch (Exception e) {
			Logger.getLogger().exception(null, "[WEB] Failed to update/edit announcement!", e, DashboardHandler.class, true);

			response.setContentType("application/json");
			response.setStatus(500);
			return ResponseUtils.getJsonResponseMessage("Internal Server Error");
		}
	}
}