package org.dreamexposure.discal.core.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Created by Nova Fox on 11/10/17.
 * Website: www.cloudcraftgaming.com
 * For Project: DisCal-Discord-Bot
 */
public class ImageUtils {
    public static boolean validate(String url, boolean allowGif) {
        try {
            Image image = ImageIO.read(new URL(url));

            if (image != null)
                return true;

            //Check if gif
            if (allowGif)
                return validateGif(url);

            return false;
        } catch (IOException e) {
            //Check if gif
            if (allowGif)
                return validateGif(url);

            return false;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean validateGif(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            InputStream in;
            try {
                in = connection.getInputStream();
                return readGif(in).equalsIgnoreCase("gif");
            } catch (IOException | NullPointerException e) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    private static String readGif(InputStream input) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(input);

        Iterator iter = ImageIO.getImageReaders(stream);
        if (!iter.hasNext()) {
            return null;
        }
        ImageReader reader = (ImageReader) iter.next();
        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(stream, true, true);
        BufferedImage bi;
        try {
            bi = reader.read(0, param);
        } catch (IOException e) {
            // Auto-generated catch block
            //e.printStackTrace();
        } finally {
            reader.dispose();
            try {
                stream.close();
            } catch (IOException e) {
                //  Auto-generated catch block
                //e.printStackTrace();
            }
        }
        return reader.getFormatName();
    }
}