package org.dreamexposure.discal.core.utils;

import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.event.Recurrence;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import reactor.core.publisher.Mono;

public class JsonUtils {
    public static String getJsonResponseMessage(final String msg) {
        return "{\"message\": \"" + msg + "\"}";
    }

    public static Mono<JSONObject> convertEventToJson(Event event, ZoneId tz, GuildSettings settings) {
        return Mono.just(new JSONObject()).flatMap(json -> {
            json.put("event_id", event.getId());
            if (event.getStart().getDateTime() != null)
                json.put("epoch_start", event.getStart().getDateTime().getValue());
            else {
                Long start = Instant.ofEpochMilli(event.getStart().getDate().getValue())
                    .plus(1, ChronoUnit.DAYS)
                    .atZone(tz)
                    .truncatedTo(ChronoUnit.DAYS)
                    .toLocalDate()
                    .atStartOfDay()
                    .atZone(tz)
                    .toInstant()
                    .toEpochMilli();

                json.put("epoch_start", start);
            }

            if (event.getEnd().getDateTime() != null)
                json.put("epoch_end", event.getEnd().getDateTime().getValue());
            else {
                Long end = Instant.ofEpochMilli(event.getEnd().getDate().getValue())
                    .plus(1, ChronoUnit.DAYS)
                    .atZone(tz)
                    .truncatedTo(ChronoUnit.DAYS)
                    .toLocalDate()
                    .atStartOfDay()
                    .atZone(tz)
                    .toInstant()
                    .toEpochMilli();

                json.put("epoch_end", end);
            }

            //These 3 are optional values
            if (event.getSummary() != null)
                json.put("summary", event.getSummary());
            if (event.getDescription() != null)
                json.put("description", event.getDescription());
            if (event.getLocation() != null)
                json.put("location", event.getLocation());

            json.put("is_parent", !(event.getId().contains("_")));

            if (event.getColorId() != null)
                json.put("color", EventColor.Companion.fromNameOrHexOrId(event.getColorId()).name());
            else
                json.put("color", EventColor.NONE.name());

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


            return DatabaseManager.getEventData(settings.getGuildID(), event.getId())
                .filter(ed -> !ed.getImageLink().isEmpty())
                .doOnNext(ed -> json.put("image", ed.getImageLink()))
                .thenReturn(json)
                .defaultIfEmpty(json);
        });
    }
}
