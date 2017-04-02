package com.cloudcraftgaming.discal.utils;

/**
 * Created by Nova Fox on 4/2/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
@SuppressWarnings("SameParameterValue")
public class GeneralUtils {
    /**
     * Gets the contents of the message at a set offset.
     * @param args The args of the command.
     * @param offset The offset in the string.
     * @return The contents of the message at a set offset.
     */
    public static String getContent(String[] args, int offset) {
        StringBuilder content = new StringBuilder();
        for (int i = offset; i < args.length; i++) {
            content.append(args[i]).append(" ");
        }
        return content.toString().trim();
    }
}