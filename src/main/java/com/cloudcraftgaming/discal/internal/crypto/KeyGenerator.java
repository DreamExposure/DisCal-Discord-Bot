package com.cloudcraftgaming.discal.internal.crypto;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Nova Fox on 3/24/2017.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class KeyGenerator {
    private static char[] VALID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456879".toCharArray();

    // cs = cryptographically secure
    @SuppressWarnings("SameParameterValue")
    public static String csRandomAlphaNumericString(int numChars) {
        SecureRandom secRand = new SecureRandom();
        Random rand = new Random();
        char[] buff = new char[numChars];

        for (int i = 0; i < numChars; ++i) {
            // reseed rand once you've used up all available entropy bits
            if ((i % 10) == 0) {
                rand.setSeed(secRand.nextLong()); // 64 bits of random!
            }
            buff[i] = VALID_CHARACTERS[rand.nextInt(VALID_CHARACTERS.length)];
        }
        return new String(buff);
    }
}