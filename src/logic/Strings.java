package logic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

public class Strings {

	public static String toBase64(BufferedImage img) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", baos);
		} catch (IOException e) {
			return "";
		}
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	public static String toString(Object object) {
		if(object instanceof Float f) return floatString(f);
		if(object == null) return "null";
		return object.toString();
	}
	
	public static String floatString(float value) {
		if(value == (int)value) return ((int)value) + "";
		return value + "";
	}
}
