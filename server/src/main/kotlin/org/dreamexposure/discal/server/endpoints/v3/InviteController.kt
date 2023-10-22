package org.dreamexposure.discal.server.endpoints.v3

import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.config.Config
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v3/invite")
class InviteController {
    @SecurityRequirement(disableSecurity = true, scopes = [])
    @GetMapping(produces = ["text/plain"])
    fun get() = Config.URL_INVITE.getString()
}
