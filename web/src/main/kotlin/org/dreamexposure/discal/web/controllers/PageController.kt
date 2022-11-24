package org.dreamexposure.discal.web.controllers

import org.dreamexposure.discal.web.handler.DiscordAccountHandler
import org.dreamexposure.discal.web.network.discal.StatusHandler
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Controller
class PageController(
    private val accountHandler: DiscordAccountHandler,
    private val statusHandler: StatusHandler,
) {
    @RequestMapping("/", "/home")
    fun home(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("index")
    }

    @RequestMapping("/about")
    fun about(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("various/about")
    }

    @RequestMapping("/commands")
    fun commands(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("various/commands")
    }

    @RequestMapping("/lazy-discal")
    fun lazyDisCal(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("various/lazy-discal")
    }

    @RequestMapping("/setup")
    fun setup(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("various/setup")
    }

    @RequestMapping("/status")
    fun status(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .doOnNext { model.remove("status") }
                .then(statusHandler.getLatestStatusInfo())
                .doOnNext { model["status"] = it }
                .thenReturn("various/status")
    }

    //Account pages
    @RequestMapping("/login")
    fun accountLogin(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.hasAccount(swe)
                .flatMap { has ->
                    if (has) {
                        return@flatMap Mono.just("redirect:/dashboard")
                    } else {
                        return@flatMap accountHandler.getAccount(swe)
                                .doOnNext { model.clear() }
                                .doOnNext(model::putAll)
                                .thenReturn("account/login")
                    }
                }
    }

    //Dashboard pages
    @RequestMapping("/dashboard")
    fun dashboard(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("dashboard/dashboard")
    }

    @RequestMapping("/dashboard/{id}")
    fun dashboardGuild(model: MutableMap<String, Any>, swe: ServerWebExchange, @PathVariable id: String): Mono<String> {
        return accountHandler.hasAccount(swe)
                .flatMap { has ->
                    if (!has) {
                        return@flatMap Mono.just("redirect:/dashboard")
                    } else {
                        return@flatMap accountHandler.getAccount(swe)
                                .doOnNext { model.clear() }
                                .doOnNext(model::putAll)
                                .doOnNext { model["dashboard_selected_id"] = id }
                                .thenReturn("dashboard/guild")
                    }
                }
    }

    @RequestMapping("/dashboard/{id}/calendar")
    fun dashboardCalendar(model: MutableMap<String, Any>, swe: ServerWebExchange, @PathVariable id: String):
            Mono<String> {
        return accountHandler.hasAccount(swe)
                .flatMap { has ->
                    if (!has) {
                        return@flatMap Mono.just("redirect:/dashboard")
                    } else {
                        return@flatMap accountHandler.getAccount(swe)
                                .doOnNext { model.clear() }
                                .doOnNext(model::putAll)
                                .doOnNext { model["dashboard_selected_id"] = id }
                                .thenReturn("dashboard/calendar")
                    }
                }
    }

    //Embed pages
    @RequestMapping("/embed/calendar/{id}")
    @Deprecated("This remains solely for backwards compat for users", ReplaceWith("N/a"))
    fun oldEmbedCalendar(model: MutableMap<String, Any>, swe: ServerWebExchange, @PathVariable id: String):
            Mono<String> {
        //This is a deprecated URL, but we are just redirecting for backwards compat
        return Mono.just("redirect:/embed/$id/calendar/1")
    }

    @RequestMapping("/embed/{id}/calendar/{num}")
    fun embedCalendarWithNum(model: MutableMap<String, Any>, swe: ServerWebExchange, @PathVariable id: String,
                             @PathVariable num: String): Mono<String> {
        return accountHandler.getEmbedAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("embed/calendar")

    }

    //Docs

    // Docs -- Misc
    @RequestMapping("/docs/event/colors")
    fun docsEventsEventColors(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/event/event-colors")
    }

    // Docs -- API Main
    @RequestMapping("/docs/api/overview")
    fun docsApiOverview(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/api/overview")
    }

    @RequestMapping("/docs/api/errors")
    fun docsApiErrors(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/api/errors")
    }

    // Docs -- API v2
    @RequestMapping("/docs/api/v2/announcement")
    fun docsApiV2Announcement(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/api/v2/announcement")
    }

    @RequestMapping("/docs/api/v2/calendar")
    fun docsApiV2Calendar(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/api/v2/calendar")
    }

    @RequestMapping("/docs/api/v2/events")
    fun docsApiV2Events(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/api/v2/events")
    }

    @RequestMapping("/docs/api/v2/guild")
    fun docsApiV2Guild(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/api/v2/guild")
    }

    @RequestMapping("/docs/api/v2/rsvp")
    fun docsApiV2Rsvp(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/api/v2/rsvp")
    }

    @RequestMapping("/docs/api/v2/status")
    fun docsApiV2Status(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("docs/api/v2/status")
    }

    //Policy pages
    @RequestMapping("/policy/privacy")
    fun privacyPolicy(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("policy/privacy")
    }

    @RequestMapping("/policy/tos")
    fun termsOfService(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("policy/tos")
    }

    //Error pages -- I actually reference this so need to make the mappings
    @RequestMapping("/400")
    fun badRequest(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("error/400")
    }

    @RequestMapping("/404")
    fun notFound(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("error/404")
    }

    @RequestMapping("/500")
    fun internalError(model: MutableMap<String, Any>, swe: ServerWebExchange): Mono<String> {
        return accountHandler.getAccount(swe)
                .doOnNext { model.clear() }
                .doOnNext(model::putAll)
                .thenReturn("error/500")
    }
}
