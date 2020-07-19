# DisCal Discord Bot

[![Discord](https://img.shields.io/discord/375357265198317579?label=DreamExposure&style=flat-square)](https://discord.gg/2TFqyuy)
[![Jenkins](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.dreamexposure.org%2Fview%2FDiscord-Bots%2Fjob%2FDisCal%2F&style=flat-square)](https://jenkins.dreamexposure.org/job/DisCal/)
[![Jenkins tests](https://img.shields.io/jenkins/tests?compact_message&jobUrl=https%3A%2F%2Fjenkins.dreamexposure.org%2Fview%2FDiscord-Bots%2Fjob%2FDisCal%2F&style=flat-square)](https://jenkins.dreamexposure.org/job/DisCal/tests)

DisCal is a discord bot that connects Discord and Google Calendar as seamlessly as possible with a wide feature set for calendar management and information.

# Core Features

* Powerful in-server integration of google calendar
* Custom Calendar creation and editing
* Event creation, editing, and deletion
* Automated announcement system to remind users of events
* Customizable prefix and mentionable commands
* Versatile and built for all communities
* Web dashboard for bot and calendar management

## Planned Features & Work in Progress Changes

* Multiple calendars per server (WIP)
* Advanced announcement configuration (WIP)
* Complex recurring event configuration (WIP)
* Proper patreon integration for automated setup.
* Better translation support (Right now using the JSON files is really messy and hard to maintain)
* And so much more!

# Quick Links

* [Website](https://www.discalbot.com)
* [Discord](https://discord.gg/2TFqyuy)
* [Trello](https://trello.com/b/2Xx3Galz)

# Technologies

DisCal is primarily written in Java with a TypeScript powered web-frontend.
We use the following technologies throughout the project:
* [Discord4J](https://github.com/Discord4J/Discord4J) API wrapper
* Project Reactor for fully reactive code
* SpringBoot web backend
* MySQL with Redis caching

# Developer RESTful API

DisCal was written for the community, and to aid in that goal, DisCal has a fully functioning REST API to allow developers to bring their applications to DisCal.

Current API Version: v2

To get an API token, please contact the development team.
* [API Docs](https://www.discalbot.com/docs/api/overview)

# Contributing

DisCal is an open source project and is maintained in our free time. We always welcome and love contributions.

## Code

1. Fork this repo and make changes in your own copy
2. Write your code and add any new tests if applicable
3. Run the new and/or existing tests with `mvn clean test` to make sure they pass
4. Commit your changes and push to your fork `git push origin master`
5. Create a new pull request and submit it back to us!

## Translations

As a calendar bot, DisCal reaches global use, and because of this , Discal allows users to select the language they wish to use.

If you are able to speak two languages (English and another language) we welcome you to translate the bot to that language!

For instructions on how to translate DisCal, continue reading below.

Do note however, that we are planning on making the translation system much more powerful and easier to maintain and contribute to in the future.

### Translation Instructions

Follow the format provided in the [TEMPLATE LANGUAGE JSON File.](client/src/main/resources/languages/TEMPLATE.json) and submit a new translation by making adding a new JSON file with the naming scheme `LANG_NAME.json` (Where "LANG_NAME" is the name of the language you translated it to).

### Translation Guidelines

We very much appreciate all of the translations we can get, but please follow the guidelines below so that we may ensure that we have high quality translations.

1. Please be fluent in the language you are translating to. We will ***NOT*** accept any translations that are made through Google Translate as it cannot tell statement context and we want to maintain high standards for the bot.
2. The general JSON format is `"key": "value"`, which means that the "key" must remain the same in all files, so ***DO NOT*** change the key, only the values.
3. In many files `%value%` or something similar is seen. These are replacement variables and will never be seen by the user. ***DO NOT CHANGE THE REPLACERS!*** 
4. At the beginning of the file, you will see `"Language: "LANG_NAME_HERE"` Please change `LANG_NAME_HERE` to the language you are translating the file to!
5. Please run your translated language file through this [Online JSON Parser](http://jsonparseronline.com) If you cannot do that, the development team will.
6. Contact the development team in our Discord guild for any additional help or info.