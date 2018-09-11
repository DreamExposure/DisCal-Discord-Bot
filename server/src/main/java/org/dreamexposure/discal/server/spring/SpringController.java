package org.dreamexposure.discal.server.spring;

import org.dreamexposure.discal.server.handler.DiscordAccountHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@SuppressWarnings({"unchecked", "unused"})
@Controller
public class SpringController {

	//Main pages
	@RequestMapping(value = {"/", "/home"})
	public String home(Map<String, Object> model, HttpServletRequest req) {
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "index";
	}

	@RequestMapping("/about")
	public String about(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "about";
	}

	@RequestMapping("/commands")
	public String commands(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "commands";
	}

	@RequestMapping("/lazy-discal")
	public String lazyDisCal(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "lazy-discal";
	}

	@RequestMapping("/setup")
	public String setup(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "setup";
	}

	//Policy pages
	@RequestMapping("/policy/privacy")
	public String privacyPolicy(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "policy/privacy";
	}

	@RequestMapping("/policy/tos")
	public String termsOfService(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "policy/tos";
	}

	//Dashboard pages
	@RequestMapping("/dashboard")
	public String dashboard(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "dashboard/dashboard";
	}

	@RequestMapping("/dashboard/guild")
	public String dashboardGuild(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "dashboard/guild";
	}

	@RequestMapping("/dashboard/guild/calendar")
	public String dashboardComponentCalendar(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "dashboard/components/calendar";
	}

	@RequestMapping("/dashboard/guild/events")
	public String dashboardComponentEvents(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "dashboard/components/events";
	}

	@RequestMapping("/dashboard/guild/announcements")
	public String dashboardComponentAnnouncements(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "dashboard/components/announcements";
	}

	@RequestMapping("/dashboard/guild/rsvp")
	public String dashboardComponentRsvp(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "dashboard/components/rsvp";
	}

	//Random Docs pages
	@RequestMapping("/docs/event/colors")
	public String docsEventsEventColors(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/events/event-colors";
	}

	//API Doc pages
	@RequestMapping("/docs/api/overview")
	public String docsApiOverview(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/overview";
	}

	@RequestMapping("/docs/api/errors")
	public String docsApiErrors(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/errors";
	}

	//API v1 doc pages
	@RequestMapping("/docs/api/v1/announcement")
	public String docsApiV1Announcement(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v1/announcement";
	}

	@RequestMapping("/docs/api/v1/calendar")
	public String docsApiV1Calendar(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v1/calendar";
	}

	@RequestMapping("/docs/api/v1/events")
	public String docsApiV1Events(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v1/events";
	}

	@RequestMapping("/docs/api/v1/guild")
	public String docsApiV1Guild(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v1/guild";
	}

	@RequestMapping("/docs/api/v1/rsvp")
	public String docsApiV1Rsvp(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v1/rsvp";
	}

	//Embed pages
	@RequestMapping("/embed/calendar/{id}")
	public String embedCalendar(Map<String, Object> model, HttpServletRequest req, @PathVariable String id) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccountForGuildEmbed(req, id));
		return "embed/calendar";
	}

	@RequestMapping("/status")
	public String status(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "status";
	}
}