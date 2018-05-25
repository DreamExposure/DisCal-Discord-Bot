package com.cloudcraftgaming.discal.api.enums.announcement;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public enum AnnouncementType {
	UNIVERSAL, SPECIFIC, COLOR, RECUR;

	/**
	 * Checks if the specified value is a valid AnnouncementType.
	 *
	 * @param _value The value to check.
	 * @return <code>true</code> if value, otherwise <code>false</code>.
	 */
	public static Boolean isValid(String _value) {
		return _value.equalsIgnoreCase("UNIVERSAL") || _value.equalsIgnoreCase("SPECIFIC") || _value.equalsIgnoreCase("COLOR") || _value.equalsIgnoreCase("COLOUR") || _value.equalsIgnoreCase("RECUR");
	}

	/**
	 * Gets the AnnouncementType from the value.
	 *
	 * @param _value The value to check.
	 * @return The AnnouncementType.
	 */
	public static AnnouncementType fromValue(String _value) {
		switch (_value.toUpperCase()) {
			case "UNIVERSAL":
				return UNIVERSAL;
			case "SPECIFIC":
				return SPECIFIC;
			case "COLOR":
				return COLOR;
			case "COLOUR":
				return COLOR;
			case "RECUR":
				return RECUR;
			default:
				return UNIVERSAL;
		}
	}
}