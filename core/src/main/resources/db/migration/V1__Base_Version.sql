-- MySQL dump 10.13  Distrib 5.7.26, for Linux (x86_64)
--
-- Host: host    Database: discal
-- ------------------------------------------------------
-- Server version	redacted

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `${prefix}announcements`
--

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `${prefix}announcements`
(
	`ANNOUNCEMENT_ID`   varchar(255) NOT NULL,
	`GUILD_ID`          varchar(255) NOT NULL,
	`SUBSCRIBERS_ROLE`  longtext     NOT NULL,
	`SUBSCRIBERS_USER`  longtext     NOT NULL,
	`CHANNEL_ID`        varchar(255) NOT NULL,
	`ANNOUNCEMENT_TYPE` varchar(255) NOT NULL,
	`EVENT_ID`          longtext     NOT NULL,
	`EVENT_COLOR`       varchar(255) NOT NULL,
	`HOURS_BEFORE`      int(11)      NOT NULL,
	`MINUTES_BEFORE`    int(11)      NOT NULL,
	`INFO`              longtext     NOT NULL,
	`ENABLED`           tinyint(1)   NOT NULL DEFAULT '1',
	`INFO_ONLY`         tinyint(1)   NOT NULL DEFAULT '0',
	PRIMARY KEY (`ANNOUNCEMENT_ID`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `${prefix}api`
--

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `${prefix}api`
(
	`USER_ID`     varchar(255) NOT NULL,
	`API_KEY`     varchar(64)  NOT NULL,
	`BLOCKED`     tinyint(1)   NOT NULL,
	`TIME_ISSUED` mediumtext   NOT NULL,
	`USES`        int(11)      NOT NULL,
	PRIMARY KEY (`USER_ID`, `API_KEY`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `${prefix}calendars`
--

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `${prefix}calendars`
(
	`GUILD_ID`         varchar(255) NOT NULL,
	`CALENDAR_NUMBER`  int(11)      NOT NULL,
	`CALENDAR_ID`      varchar(255) NOT NULL,
	`CALENDAR_ADDRESS` longtext     NOT NULL,
	`EXTERNAL`         tinyint(1)   NOT NULL DEFAULT '0',
	PRIMARY KEY (`GUILD_ID`, `CALENDAR_NUMBER`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `${prefix}events`
--

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `${prefix}events`
(
	`GUILD_ID`   varchar(255) NOT NULL,
	`EVENT_ID`   varchar(255) NOT NULL,
	`EVENT_END`  bigint(20)   NOT NULL,
	`IMAGE_LINK` longtext,
	PRIMARY KEY (`GUILD_ID`, `EVENT_ID`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `${prefix}guild_settings`
--

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `${prefix}guild_settings`
(
	`GUILD_ID`            varchar(255) NOT NULL,
	`EXTERNAL_CALENDAR`   tinyint(1)   NOT NULL,
	`PRIVATE_KEY`         varchar(16)  NOT NULL,
	`ACCESS_TOKEN`        longtext     NOT NULL,
	`REFRESH_TOKEN`       longtext     NOT NULL,
	`CONTROL_ROLE`        longtext     NOT NULL,
	`DISCAL_CHANNEL`      longtext     NOT NULL,
	`SIMPLE_ANNOUNCEMENT` tinyint(1)   NOT NULL DEFAULT '0',
	`PATRON_GUILD`        tinyint(1)   NOT NULL,
	`DEV_GUILD`           tinyint(1)   NOT NULL,
	`MAX_CALENDARS`       int(11)      NOT NULL,
	`DM_ANNOUNCEMENTS`    longtext     NOT NULL,
	`LANG`                varchar(255) NOT NULL DEFAULT 'ENGLISH',
	`PREFIX`              varchar(255) NOT NULL DEFAULT '!',
	`12_HOUR`             tinyint(1)   NOT NULL DEFAULT '1',
	`BRANDED`             tinyint(1)   NOT NULL DEFAULT '0',
	PRIMARY KEY (`GUILD_ID`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `${prefix}rsvp`
--

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE IF NOT EXISTS `${prefix}rsvp`
(
	`GUILD_ID`      varchar(255) NOT NULL,
	`EVENT_ID`      varchar(255) NOT NULL,
	`EVENT_END`     mediumtext   NOT NULL,
	`GOING_ON_TIME` longtext,
	`GOING_LATE`    longtext,
	`NOT_GOING`     longtext,
	`UNDECIDED`     longtext,
	PRIMARY KEY (`GUILD_ID`, `EVENT_ID`)
) ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2019-06-23  7:15:33
