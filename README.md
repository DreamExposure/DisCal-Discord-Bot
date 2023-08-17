# DisCal

[![Discord](https://img.shields.io/discord/375357265198317579?label=DreamExposure&style=flat-square)](https://discord.gg/2TFqyuy)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/DreamExposure/DisCal-Discord-Bot/gradle.yml?branch=develop&label=Build&style=flat-square)
[![Website](https://img.shields.io/website?down_color=red&down_message=offline&label=Status&style=flat-square&up_message=online&url=https%3A%2F%2Fwww.discalbot.com)](https://discalbot.com)

DisCal is a discord bot that connects Discord and Google Calendar as seamlessly as possible with a wide feature set for
calendar management and information.

# 🔗 Quick Links

* [Website](https://www.discalbot.com)
* [Discord](https://discord.gg/2TFqyuy)

# 💎 Core Features

* Powerful in-server integration of google calendar
* Custom Calendar creation and editing
* Event creation, editing, and deletion
* Automated announcement system to remind users of events
* Customizable prefix and mentionable commands
* Versatile and built for all communities
* Web dashboard for bot and calendar management

## 🎉 Patron-Only Features

Patrons and supporters on the $5/month plans get access to work in progress and exclusive features.

* External Calendars
    - Use an already existing calendar that is on your Google account with DisCal
* Web Dashboard (WIP)
    - Use the web dashboard to manage the bot, calendar, and more without the need for commands.
    - Still very early in development
* Server Branding
    - Hide the DisCal name in favor of using your server's name on announcements and embeds.
* Announcement Publishing
    - Announcements posted in news channels can be (optionally) automatically "published" so servers following the news
      channel receive them as well!
* Gif support for event images
* Automatically assign roles to users when RSVPing to an event.

## 📝 Planned Features & Work in Progress Changes

* Multiple calendars per server (WIP)
* Advanced announcement configuration (WIP)
* Complex recurring event configuration (WIP)
* Proper patreon integration for automated setup.
* Better translation support (Right now using the JSON files is really messy and hard to maintain)
* And so much more!

# 📦 Modules & Services
* Core
    * The central inner workings shared across other modules
* Server
    * The backend API responsible for network health monitoring and houses the RESTful API
* Client
    * Does all the heavy lifting. This is the discord bot and runs a single shard per instance
* Web
    * The official website. This houses all the frontend code and handles logging in with Discord for the Dashboard
* C.A.M
    * The Central Authentication Manager. This service maintains the credentials for services used by DisCal

# 🧰 Technologies

DisCal is primarily written in Java with a TypeScript powered web-frontend. We use the following technologies throughout
the project:

* [Discord4J](https://github.com/Discord4J/Discord4J) API wrapper
* Project Reactor for fully reactive code
* SpringBoot web backend
* MySQL with Redis caching

# ⚙️ Developer RESTful API

DisCal was written for the community, and to aid in that goal, DisCal has a fully functioning REST API to allow
developers to bring their applications to DisCal.

Current API Version: v2

To get an API token, please contact the development team.

* [API Docs](https://www.discalbot.com/docs/api/overview)

# ✒️ Contributing

DisCal is an open source project and is maintained in our free time. We always welcome and love contributions.

## 📚 Code

1. Fork this repo and make changes in your own copy
2. Write your code and add any new tests if applicable
3. Run the new and/or existing tests with `mvn clean test` to make sure they pass
4. Commit your changes and push to your fork `git push origin master`
5. Create a new pull request and submit it back to us!

## 🗺️ Translations

> This section is a work in progress. Thank you for your understanding

DisCal reaches far and wide, and to help reach more people, we want to support fully localized text throughout the bot
and website. To do that, we use a simple but robust system. If you are fluent in English and another language, we
welcome your help in translating the bot's text. Below are instructions and the conventions we use to keep translations
orderly and working.

We ask that you do not use services like Google Translate as the context of a sentence can be lost or misinterpreted by
software causing confusion for non-english speakers. Thank you.

### 📖 Conventions

* Language files are located in `/core/src/main/resources/i18n/`
* All file names follow the format `name_lang-code.properties`
    - For example, the Spanish common file would use `common_es.properties`
* File contents is formatted as `key=value` where `key` should not be modified
* Variables are input as `{n}` where `n` is the zero-indexed order it is passed through in code.
    - In english, these are always in order `0, 1, 2... 5`. Some languages these may be out of order in order to
      maintain the correct variables in the correct place `1, 0, 3, 2...5`.
    - If the english variant has a variable, the translated version must also have that somewhere in the string.

### ✒️ Adding Translations

1. First fork this repository.
2. Then to translate a file, create a new file in the same folder as the english variant, following our conventions
   above. Then translate each of the value strings from the original english into the new language.
3. Finally, once you have completed your additions, open a pull request and submit it to us!
