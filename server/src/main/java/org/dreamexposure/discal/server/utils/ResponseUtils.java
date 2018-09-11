package org.dreamexposure.discal.server.utils;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class ResponseUtils {
	public static String getJsonResponseMessage(String msg) {
		return "{\"Message\": \"" + msg + "\"}";
	}
}