package org.dreamexposure.discal.web.spring;

import org.dreamexposure.discal.web.handler.DiscordAccountHandler;
import org.dreamexposure.discal.web.network.discal.StatusHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings({"unused"})
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
		return "various/about";
	}

	@RequestMapping("/commands")
	public String commands(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "various/commands";
	}

	@RequestMapping("/lazy-discal")
	public String lazyDisCal(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "various/lazy-discal";
	}

	@RequestMapping("/setup")
	public String setup(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "various/setup";
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

	//Account pages
	@RequestMapping("/login")
	public String accountLogin(Map<String, Object> model, HttpServletRequest req) {
		if (DiscordAccountHandler.getHandler().hasAccount(req))
			return "redirect:/dashboard";
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "account/login";
	}

	//Dashboard pages
	@RequestMapping("/dashboard/{id}")
	public String dashboard(Map<String, Object> model, HttpServletRequest req, @PathVariable String id) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "dashboard/guild";
	}

	//Random Docs pages
	@RequestMapping("/docs/event/colors")
	public String docsEventsEventColors(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/event/event-colors";
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

	//API v2 doc pages
	@RequestMapping("/docs/api/v2/announcement")
	public String docsApiV2Announcement(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v2/announcement";
	}

	@RequestMapping("/docs/api/v2/calendar")
	public String docsApiV2Calendar(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v2/calendar";
	}

	@RequestMapping("/docs/api/v2/events")
	public String docsApiV2Events(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v2/events";
	}

	@RequestMapping("/docs/api/v2/guild")
	public String docsApiV2Guild(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v2/guild";
	}

	@RequestMapping("/docs/api/v2/rsvp")
	public String docsApiV2Rsvp(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v2/rsvp";
	}

	@RequestMapping("/docs/api/v2/status")
	public String docsApiV2Status(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "docs/api/v2/status";
	}

	//Embed pages
	@RequestMapping("/embed/calendar/{id}")
	public String embedCalendar(Map<String, Object> model, HttpServletRequest req, @PathVariable String id) {
		//This is a deprecated URL, but we are just redirecting for backwards compat.
		return "redirect:/embed/calendar/" + id + "/1";
	}

	@RequestMapping("/embed/calendar/{id}/{num}")
	public String embedCalendarWithNum(Map<String, Object> model, HttpServletRequest req, @PathVariable String id, @PathVariable String num) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getEmbedAccount(req));
		return "embed/calendar";
	}

	@RequestMapping("/status")
	public String status(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));

		//Just add status info here so we don't need to use JS for the status page.
		model.remove("status");
		model.put("status", StatusHandler.getLatestStatusInfo());

		return "various/status";
	}

	//Error pages - I actually reference this so need the mapping here
	@RequestMapping("/400")
	public String badRequest(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "error/400";
	}

	@RequestMapping("/404")
	public String notFound(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "error/404";
	}

	@RequestMapping("/500")
	public String internalError(Map<String, Object> model, HttpServletRequest req) {
		model.clear();
		model.putAll(DiscordAccountHandler.getHandler().getAccount(req));
		return "error/500";
	}
}