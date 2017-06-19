package com.cloudcraftgaming.discal.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Nova Fox on 6/2/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal
 */
public class ImageUtils {
	public static boolean validate(String url) {
		try {
			Image image = ImageIO.read(new URL(url));
			return image != null;
		} catch (IOException e) {
			return false;
		}
	}
}