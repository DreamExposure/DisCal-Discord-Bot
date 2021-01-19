package org.dreamexposure.discal.core.utils;

import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.Recurrence;
import org.json.JSONObject;

public class JsonUtils {
    public static String getJsonResponseMessage(final String msg) {
        return "{\"message\": \"" + msg + "\"}";
    }

    //TODO: rewrite to non-blocking
    public static JSONObject convertEventToJson(final Event event, final GuildSettings settings) {
        final JSONObject json = new JSONObject();

        json.put("event_id", event.getId());
        json.put("epoch_start", event.getStart().getDateTime().getValue());
        json.put("epoch_end", event.getEnd().getDateTime().getValue());

        //These 3 are optional values
        if (event.getSummary() != null)
            json.put("summary", event.getSummary());
        if (event.getDescription() != null)
            json.put("description", event.getDescription());
        if (event.getLocation() != null)
            json.put("location", event.getLocation());

        json.put("is_parent", !(event.getId().contains("_")));

        json.put("color", EventColor.Companion.fromNameOrHexOrId(event.getColorId()).name());

        if (event.getRecurrence() != null && !event.getRecurrence().isEmpty()) {
            json.put("recur", true);
            final Recurrence r = Recurrence.Companion.fromRRule(event.getRecurrence().get(0));

            final JSONObject recurrence = new JSONObject();
            recurrence.put("frequency", r.getFrequency().name());
            recurrence.put("count", r.getCount());
            recurrence.put("interval", r.getInterval());

            json.put("recurrence", recurrence); //Optional
        } else
            json.put("recur", false);

        final EventData ed = DatabaseManager.getEventData(settings.getGuildID(), event.getId()).block();

        //Event image is also optional
        if (ed != null && ed.getImageLink() != null)
            json.put("image", ed.getImageLink());

        return json;
    }
}
