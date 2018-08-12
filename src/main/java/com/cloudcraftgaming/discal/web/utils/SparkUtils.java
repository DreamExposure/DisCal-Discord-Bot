package com.cloudcraftgaming.discal.web.utils;

/**
 * Created by Nova Fox on 12/17/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class SparkUtils {
	@SuppressWarnings("ThrowableNotThrown")
	public static void initSpark() {
		/*
		if (BotSettings.RUN_API.get().equalsIgnoreCase("true")) {

			notFound(((request, response) -> {
				response.redirect("/", 301);
				return response.body();
			}));

			//API endpoints
			path("/api/v1", () -> {
				before("/*", (q, a) -> Logger.getLogger().api("Received API Call", q.ip(), q.host(), q.pathInfo()));
				path("/dashboard", () -> {
					path("/select", () -> {
						post("/guild", DashboardHandler::handleGuildSelect);
						post("/settings", DashboardHandler::handleSettingsSelect);
					});

					path("/create", () -> {
						post("/calendar", DashboardHandler::handleCalendarCreate);
						post("/announcement", DashboardHandler::handleAnnouncementCreate);
					});
					path("/update", () -> {
						post("/announcement", DashboardHandler::handleAnnouncementUpdate);
						post("/calendar", DashboardHandler::handleCalendarUpdate);
						post("/settings", DashboardHandler::handleSettingsUpdate);
					});
					path("/delete", () -> {
						post("/calendar", DashboardHandler::deleteCalendar);
						post("/announcement", DashboardHandler::deleteAnnouncement);
					});
				});
			});
		}
		*/
	}
}