package org.dreamexposure.discal.core.business

import discord4j.core.`object`.component.*
import discord4j.core.`object`.emoji.Emoji
import org.dreamexposure.discal.core.enums.event.EventColor
import org.dreamexposure.discal.core.extensions.autocompleteSafe
import org.dreamexposure.discal.core.extensions.toMarkdown
import org.dreamexposure.discal.core.`object`.new.*
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.Month

@Component
class ComponentService {

    fun getStaticMessageComponents(): Array<LayoutComponent> {
        val refreshButton = Button.secondary(
            "refresh-static-message",
            Emoji.of(1465798960483668122, "refresh", false)
        )

        return arrayOf(ActionRow.of(refreshButton))
    }

    fun getEventRsvpComponents(event: Event, settings: GuildSettings, alwaysShow: Boolean = false): Array<LayoutComponent> {
        if (!alwaysShow && !settings.showRsvpDropdown) return emptyArray() // This way we don't need the message UI code to get cluttered

        val goingOnTime = SelectMenu.Option.of(getCommonMsg("dropdown.rsvp.option.on-time.label", settings.locale), "rsvp_on_time")
            .withEmoji(Emoji.of(1465796203013734490, "rsvp_on_time", false))

        val goingLate = SelectMenu.Option.of(getCommonMsg("dropdown.rsvp.option.late.label", settings.locale), "rsvp_late")
            .withEmoji(Emoji.of(1465796205635309578, "rsvp_late", false))

        val notGoing = SelectMenu.Option.of(getCommonMsg("dropdown.rsvp.option.not-going.label", settings.locale), "rsvp_not_going")
            .withEmoji(Emoji.of(1465796207564554485, "rsvp_not_going", false))

        val undecided = SelectMenu.Option.of(getCommonMsg("dropdown.rsvp.option.undecided.label", settings.locale), "rsvp_undecided")
            .withEmoji(Emoji.of(1465796206566309888, "rsvp_undecided", false))

        // So, I checked the DBs and there seem to be no event IDs longer than 75 characters, so I'm just gonna not worry until it becomes a problem
        val selectMenu = SelectMenu.of("rsvp|${event.calendarNumber}|${event.id}", goingOnTime, goingLate, notGoing, undecided)
            .withPlaceholder(getCommonMsg("dropdown.rsvp.placeholder", settings.locale))

        return arrayOf(ActionRow.of(selectMenu))
    }

    fun <T> getWizardComponents(wizard: WizardState<T>, settings: GuildSettings): Array<LayoutComponent> {
        val wizardType = when (wizard) {
            is CalendarWizardState -> "calendar"
            is EventWizardState -> "event"
            is AnnouncementWizardState -> "announcement"
            else -> throw UnsupportedOperationException("Unexpected wizard type")
        }

        val confirmButtonTitle =
            if (wizard.editing) "button.wizard.confirm.edit.label"
            else "button.wizard.confirm.create.label"
        val confirmButton = Button.success(
            "wizard-confirm-$wizardType",
            Emoji.unicode("\u2714\ufe0f"),
            getCommonMsg(confirmButtonTitle, settings.locale)
        )

        val cancelButton = Button.secondary(
            "wizard-cancel-$wizardType",
            Emoji.unicode("\u274c"),
            getCommonMsg("button.wizard.cancel.label", settings.locale)
        )

        return arrayOf(ActionRow.of(confirmButton, cancelButton))
    }

    fun getEventCreateModalComponents(settings: GuildSettings, calendars: List<Calendar>): Array<LayoutComponent> {
        val nameInput = TextInput.small("event.create.name")
            .placeholder(getCommonMsg("modal.event.create.name.placeholder", settings.locale))
            .required(false)

        val descriptionInput = TextInput.paragraph("event.create.description")
            .placeholder(getCommonMsg("modal.event.create.description.placeholder", settings.locale))
            .required(false)

        val locationInput = TextInput.small("event.create.location")
            .placeholder(getCommonMsg("modal.event.create.location.placeholder", settings.locale))
            .required(false)

        // Generate color options
        val colorOptions = EventColor.entries.map {
            SelectMenu.Option.of(it.name, it.name)
                .withEmoji(it.emoji)
                .withDefault(it == EventColor.NONE)
        }
        val colorSelect = SelectMenu.of("event.create.color", colorOptions)
            .withPlaceholder(getCommonMsg("modal.event.create.color.placeholder", settings.locale))
            .required(false)

        // Generate calendar options
        val calendarOptions = calendars.subList(0, 25.coerceAtMost(calendars.size)).map {
            SelectMenu.Option.of("[${it.metadata.number}] ${it.name.toMarkdown().autocompleteSafe(6)}", it.metadata.number.toString())
                .withDefault(it.metadata.number == 1)
        }
        val calendarSelect = SelectMenu.of("event.create.calendar", calendarOptions)
            .withPlaceholder(getCommonMsg("modal.event.create.calendar.placeholder", settings.locale))
            .required(true)


        return arrayOf(
            Label.of(getCommonMsg("modal.event.create.name.label", settings.locale), nameInput),
            Label.of(getCommonMsg("modal.event.create.description.label", settings.locale), descriptionInput),
            Label.of(getCommonMsg("modal.event.create.location.label", settings.locale), locationInput),
            Label.of(getCommonMsg("modal.event.create.color.label", settings.locale), colorSelect),
            Label.of(getCommonMsg("modal.event.create.calendar.label", settings.locale), calendarSelect),
        )
    }

