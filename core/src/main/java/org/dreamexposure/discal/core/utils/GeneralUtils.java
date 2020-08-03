package org.dreamexposure.discal.core.utils;

import java.util.List;
import java.util.Random;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class GeneralUtils {
    /**
     * Gets the contents of the message at a set offset.
     *
     * @param args   The args of the command.
     * @param offset The offset in the string.
     * @return The contents of the message at a set offset.
     */
    public static String getContent(final List<String> args, final int offset) {
        final StringBuilder content = new StringBuilder();
        for (int i = offset; i < args.size(); i++) {
            content.append(args.get(i)).append(" ");
        }
        return content.toString().trim();
    }

    /**
     * Gets the contents of the message at a set offset.
     *
     * @param args   The args of the command.
     * @param offset The offset in the string.
     * @return The contents of the message at a set offset.
     */
    public static String getContent(final String[] args, final int offset) {
        final StringBuilder content = new StringBuilder();
        for (int i = offset; i < args.length; i++) {
            content.append(args[i]).append(" ");
        }
        return content.toString().trim();
    }

    /**
     * Trims the string front to back.
     *
     * @param str The String to trim.
     * @return The trimmed string.
     */
    public static String trim(String str) {
        while (str.length() > 1 && str.charAt(0) == ' ') {
            str = str.substring(1);
        }
        return str.trim();
    }

    /**
     * This is an overkill parser made by xaanit. You can thank him for this nightmare.
     * <br> <br>
     * regardless, it works, and therefore we will use it because generally speaking it seems some users do not understand that "<" and ">" are not in fact required and are just symbols <b>CLEARLY DEFINED</b> in our documentation.
     *
     * @param str The string to parse.
     * @return The string, but without the user errors.
     */
    @SuppressWarnings("MagicNumber")
    public static String overkillParser(final String str) {
        final Random random = new Random(str.length() * 2L >>> 4 & 3);
        final StringBuilder leftFace = new StringBuilder();
        final StringBuilder rightFace = new StringBuilder();
        final String alphabet = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 30; i++) {
            leftFace.append(alphabet.charAt(random.nextInt(alphabet.length())));
            rightFace.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return str.replace("<<", leftFace.toString()).replace(">>", rightFace.toString()).replace("<", "").replace(">", "").replace(leftFace.toString(), "<").replace(rightFace.toString(), ">");
    }
}