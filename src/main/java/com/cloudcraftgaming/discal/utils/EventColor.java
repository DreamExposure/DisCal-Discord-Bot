package com.cloudcraftgaming.discal.utils;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 3/22/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public enum EventColor {
    MELROSE(1, "A4BDFC", 164, 189, 252), RIPTIDE(2, "7AE7BF", 122, 231, 191),
    MAUVE(3, "DBADFF", 219, 173, 255), TANGERINE(4, "FF887C", 255, 136, 124),
    DANDELION(5, "FBD75B", 251, 215, 91), MAC_AND_CHEESE(6, "FFB878", 255, 184, 120),
    TURQUOISE(7, "46D6DB", 70, 214, 219), MERCURY(8, "E1E1E1", 255, 255, 255),
    BLUE(9, "5484ED", 84, 132, 237), GREEN(10, "51B749", 81, 183, 73),
    RED(11, "DC2127", 220, 33, 39);

    private final Integer id;
    private final String hex;

    //TOOD: Add RGB getters
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


    public Integer getId() {
        return id;
    }

    public String getHex() {
        return hex;
    }

    public Integer getR() {
        return r;
    }

    public Integer getG() {
        return g;
    }

    public Integer getB() {
        return b;
    }

    //Static methods
    public static ArrayList<EventColor> getAllColors() {
        ArrayList<EventColor> colors = new ArrayList<>();
        colors.add(MELROSE);
        colors.add(RIPTIDE);
        colors.add(MAUVE);
        colors.add(TANGERINE);
        colors.add(DANDELION);
        colors.add(MAC_AND_CHEESE);
        colors.add(TURQUOISE);
        colors.add(MERCURY);
        colors.add(BLUE);
        colors.add(GREEN);
        colors.add(RED);
        return colors;
    }

    public static boolean exists(String nameOrHex) {
        for (EventColor c : getAllColors()) {
            if (c.name().equalsIgnoreCase(nameOrHex) || c.getHex().equals(nameOrHex)) {
                return true;
            }
        }
        return false;
    }

    public static boolean exists(Integer id) {
        for (EventColor c : getAllColors()) {
            if (c.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static EventColor fromNameOfHex(String nameOrHex) {
        for (EventColor c : getAllColors()) {
            if (c.name().equalsIgnoreCase(nameOrHex) || c.getHex().equals(nameOrHex)) {
                return c;
            }
        }
        return RED;
    }

    public static EventColor fromId(Integer id) {
        for (EventColor c : getAllColors()) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return RED;
    }
}