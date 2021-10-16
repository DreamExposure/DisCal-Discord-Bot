# noinspection SqlResolveForFile

# Announcements table
ALTER TABLE `${prefix}announcements` CONVERT TO CHARACTER
    SET utf8mb4 COLLATE utf8mb4_general_ci;

# API table
ALTER TABLE `${prefix}api` CONVERT TO CHARACTER
    SET utf8mb4 COLLATE utf8mb4_general_ci;

# Calendars table
ALTER TABLE `${prefix}calendars` CONVERT TO CHARACTER
    SET utf8mb4 COLLATE utf8mb4_general_ci;

# Events table
ALTER TABLE `${prefix}events` CONVERT TO CHARACTER
    SET utf8mb4 COLLATE utf8mb4_general_ci;

# Guild Settings table
ALTER TABLE `${prefix}guild_settings` CONVERT TO CHARACTER
    SET utf8mb4 COLLATE utf8mb4_general_ci;

# RSVP table
ALTER TABLE `${prefix}rsvp` CONVERT TO CHARACTER
    SET utf8mb4 COLLATE utf8mb4_general_ci;

