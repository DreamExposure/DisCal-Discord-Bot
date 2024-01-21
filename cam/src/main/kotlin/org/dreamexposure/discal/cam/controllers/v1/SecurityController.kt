package org.dreamexposure.discal.cam.controllers.v1

import org.dreamexposure.discal.cam.business.SecurityService
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.`object`.new.security.Scope.INTERNAL_CAM_VALIDATE_TOKEN
import org.dreamexposure.discal.core.`object`.new.security.TokenType.INTERNAL
import org.dreamexposure.discal.core.`object`.rest.v1.security.ValidateRequest
import org.dreamexposure.discal.core.`object`.rest.v1.security.ValidateResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/security")
class SecurityController(
        private val securityService: SecurityService,
) {
    @SecurityRequirement(schemas = [INTERNAL], scopes = [INTERNAL_CAM_VALIDATE_TOKEN])
    @PostMapping("/validate", produces = ["application/json"])
    suspend fun validate(@RequestBody request: ValidateRequest): ValidateResponse {
        val result = securityService.authenticateAndAuthorizeToken(
            request.token,
            request.schemas,
            request.scopes,
        )

        return ValidateResponse(result.first == HttpStatus.OK, result.first, result.second)
    }
}
