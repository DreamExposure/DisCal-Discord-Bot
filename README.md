# DisCal
[![Discord](https://img.shields.io/discord/375357265198317579?label=DreamExposure&style=flat-square)](https://discord.gg/2TFqyuy)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/DreamExposure/DisCal-Discord-Bot/gradle.yml?branch=develop&label=Build&style=flat-square)](https://github.com/DreamExposure/DisCal-Discord-Bot/actions)
[![Website](https://img.shields.io/website?down_color=red&down_message=offline&label=Status&style=flat-square&up_message=online&url=https%3A%2F%2Fwww.discalbot.com)](https://discalbot.com/status)


A calendar bot made for communities, 
DisCal integrates directly with calendar services to bring you superior support and features.
Custom calendars, events, automated reminders and more, ready for you, and ready for your community.

# 🔗 Quick Links
- [Invite](https://discord.com/api/oauth2/authorize?client_id=265523588918935552&permissions=420979666000&scope=bot%20applications.commands)
- [Website](https://discalbot.com) (rewrite in progress, dev version available [here](https://dev.discalbot.com))
- [Discord Support Server](https://discord.gg/2TFqyuy)
- [Patreon](https://www.patreon.com/Novafox)

# 💎 Core Features
- Custom Calendars 
  - Create a fully custom calendar to suit your community's needs, without feeling out of place.
  - Powerful in-server integration of Google Calendar (plus more in the future)
- Unlimited Events
  - Have a busy community? DisCal can make sure all your community events are scheduled, no matter the amount.
- Automated Reminders
  - DisCal can automatically remind your community of upcoming events, so no one misses out.
- Integrated RSVP
  - Need to know who is planning to attend? Community members can let you know whether they are attending.


## 🎉 Patron features
Patrons and supporters on the $5/month plans (or more) get access to work in progress and exclusive features.
These features aren't required for core functionality and help support the development and hosting of this bot.

- Web Dashboard (Early WIP)
- Multiple calendars
- Server branding
  - Hide the DisCal name in favor of using your server's name on announcements and embeds.
- Announcement Publishing
  - Announcements posted in news channels can be (optionally) automatically "published" so servers following the news channel receive them as well!
- Gif support for event images
- Automatically assign roles to users when RSVPing to an event.

## ⌨️ Commands
- [Commands](https://discalbot.com/commands)
- If you would like a mobile-friendly experience, you can try the [dev site's version](https://dev.discalbot.com/commands)

<!--TODO: Add commands here in collapsed tables-->


# 🗓️ Planned & Work In Progress
This bot is a hobby project for me, please not that white these features are planned, there's no solid timeline.
- Website rewrite (it's old and ugly)
- [WIP] Migration to Spring data
- [WIP] Kotlin coroutines rewrite

# 🧰 Tech stack
- Java 17
- [Discord4J](https://github.com/Discord4J/DIscord4J)
- Spring Boot (DI, Data, Actuator, etc.)
- Flyway for automatic database migrations (MySQL)
- Redis cluster caching
- Fully containerized with Docker (hosted in Kubernetes, docker-compose for local development)

# ✏️ Contributing
DisCal is an open source, GPL-3 project. We always welcome and appreciate contributions.

## 💻 Development & Local Testing
For development, you need JDK 17+ and Docker installed.

1. Fork this repository and open it in your favorite editor (IntelliJ recommended for Kotlin)
2. Write your code and add applicable tests
3. Compile and build the docker image with `./gradlew clean jibDockerBuild`
4. Place config in `./docker/{api/bot/cam}/application.properties`
5. Start the bot and dependencies for testing with `docker compose up -d`
    - You can connect to the Java debugger at port `5005`
6. Create a pull request and describe your changes! <3

## 🌐 Localization & Internationalization
Please only submit localizations if you speak and/or write the language you are translating to.
We want to keep these translations correct and high quality, running the strings through Google Translate or DeepL is not acceptable.
Thank you for understanding

In the early days of the bot, we had a pretty dis-organized json file system for translated strings.
This was messy and somewhat confusing. Since the 2.0 update, we now utilize properties files

1. The base english locale file is located at `/src/main/resources/locale/values.properties`
2. Files are named `values_{lang-code}.properties`. For example, the Spanish locale file would be `values_es.properties`
3. Translate the strings and submit it back to us (either via Discord, or a pull request to this repo)

> **NOTE**: Variables use `{N}` where `N` is the zero-indexed order it is passed through in code.
>
> In English, these are always in order `0, 1, 2... 5`.