    fun getEventRecurrenceWeeklyModalComponents(settings: GuildSettings, event: Event.PartialEvent): Array<LayoutComponent> {
        // Determine pre-selected days
        val selectedDays = emptyList<DayOfWeek>().toMutableList()
        event.recurrence?.byDay?.forEach { selectedDays.add(it.dayOfWeek) }

        if (selectedDays.isEmpty() && event.start != null) selectedDays.add(event.start.atZone(event.timezone).dayOfWeek)

        // Generate the list of days able to be selected
        val dayOptions = DayOfWeek.entries.map { day ->
            SelectMenu.Option.of(day.name, day.name)
                .withDefault(selectedDays.contains(day))
        }

        val select = SelectMenu.of("select.event.recurrence.days", dayOptions)
            .withMinValues(1)
            .withMaxValues(dayOptions.size)
            .required(true)

       return arrayOf(Label.of(getCommonMsg("select.event.recurrence.days.label", settings.locale), select))
    }

    fun getEventRecurrenceMonthlyDateModalComponents(settings: GuildSettings, event: Event.PartialEvent): Array<LayoutComponent> {
        // determine if there should be prefilled date
        val prefilledDayNumber = event.start?.atZone(event.timezone)?.dayOfMonth

        val dateInput = TextInput.small("event-recurrence-monthly-date", 1, 2)
            .placeholder(getCommonMsg("modal.event.recurrence.monthly.date.placeholder", settings.locale))
            .required(true)
            .prefilled(prefilledDayNumber?.toString() ?: "")

        return arrayOf(Label.of(getCommonMsg("modal.event.recurrence.monthly.date.label", settings.locale), dateInput))
    }

    fun getEventRecurrenceMonthlyVariableModalComponents(settings: GuildSettings, event: Event.PartialEvent): Array<LayoutComponent> {
        // Generate the list of day positions
        val dayPositions = EventRecurrence.SetPos.entries.map {
            SelectMenu.Option.of(it.name, it.name)
                .withDefault(it == EventRecurrence.SetPos.FIRST)
        }
        val selectPosition = SelectMenu.of("select.event.recurrence.position", dayPositions)
            .withPlaceholder(getCommonMsg("modal.event.recurrence.monthly.position.placeholder", settings.locale))
            .required(true)


        // Determine pre-selected day
        val selectedDay = event.start?.atZone(event.timezone)?.dayOfWeek

        // Generate the list of days able to be selected
        val dayOptions = DayOfWeek.entries.map { day ->
            SelectMenu.Option.of(day.name, day.name)
                .withDefault(selectedDay == day)
        }
        val selectDay = SelectMenu.of("select.event.recurrence.day", dayOptions)
            .withPlaceholder(getCommonMsg("modal.event.recurrence.monthly.day.placeholder", settings.locale))
            .required(true)

        return arrayOf(
            Label.of(getCommonMsg("modal.event.recurrence.monthly.position.label", settings.locale), selectPosition),
            Label.of(getCommonMsg("modal.event.recurrence.monthly.day.label", settings.locale), selectDay),
        )
    }

    fun getEventRecurrenceYearlyDateModalComponents(settings: GuildSettings, event: Event.PartialEvent): Array<LayoutComponent> {
        // Determine if there should be a prefilled date
        val prefilledMonth = event.start?.atZone(event.timezone)?.month
        val prefilledDate = event.start?.atZone(event.timezone)?.dayOfMonth

        // Generate list of months
        val monthOptions = Month.entries.map { month ->
            SelectMenu.Option.of(month.name, month.name)
                .withDefault(month == prefilledMonth)
        }
        val selectMonth = SelectMenu.of("select.event.recurrence.month", monthOptions)
            .withPlaceholder(getCommonMsg("modal.event.recurrence.yearly.date.month.placeholder", settings.locale))
            .required(true)

        val dateInput = TextInput.small("event-recurrence-yearly-date", 1, 2)
            .placeholder(getCommonMsg("modal.event.recurrence.yearly.date.date.placeholder", settings.locale))
            .required(true)
            .prefilled(prefilledDate?.toString() ?: "")

        return arrayOf(
            Label.of(getCommonMsg("modal.event.recurrence.yearly.date.month.label", settings.locale), selectMonth),
            Label.of(getCommonMsg("modal.event.recurrence.yearly.date.date.label", settings.locale), dateInput),
        )
    }

