package org.dreamexposure.discal.server.endpoints.v3

import org.dreamexposure.discal.core.annotations.Authentication
import org.dreamexposure.discal.core.config.Config
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v3/invite")
class InviteController {
    @Authentication(access = Authentication.AccessLevel.PUBLIC)
    @GetMapping(produces = ["text/plain"])
    fun get() = Config.URL_INVITE.getString()
}
