package com.cloudcraftgaming.discal.utils;

/**
 * Created by Nova Fox on 6/2/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ImageUtils {
	public static boolean validate(String url) {
		return url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png");
	}
}