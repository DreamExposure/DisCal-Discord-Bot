package org.dreamexposure.discal.server.endpoints.v3

import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.annotations.SecurityRequirement
import org.dreamexposure.discal.core.business.AnnouncementService
import org.dreamexposure.discal.core.`object`.new.Announcement
import org.dreamexposure.discal.core.`object`.new.security.Scope
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v3/guilds/{guildId}/announcements")
class AnnouncementController(
    private val announcementService: AnnouncementService,
) {
    // TODO: Need way to check if authenticated user has access to the guild...

    @SecurityRequirement(scopes = [Scope.ANNOUNCEMENT_WRITE])
    @PostMapping(produces = ["application/json"], consumes = ["application/json"])
    suspend fun createAnnouncement(@PathVariable guildId: Snowflake, @RequestBody announcement: Announcement): Announcement {
        return announcementService.createAnnouncement(announcement)
    }

    @SecurityRequirement(scopes = [Scope.ANNOUNCEMENT_READ])
    @GetMapping(produces = ["application/json"])
    suspend fun getAllAnnouncements(@PathVariable guildId: Snowflake): List<Announcement> {
        return announcementService.getAllAnnouncements(guildId)
    }

    @SecurityRequirement(scopes = [Scope.ANNOUNCEMENT_READ])
    @GetMapping("/{announcementId}")
    suspend fun getAnnouncement(@PathVariable guildId: Snowflake, @PathVariable announcementId: String): Announcement? {
        return announcementService.getAnnouncement(guildId, announcementId)
    }

    @SecurityRequirement(scopes = [Scope.ANNOUNCEMENT_WRITE])
    @PatchMapping("/{announcementId}", produces = ["application/json"], consumes = ["application/json"])
    suspend fun patchAnnouncement(@PathVariable guildId: Snowflake, @PathVariable announcementId: String, @RequestBody announcement: Announcement) {
        announcementService.updateAnnouncement(announcement)
    }

    @SecurityRequirement(scopes = [Scope.ANNOUNCEMENT_WRITE])
    @DeleteMapping("/{announcementId}")
    suspend fun deleteAnnouncement(@PathVariable guildId: Snowflake, @PathVariable announcementId: String) {
        announcementService.deleteAnnouncement(guildId, announcementId)
    }
}
