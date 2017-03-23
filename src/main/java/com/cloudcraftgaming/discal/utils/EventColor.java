package com.cloudcraftgaming.discal.utils;

import java.util.ArrayList;

/**
 * Created by Nova Fox on 3/22/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public enum EventColor {
    MELROSE(1, "A4BDFC"), RIPTIDE(2, "7AE7BF"), MAUVE(3, "DBADFF"), TANGERINE(4, "FF887C"), DANDELION(5, "FBD75B"),
    MAC_AND_CHEESE(6, "FFB878"), TURQUOISE(7, "46D6DB"), MERCURY(8, "E1E1E1"), BLUE(9, "5484ED"), GREEN(10, "51B749"),
    RED(11, "DC2127");

    private final Integer id;
    private final String hex;

    EventColor(Integer _id, String _hex) {
        id = _id;
        hex = _hex;
    }


    public Integer getId() {
        return id;
    }

    public String getHex() {
        return hex;
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