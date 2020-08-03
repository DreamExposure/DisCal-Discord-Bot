package org.dreamexposure.discal.core.enums.event;

import discord4j.rest.util.Color;

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

    EventColor(final Integer _id, final String _hex, final Integer _r, final Integer _g, final Integer _b) {
        this.id = _id;
        this.hex = _hex;

        this.r = _r;
        this.b = _b;
        this.g = _g;
    }


    public int getId() {
        return this.id;
    }

    public String getHex() {
        return this.hex;
    }

    public int getR() {
        return this.r;
    }

    public int getG() {
        return this.g;
    }

    public int getB() {
        return this.b;
    }

    public Color asColor() {
        return Color.of(this.r, this.g, this.b);
    }

    //Static methods
    public static boolean exists(final String nameOrHexOrId) {
        for (final EventColor c : values()) {
            if (c.name().equalsIgnoreCase(nameOrHexOrId) || c.getHex().equals(nameOrHexOrId)) {
                return true;
            } else {
                try {
                    final int i = Integer.parseInt(nameOrHexOrId);
                    if (c.getId() == i)
                        return true;
                } catch (final NumberFormatException e) {
                    //Not number, just ignore.
                }
            }
        }
        return false;
    }

    public static boolean exists(final Integer id) {
        for (final EventColor c : values()) {
            if (c.getId() == id)
                return true;
        }
        return false;
    }

    public static EventColor fromNameOrHexOrID(final String nameOrHexOrID) {
        for (final EventColor c : values()) {
            if (c.name().equalsIgnoreCase(nameOrHexOrID) || c.getHex().equals(nameOrHexOrID)) {
                return c;
            } else {
                try {
                    final int i = Integer.parseInt(nameOrHexOrID);
                    if (c.getId() == i)
                        return c;
                } catch (final NumberFormatException e) {
                    //Not number, just ignore.
                }
            }
        }
        return NONE;
    }

    public static EventColor fromId(final Integer id) {
        for (final EventColor c : values()) {
            if (c.getId() == id)
                return c;
        }
        return NONE;
    }
}