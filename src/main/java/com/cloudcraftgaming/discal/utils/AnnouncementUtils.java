package com.cloudcraftgaming.discal.utils;

import com.cloudcraftgaming.discal.database.DatabaseManager;
import com.cloudcraftgaming.discal.module.announcement.Announcement;
import com.cloudcraftgaming.discal.module.announcement.AnnouncementType;
import com.google.api.services.calendar.model.Event;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * Created by Nova Fox on 4/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class AnnouncementUtils {
    /**
     * Checks if the announcement exists.
     * @param value The announcement ID.
     * @param event The event received.
     * @return <code>true</code> if the announcement exists, else <code>false</code>.
     */
    public static Boolean announcementExists(String value, MessageReceivedEvent event) {
        for (Announcement a : DatabaseManager.getManager().getAnnouncements(event.getGuild().getLongID())) {
            if (a.getAnnouncementId().toString().equals(value)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isCompatible(Announcement a, Event e) {
    	if (a.getAnnouncementType().equals(AnnouncementType.UNIVERSAL)) {
    		return true;
	    } else if (a.getAnnouncementType().equals(AnnouncementType.SPECIFIC)) {
    		if (e.getId().equals(a.getEventId())) {
    			return true;
		    }
	    } else if (a.getAnnouncementType().equals(AnnouncementType.COLOR)) {
    		EventColor ec = EventColor.fromNameOrHexOrID(e.getId());
    		if (!a.getEventColor().equals(EventColor.NONE)) {
    			if (a.getEventColor().equals(ec)) {
    				return true;
			    }
		    }
	    } else if (a.getAnnouncementType().equals(AnnouncementType.RECUR)) {
    		if (e.getId().contains("_")) {
    			String recurId = e.getId().split("_")[0];
    			if (recurId.equals(a.getEventId())) {
    				return true;
			    }
		    }
	    }
	    return false;
    }
}