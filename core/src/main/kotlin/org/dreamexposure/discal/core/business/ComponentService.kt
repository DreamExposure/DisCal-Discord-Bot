package org.dreamexposure.discal.core.business

import discord4j.common.util.Snowflake
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.`object`.component.LayoutComponent
import discord4j.core.`object`.component.SelectMenu
import discord4j.core.`object`.emoji.Emoji
import org.dreamexposure.discal.core.`object`.new.*
import org.dreamexposure.discal.core.utils.getCommonMsg
import org.springframework.stereotype.Component

@Component
class ComponentService {

    fun getStaticMessageComponents(): Array<LayoutComponent> {
        val refreshButton = Button.secondary(
            "refresh-static-message",
            Emoji.custom(Snowflake.of(1175580426585247815), "refresh_ts", false)
        )

        return arrayOf(ActionRow.of(refreshButton))
    }

    fun getEventRsvpComponents(event: Event, settings: GuildSettings): Array<LayoutComponent> {
        val goingOnTime = SelectMenu.Option.of(getCommonMsg("dropdown.rsvp.option.on-time.label", settings.locale), "rsvp_on_time")
            .withEmoji(Emoji.custom(Snowflake.of(1390911034708988025), "rsvp_on_time_ts", false))

        val goingLate = SelectMenu.Option.of(getCommonMsg("dropdown.rsvp.option.late.label", settings.locale), "rsvp_late")
            .withEmoji(Emoji.custom(Snowflake.of(1390911037074833489), "rsvp_late_ts", false))

        val notGoing = SelectMenu.Option.of(getCommonMsg("dropdown.rsvp.option.not-going.label", settings.locale), "rsvp_not_going")
            .withEmoji(Emoji.custom(Snowflake.of(1390911044515397733), "rsvp_not_going_ts", false))

        val undecided = SelectMenu.Option.of(getCommonMsg("dropdown.rsvp.option.undecided.label", settings.locale), "rsvp_undecided")
            .withEmoji(Emoji.custom(Snowflake.of(1390911039331237968), "rsvp_undecided", false))

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
            else -> throw NotImplementedError("Unexpected wizard type")
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
}
