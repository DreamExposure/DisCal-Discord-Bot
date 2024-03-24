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

<details>
<summary>How permissions are handled</summary>

DisCal uses a simple-to-understand permission scheme for handling access to commands.
- **Elevated**
  - Requires `ADMINISTRATOR` or `MANAGE_SERVER` permission nodes, or being the guild owner
- **Privileged**
  -  Requires DisCal control role (default control role is `@everyone`)
- **Everyone**
  - Everyone will always be able to access (unless commands are disabled for the channel)
- **Patron-Only**
  - Requires guild to be a patron-guild at the early access tier or higher
- **Dev-Only**
  - Only DisCal Developers are able to use these commands
</details>

<details>
<summary>Calendar Commands (/calendar)</summary>

| Command                 | Description                            | Permissions |
|-------------------------|----------------------------------------|-------------|
| `/calendar create`      | Starts the calendar creation wizard    | elevated    |
| `/calendar name`        | Sets the calendar's name               | elevated    |
| `/calendar description` | Sets the calendar's description        | elevated    |
| `/calendar timezone`    | Sets the calendar's timezone           | elevated    |
| `/calendar review`      | Displays the calendar's properties     | elevated    |
| `/calendar confirm`     | Commits the changes made in the wizard | elevated    |
| `/calendar cancel`      | Cancels the wizard                     | elevated    |
| `/calendar delete`      | Deletes the calendar                   | elevated    |
| `/calendar edit`        | Starts the calendar edit wizard        | elevated    |
</details>

<details>
<summary>Displaycal Commands (/displaycal)</summary>

| Command              | Description                                           | Permissions |
|----------------------|-------------------------------------------------------|-------------|
| `/displaycal new`    | Creates a new auto-updating calendar overview message | elevated    |
</details>

<details>
<summary>Event Commands (/event)</summary>

| Command              | Description                                         | Permissions                         |
|----------------------|-----------------------------------------------------|-------------------------------------|
| `/event view`        | Displays the event's details                        | everyone                            |
| `/event create`      | Starts the event creation wizard                    | privileged                          |
| `/event name`        | Sets the event's name                               | privileged                          |
| `/event description` | Sets the event's description                        | privileged                          |
| `/event start`       | Sets the event's start                              | privileged                          |
| `/event end`         | Sets the event's end                                | privileged                          |
| `/event color`       | Sets the event's color                              | privileged                          |
| `/event location`    | Sets the event's location                           | privileged                          |
| `/event image`       | Sets the event's image                              | privileged, gif support patron-only |
| `/event recur`       | Toggles whether the event recurs, and how it recurs | privileged                          |
| `/event review`      | Displays the event's properties                     | privileged                          |
| `/event confirm`     | Commits the changes made in the wizard              | privileged                          |
| `/event cancel`      | Cancels the wizard                                  | privileged                          |
| `/event edit`        | Starts the event edit wizard                        | privileged                          |
| `/event copy`        | Copies an existing event's details to a new event   | privileged                          |
| `/event delete`      | Deletes an event                                    | privileged                          |
</details>

<details>
<summary>Events Commands (/events)</summary>

| Command            | Description                                       | Permissions |
|--------------------|---------------------------------------------------|-------------|
| `/events upcoming` | Lists the next X upcoming events                  | everyone    |
| `/events ongoing`  | Lists the ongoing events                          | everyone    |
| `/events today`    | Lists the events occurring in the next 24 hours   | everyone    |
| `/events range`    | Lists the events found in the date range provided | everyone    |
</details>

<details>
<summary>RSVP Commands (/rsvp)</summary>

| Command        | Description                                                                                                                          | Permissions           |
|----------------|--------------------------------------------------------------------------------------------------------------------------------------|-----------------------|
| `/rsvp ontime` | RSVPs as going to the event on time                                                                                                  | everyone              |
| `/rsvp late`   | RSVPs as going to the event, but arriving late                                                                                       | everyone              |
| `/rsvp not`    | RSVPs as not going to the event                                                                                                      | everyone              |
| `/rsvp unsure` | RSVPs as unsure if you will be able to attend                                                                                        | everyone              |
| `/rsvp remove` | Removes your RSVP status from the event                                                                                              | everyone              |
| `/rsvp list`   | Lists who has RSVPed to the event                                                                                                    | everyone              |
| `/rsvp limit`  | Sets the max number of people allowed to attend. `-1` to disable the limit                                                           | privileged            |
| `/rsvp role`   | Sets the role assigned when RSVP'd to the event. `@everyone` to disable. *NOTE:* These roles are currently not automatically removed | elevated, patron-only |
</details>

