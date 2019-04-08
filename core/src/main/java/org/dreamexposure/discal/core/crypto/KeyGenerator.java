package org.dreamexposure.discal.core.crypto;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
@SuppressWarnings("Duplicates")
public class KeyGenerator {
	private static char[] VALID_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456879".toCharArray();

	private static char[] VALID_CHARS_2 = "abcdefghijklmnopqrstuv0123456789".toCharArray();

	// cs = cryptographically secure
	@SuppressWarnings("SameParameterValue")
	public static String csRandomAlphaNumericString(int numChars) {
		SecureRandom secRand = new SecureRandom();
		Random rand = new Random();
		char[] buff = new char[numChars];

		for (int i = 0; i < numChars; ++i) {
			// reseed rand once you've used up all available entropy bits
			if ((i % 10) == 0)
				rand.setSeed(secRand.nextLong()); // 64 bits of random!

			buff[i] = VALID_CHARACTERS[rand.nextInt(VALID_CHARACTERS.length)];
		}
		return new String(buff);
	}

	public static String generateEventId() {
		SecureRandom secRand = new SecureRandom();
		Random rand = new Random();
		char[] buff = new char[9];

		for (int i = 0; i < 9; ++i) {
			// reseed rand once you've used up all available entropy bits
			if ((i % 10) == 0)
				rand.setSeed(secRand.nextLong()); // 64 bits of random!

			buff[i] = VALID_CHARS_2[rand.nextInt(VALID_CHARS_2.length)];
		}
		return "e" + new String(buff);
	}

	public static String generateAnnouncementId() {
		SecureRandom secRand = new SecureRandom();
		Random rand = new Random();
		char[] buff = new char[9];

		for (int i = 0; i < 9; ++i) {
			// reseed rand once you've used up all available entropy bits
			if ((i % 10) == 0)
				rand.setSeed(secRand.nextLong()); // 64 bits of random!

			buff[i] = VALID_CHARS_2[rand.nextInt(VALID_CHARS_2.length)];
		}
		return "a" + new String(buff);
	}
}