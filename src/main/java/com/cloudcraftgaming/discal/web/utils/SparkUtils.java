package com.cloudcraftgaming.discal.web.utils;

import com.cloudcraftgaming.discal.web.endpoints.v1.*;

import static spark.Spark.*;

/**
 * Created by Nova Fox on 12/17/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class SparkUtils {
	@SuppressWarnings("ThrowableNotThrown")
	public static void initSpark() {
		staticFileLocation("/web/public"); // Main site location

		//Register the API Endpoints
		before("/api/*", (request, response) -> {
			if (!request.requestMethod().equalsIgnoreCase("POST")) {
				System.out.println("Denied '" + request.requestMethod() + "' access from: " + request.ip());
				halt(405, "Method not allowed");
			}
			//Check authorization
			if (request.headers().contains("Authorization") && !request.headers("Authorization").equals("API_KEY")) {
				//TODO: Actually check auth!!! < Just lazy right now
				halt(401, "Unauthorized");
			}
			//Only accept json because its easier to parse and handle.
			if (!request.contentType().equalsIgnoreCase("application/json")) {
				halt(400, "Bad Request");
			}
		});

		//API endpoints
		path("/api/v1", () -> {
			before("/*", (q, a) -> System.out.println("Received API call from: " + q.ip() + "; Host:" + q.host()));
			path("/guild", () -> {
				path("/settings", () -> {
					post("/get", GuildEndpoint::getSettings);
					post("/update", GuildEndpoint::updateSettings);
				});
				path("/info", () -> post("/from-user/list", GuildEndpoint::getUserGuilds));
			});
			path("/announcement", () -> {
				post("/get", AnnouncementEndpoint::getAnnouncement);
				post("/create", AnnouncementEndpoint::createAnnouncement);
				post("/update", AnnouncementEndpoint::updateAnnouncement);
				post("/delete", AnnouncementEndpoint::deleteAnnouncement);
				post("/list", AnnouncementEndpoint::listAnnouncements);
			});
			path("/calendar", () -> {
				post("/get", CalendarEndpoint::getCalendar);
				post("/list", CalendarEndpoint::listCalendars);
				post("time", TimeEndpoint::getTime);
			});
			path("/rsvp", () -> {
				post("/get", RsvpEndpoint::getRsvp);
				post("/update", RsvpEndpoint::updateRsvp);
			});
		});
	}
}