<details>
<summary>Announcement Commands (/announcement)</summary>

| Command                     | Description                                                                         | Permissions             |
|-----------------------------|-------------------------------------------------------------------------------------|-------------------------|
| `/announcement create`      | Starts the announcement create wizard                                               | privileged              |
| `/announcement type`        | Sets the announcement type. Valid types: UNIVERSAL, SPECIFIC, COLOR, RECUR          | privileged              |
| `/announcement event`       | Sets the announcement's event. Only needed when using SPECIFIC or RECUR types       | privileged              |
| `/announcement color`       | Sets the announcement's color. Only needed when using COLOR type                    | privileged              |
| `/announcement channel`     | Sets the channel the announcement will be posted in                                 | privileged              |
| `/announcement minutes`     | Sets the minutes before an event to announce. Added to hours                        | privileged              |
| `/announcement hours`       | Sets the hours before an event to announce. Added to minutes                        | privileged              |
| `/announcement info`        | Sets the additional info to be posted along with the event. No text input to remove | privileged              |
| `/announcement calendar`    | Sets the calendar the announcement will read from. Defaults to 1 (main calendar)    | privileged              |
| `/announcement publish`     | Toggles if the announcement should be pushed to channel subscribers                 | privileged, patron-only |
| `/announcement review`      | Displays the announcement properties in the wizard	                                 | privileged              |
| `/announcement confirm`     | Commits the changes made in the wizard	                                             | privileged              |
| `/announcement cancel`      | Cancels the announcement wizard                                                     | privileged              |
| `/announcement edit`        | Starts the announcement edit wizard                                                 | privileged              |
| `/announcement copy`        | Copies an existing announcement to a new one                                        | privileged              |
| `/announcement delete`      | Deletes an announcement                                                             | privileged              |
| `/announcement enable`      | Sets whether an announcement is enabled                                             | privileged              |
| `/announcement view`        | Displays an existing announcement's properties	                                     | everyone                |
| `/announcement list`        | Lists announcements, -1 for all                                                     | everyone                |
| `/announcement subscribe`   | Subscribes to an announcement to be pinged when it is posted                        | everyone                |
| `/announcement unsubscribe` | Unsubscribes to an announcement, to stop being pinged when it is posted             | everyone                |
</details>

<details>
<summary>Settings Commands (/settings)</summary>

| Command                        | Description                                                              | Permissions           |
|--------------------------------|--------------------------------------------------------------------------|-----------------------|
| `/settings view`               | Displays the current settings for the guild                              | elevated              |
| `/settings role`               | Sets the role required to use privileged commands                        | elevated              |
| `/settings announcement-style` | Changes the style announcements will be posted as                        | elevated              |
| `/settings language`           | Changes the language the bot will use in responses                       | elevated              |
| `/settings time-format`        | Changes what format to display date/time when needed                     | elevated              |
| `/settings branding`           | Toggles between DisCal branding or the guild's name/image where possible | elevated, patron-only |
</details>

<details>
<summary>All Other Commands</summary>

| Command         | Description                                                  | Permissions           |
|-----------------|--------------------------------------------------------------|-----------------------|
| `/discal`       | Displays information about the bot                           | everyone              |
| `/linkcal`      | Provides info and a link to view the guild's calendar        | everyone              |
| `/time`         | Displays the current time as seen by the calendar's timezone | everyone              |
| [WIP] `/addcal` | Starts the process to add a pre-existing calendar            | patron-only, dev-only |
| `help`          | Links to the commands page and documentation                 | everyone              |
</details>


# 🗓️ Planned & Work In Progress
This bot is a hobby project for me, please note that while these features are planned, there's no solid timeline.
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
