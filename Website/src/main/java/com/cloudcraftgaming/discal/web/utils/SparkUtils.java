package com.cloudcraftgaming.discal.web.utils;

import com.cloudcraftgaming.discal.api.database.DatabaseManager;
import com.cloudcraftgaming.discal.api.object.BotSettings;
import com.cloudcraftgaming.discal.api.object.web.UserAPIAccount;
import com.cloudcraftgaming.discal.logger.Logger;
import com.cloudcraftgaming.discal.web.endpoints.v1.*;
import com.cloudcraftgaming.discal.web.handler.DashboardHandler;
import com.cloudcraftgaming.discal.web.handler.DiscordAccountHandler;
import com.cloudcraftgaming.discal.web.network.discord.DiscordLoginHandler;
import spark.ModelAndView;

import static spark.Spark.*;

/**
 * Created by Nova Fox on 12/17/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class SparkUtils {
	@SuppressWarnings("ThrowableNotThrown")
	public static void initSpark() {
		if (BotSettings.RUN_API.get().equalsIgnoreCase("true")) {
			port(Integer.valueOf(BotSettings.PORT.get()));

			staticFileLocation("/web/public"); // Main site location

			notFound(((request, response) -> {
				response.redirect("/", 301);
				return response.body();
			}));

			//Register the API Endpoints
			before("/api/*", (request, response) -> {
				if (!request.requestMethod().equalsIgnoreCase("POST")) {
					Logger.getLogger().api("Denied '" + request.requestMethod() + "' access", request.ip());
					halt(405, "Method not allowed");
				}
				//Check authorization
				if (DiscordAccountHandler.getHandler().hasAccount(request.session().id())) {
					//User is logged in from website, no API key needed
					Logger.getLogger().api("API Call from website", request.ip());
				} else {
					//Requires "Authorization Header
					if (request.headers().contains("Authorization")) {
						String key = request.headers("Authorization");
						UserAPIAccount acc = DatabaseManager.getManager().getAPIAccount(key);
						if (acc != null) {
							if (acc.isBlocked()) {
								Logger.getLogger().api("Attempted to use blocked API Key: " + acc.getAPIKey(), request.ip());
								halt(401, "Unauthorized");
							} else {
								//Everything checks out!
								acc.setUses(acc.getUses() + 1);
								DatabaseManager.getManager().updateAPIAccount(acc);
							}
						} else {
							Logger.getLogger().api("Attempted to use invalid API Key: " + key, request.ip());
							halt(401, "Unauthorized");
						}
					} else {
						Logger.getLogger().api("Attempted to use API without authorization header", request.ip());
						halt(400, "Bad Request");
					}
				}
				//Only accept json because its easier to parse and handle.
				/*
				if (!request.contentType().equalsIgnoreCase("application/json")) {
					halt(400, "Bad Request");
				}
				*/
			});

			//API endpoints
			path("/api/v1", () -> {
				before("/*", (q, a) -> Logger.getLogger().api("Received API Call", q.ip(), q.host(), q.pathInfo()));
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
				});
				path("/events", () -> {
					path("/list", () -> {
						post("/month", EventEndpoint::getEventsForMonth);
						post("/date", EventEndpoint::getEventsForSelectedDate);
					});
					post("/update", EventEndpoint::updateEvent);
					post("/create", EventEndpoint::createEvent);
					post("/delete", EventEndpoint::deleteEvent);
				});
				path("/rsvp", () -> {
					post("/get", RsvpEndpoint::getRsvp);
					post("/update", RsvpEndpoint::updateRsvp);
				});
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


			//Various endpoints for thyme because yeah...
			path("/account", () -> {
				get("/login", DiscordLoginHandler::handleDiscordCode);
				get("/logout", DiscordLoginHandler::handleLogout);
			});

			//Templates and pages...
			get("/", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/index"), new ThymeleafTemplateEngine());
			get("/home", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/index"), new ThymeleafTemplateEngine());
			get("/about", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/about"), new ThymeleafTemplateEngine());
			get("/commands", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/commands"), new ThymeleafTemplateEngine());
			get("/lazy-discal", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/lazy-discal"), new ThymeleafTemplateEngine());
			get("/setup", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/setup"), new ThymeleafTemplateEngine());
			get("/policy/privacy", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/policy/privacy"), new ThymeleafTemplateEngine());

			//Dashboard pages
			get("/dashboard", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/dashboard/dashboard"), new ThymeleafTemplateEngine());
			get("/dashboard/guild", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/dashboard/guild"), new ThymeleafTemplateEngine());
			get("/dashboard/guild/calendar", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/dashboard/components/calendar"), new ThymeleafTemplateEngine());
			get("/dashboard/guild/events", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/dashboard/components/events"), new ThymeleafTemplateEngine());
			get("/dashboard/guild/announcements", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/dashboard/components/announcements"), new ThymeleafTemplateEngine());
			get("/dashboard/guild/rsvp", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/dashboard/components/rsvp"), new ThymeleafTemplateEngine());

			//Embed pages
			//get("/embed/calendar/:guild", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccountForGuildEmbed(rq.session().id(), rq.params(":guild")), "pages/embed/calendar"), new ThymeleafTemplateEngine());

			//Various other doc pages
			get("/docs/event/colors", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/events/event-colors"), new ThymeleafTemplateEngine());

			//API Doc Pages
			get("/docs/api/overview", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/api/overview"), new ThymeleafTemplateEngine());
			get("/docs/api/errors", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/api/errors"), new ThymeleafTemplateEngine());

			//API v1 Doc Pages
			get("/docs/api/v1/announcement", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/api/v1/announcement"), new ThymeleafTemplateEngine());
			get("/docs/api/v1/calendar", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/api/v1/calendar"), new ThymeleafTemplateEngine());
			get("/docs/api/v1/events", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/api/v1/events"), new ThymeleafTemplateEngine());
			get("/docs/api/v1/guild", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/api/v1/guild"), new ThymeleafTemplateEngine());
			get("/docs/api/v1/rsvp", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/api/v1/rsvp"), new ThymeleafTemplateEngine());
			get("/docs/api/v1/time", (rq, rs) -> new ModelAndView(DiscordAccountHandler.getHandler().getAccount(rq.session().id()), "pages/docs/api/v1/time"), new ThymeleafTemplateEngine());
		}
	}
}