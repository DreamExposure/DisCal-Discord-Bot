package org.dreamexposure.discal.core.utils;

import com.google.api.services.calendar.model.Event;

import org.dreamexposure.discal.core.database.DatabaseManager;
import org.dreamexposure.discal.core.enums.event.EventColor;
import org.dreamexposure.discal.core.object.GuildSettings;
import org.dreamexposure.discal.core.object.event.EventData;
import org.dreamexposure.discal.core.object.event.Recurrence;
import org.json.JSONObject;

public class JsonUtils {
	public static String getJsonResponseMessage(String msg) {
		return "{\"message\": \"" + msg + "\"}";
	}

	public static JSONObject convertEventToJson(Event event, GuildSettings settings) {
		JSONObject json = new JSONObject();

		json.put("id", event.getId());
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

		json.put("color", EventColor.fromNameOrHexOrID(event.getColorId()).name());

		if (event.getRecurrence() != null && event.getRecurrence().size() > 0) {
			json.put("recur", true);
			Recurrence r = new Recurrence().fromRRule(event.getRecurrence().get(0));

			JSONObject recurrence = new JSONObject();
			recurrence.put("frequency", r.getFrequency().name());
			recurrence.put("count", r.getCount());
			recurrence.put("interval", r.getInterval());

			json.put("recurrence", recurrence); //Optional
		} else
			json.put("recur", false);

		EventData ed = DatabaseManager.getManager()
				.getEventData(settings.getGuildID(), event.getId());

		//Event image is also optional
		if (ed.getImageLink() != null)
			json.put("image", ed.getImageLink());

		return json;
	}
}
