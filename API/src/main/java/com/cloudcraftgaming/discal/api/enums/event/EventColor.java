package com.cloudcraftgaming.discal.api.enums.event;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("SpellCheckingInspection")
public enum EventColor {
	MELROSE(1, "A4BDFC", 164, 189, 252), RIPTIDE(2, "7AE7BF", 122, 231, 191),
	MAUVE(3, "DBADFF", 219, 173, 255), TANGERINE(4, "FF887C", 255, 136, 124),
	DANDELION(5, "FBD75B", 251, 215, 91), MAC_AND_CHEESE(6, "FFB878", 255, 184, 120),
	TURQUOISE(7, "46D6DB", 70, 214, 219), MERCURY(8, "E1E1E1", 255, 255, 255),
	BLUE(9, "5484ED", 84, 132, 237), GREEN(10, "51B749", 81, 183, 73),
	RED(11, "DC2127", 220, 33, 39), NONE(12, "NONE", 56, 138, 237);

	private final Integer id;
	private final String hex;

	private final Integer r;
	private final Integer g;
	private final Integer b;

	EventColor(Integer _id, String _hex, Integer _r, Integer _g, Integer _b) {
		id = _id;
		hex = _hex;

		r = _r;
		b = _b;
		g = _g;
	}


	public int getId() {
		return id;
	}

	public String getHex() {
		return hex;
	}

	public int getR() {
		return r;
	}

	public int getG() {
		return g;
	}

	public int getB() {
		return b;
	}

	//Static methods
	public static boolean exists(String nameOrHexOrId) {
		for (EventColor c : values()) {
			if (c.name().equalsIgnoreCase(nameOrHexOrId) || c.getHex().equals(nameOrHexOrId)) {
				return true;
			} else {
				try {
					int i = Integer.valueOf(nameOrHexOrId);
					if (c.getId() == i)
						return true;
				} catch (NumberFormatException e) {
					//Not number, just ignore.
				}
			}
		}
		return false;
	}

	public static boolean exists(Integer id) {
		for (EventColor c : values()) {
			if (c.getId() == id)
				return true;
		}
		return false;
	}

	public static EventColor fromNameOrHexOrID(String nameOrHexOrID) {
		for (EventColor c : values()) {
			if (c.name().equalsIgnoreCase(nameOrHexOrID) || c.getHex().equals(nameOrHexOrID)) {
				return c;
			} else {
				try {
					int i = Integer.valueOf(nameOrHexOrID);
					if (c.getId() == i)
						return c;
				} catch (NumberFormatException e) {
					//Not number, just ignore.
				}
			}
		}
		return NONE;
	}

	public static EventColor fromId(Integer id) {
		for (EventColor c : values()) {
			if (c.getId() == id)
				return c;
		}
		return NONE;
	}
}