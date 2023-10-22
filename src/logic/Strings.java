package logic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

/**
 * Class to convert objects to String and some transforms with Strings
 * @author Agzam4
 */
public class Strings {

	/**
	 * Convert image to BASE64 in PNG format
	 * @param img - image
	 * @return encoded in base64 image
	 */
	public static String toBase64(BufferedImage img) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", baos);
		} catch (IOException e) {
			return "";
		}
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
	
	/**
	 * @param object
	 * @return:<br>
	 * <code>"null"</code> if object is null<br>
	 * {@link #floatString(object)} if object instance of Float<br>
	 * on otherwise return {@link #object.toString(Object)};
	 */
	public static String toString(Object object) {
		if(object instanceof Float f) return floatString(f);
		if(object == null) return "null";
		return object.toString();
	}
	
	/**
	 * Convert float to String
	 * @param value - float value
	 * @return a short string variant of float:<br>
	 * <code>
	 * 1.1 -> 1.1 <br>
	 * 1.0 -> 1
	 * </code>
	 */
	public static String floatString(float value) {
		if(value == (int)value) return ((int)value) + "";
		return value + "";
	}

//	public static String replaceStart(String a, String b) {
//		if(b.length() < a.length()) return replaceStart(b, a);
//		
//		for (int i = 0; i < b.length(); i++) {
//			if(a.charAt(i) == b.charAt(i)) continue;
//			StringBuilder end = new StringBuilder();
//			for (int j = i; j < a.length(); j++) {
//				end.append(a.charAt(j));
//			}
//			return end.substring(i);
//		}
//		return "";
//	}
}
