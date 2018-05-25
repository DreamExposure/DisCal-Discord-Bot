package com.cloudcraftgaming.discal.api.object.announcement;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class AnnouncementCreatorResponse {
	private final Boolean successful;

	private Announcement announcement;

	/**
	 * Creates an AnnouncementCreatorResponse.
	 *
	 * @param _successful Whether or not the creator was successful.
	 */
	public AnnouncementCreatorResponse(Boolean _successful) {
		successful = _successful;
	}

	/**
	 * Creates an AnnouncementCreatorResponse.
	 *
	 * @param _successful   Whether or not the creator was successful.
	 * @param _announcement The announcement involved.
	 */
	public AnnouncementCreatorResponse(Boolean _successful, Announcement _announcement) {
		successful = _successful;
		announcement = _announcement;
	}

	//Getters

	/**
	 * Gets whether or not the creator was successful.
	 *
	 * @return Whether or not the creator was successful.
	 */
	public Boolean isSuccessful() {
		return successful;
	}

	/**
	 * Gets the announcement involved.
	 *
	 * @return The Announcement involved.
	 */
	public Announcement getAnnouncement() {
		return announcement;
	}

	//Setters

	/**
	 * Sets the announcement involved.
	 *
	 * @param _announcement The Announcement involved.
	 */
	public void setAnnouncement(Announcement _announcement) {
		announcement = _announcement;
	}
}