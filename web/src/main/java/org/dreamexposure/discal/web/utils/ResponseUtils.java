package org.dreamexposure.discal.web.utils;

public class ResponseUtils {
	public static String getJsonResponseMessage(String msg) {
		return "{\"Message\": \"" + msg + "\"}";
	}
}