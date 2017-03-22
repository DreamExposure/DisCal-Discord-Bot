package com.cloudcraftgaming.discal.utils;

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

    //TODO: Add needed methods here...
}