    fun getEventRecurrenceYearlyVariableModalComponents(settings: GuildSettings, event: Event.PartialEvent): Array<LayoutComponent> {
        // Determine preselected day and month
        val selectedDay = event.start?.atZone(event.timezone)?.dayOfWeek
        val prefilledMonth = event.start?.atZone(event.timezone)?.month


        // Generate the list of day positions
        val dayPositions = EventRecurrence.SetPos.entries.map {
            SelectMenu.Option.of(it.name, it.name)
                .withDefault(it == EventRecurrence.SetPos.FIRST)
        }
        val selectPosition = SelectMenu.of("select.event.recurrence.position", dayPositions)
            .withPlaceholder(getCommonMsg("modal.event.recurrence.yearly.variable.position.placeholder", settings.locale))
            .required(true)

        // Generate the list of days able to be selected
        val dayOptions = DayOfWeek.entries.map { day ->
            SelectMenu.Option.of(day.name, day.name)
                .withDefault(selectedDay == day)
        }
        val selectDay = SelectMenu.of("select.event.recurrence.day", dayOptions)
            .withPlaceholder(getCommonMsg("modal.event.recurrence.yearly.variable.day.placeholder", settings.locale))
            .required(true)

        // Generate list of months
        val monthOptions = Month.entries.map { month ->
            SelectMenu.Option.of(month.name, month.name)
                .withDefault(month == prefilledMonth)
        }
        val selectMonth = SelectMenu.of("select.event.recurrence.month", monthOptions)
            .withPlaceholder(getCommonMsg("modal.event.recurrence.yearly.variable.month.placeholder", settings.locale))
            .required(true)


        return arrayOf(
            Label.of(getCommonMsg("modal.event.recurrence.yearly.variable.position.label", settings.locale), selectPosition),
            Label.of(getCommonMsg("modal.event.recurrence.yearly.variable.day.label", settings.locale), selectDay),
            Label.of(getCommonMsg("modal.event.recurrence.yearly.variable.month.label", settings.locale), selectMonth),
        )
    }

    fun getEventRecurrenceMonthlyDropdownComponents(settings: GuildSettings): Array<LayoutComponent> {
        // dropdown with wizard to ask "on specific day of month (ex 15th)", or "Nth day of month (ex first Tuesday)"
        val dateOption = SelectMenu.Option.of(getCommonMsg("select.event.recurrence.month.option.date.label", settings.locale), "monthly_date")
            .withDescription(getCommonMsg("select.event.recurrence.month.option.date.description", settings.locale))
            .withDefault(true) // Default behavior of monthly recurrence without additional rules
        val variableOption = SelectMenu.Option.of(getCommonMsg("select.event.recurrence.month.option.variable.label", settings.locale), "monthly_variable")
            .withDescription(getCommonMsg("select.event.recurrence.month.option.variable.description", settings.locale))

        val selectMenu = SelectMenu.of("select.event.recurrence.month-option", dateOption, variableOption)
            .withPlaceholder(getCommonMsg("select.event.recurrence.month.placeholder", settings.locale))

        return arrayOf(ActionRow.of(selectMenu))
    }

    fun getEventRecurrenceYearlyDropdownComponents(settings: GuildSettings): Array<LayoutComponent> {
        // dropdown with wizard to ask "on specific date or nth day of x month:
        val dateOption = SelectMenu.Option.of(getCommonMsg("select.event.recurrence.yearly.option.date.label", settings.locale), "yearly_date")
            .withDescription(getCommonMsg("select.event.recurrence.yearly.option.date.description", settings.locale))
            .withDefault(true) // Default behavior of yearly recurrence without additional rules
        val variableOption = SelectMenu.Option.of(getCommonMsg("select.event.recurrence.yearly.option.variable.label", settings.locale), "yearly_variable")
            .withDescription(getCommonMsg("select.event.recurrence.yearly.option.variable.description", settings.locale))

        val selectMenu = SelectMenu.of("select.event.recurrence.yearly-option", dateOption, variableOption)
            .withPlaceholder(getCommonMsg("select.event.recurrence.yearly.placeholder", settings.locale))

        return arrayOf(ActionRow.of(selectMenu))
    }
}
