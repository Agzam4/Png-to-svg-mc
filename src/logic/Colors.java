package logic;

import java.awt.Color;

/**
 * Class for working with colors
 * @author Agzam4
 */
public class Colors {

	/**
	 * @param rgba - 0xrrggbbaa
	 * @return hex code of rgba<br>
	 * Possible returns:<br>
	 * <code>
	 * #rrggbbaa - if (alpha < 255) <br>
	 * #rrggbb - if (r/g/b<sub>first</sub> != r/g/b<sub>second</sub>)<br>
	 * #rgb - if (r/g/b<sub>first</sub> == r/g/b<sub>second</sub>)<br>
	 * </code>
	 */
	public static String toHex(int rgba) {
		return toHex(
				 (rgba >> 24) & 0xFF, 
				 (rgba >> 16) & 0xFF,
				 (rgba >> 8) & 0xFF, 
				 (rgba) & 0xFF);
	}
	/** 
	 * Change alpha in rgba to max
	 * @author hortiSquash
	 **/
	public static int RGBAtoRGB(int rgba) {
		return (rgba | 0xFF);
	}

	/**
	 * @param r - red [0-255]
	 * @param g - green [0-255]
	 * @param b - blue [0-255]
	 * @return hex code of color<br>
	 * Possible returns:<br>
	 * <code>
	 * #rrggbb - if (r/g/b<sub>first</sub> != r/g/b<sub>second</sub>)<br>
	 * #rgb - if (r/g/b<sub>first</sub> == r/g/b<sub>second</sub>)<br>
	 * </code>
	 */
	public static String toHex(Color color) {
		return toHex(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	/**
	 * @param r - red [0-255]
	 * @param g - green [0-255]
	 * @param b - blue [0-255]
	 * @return hex code of color<br>
	 * Possible returns:<br>
	 * <code>
	 * #rrggbb - if (r/g/b<sub>first</sub> != r/g/b<sub>second</sub>)<br>
	 * #rgb - if (r/g/b<sub>first</sub> == r/g/b<sub>second</sub>)<br>
	 * </code>
	 */
	public static String toHex(int r, int g, int b) {
		String color = String.format("%02x%02x%02x", r, g, b);
		boolean c3 = true;
		for (int i = 0; i < 3; i++) {
			if(color.charAt(i*2+1) != color.charAt(i*2)) { //
				c3 = false;
				break;
			}
		}
		if(c3) return "#" + color.charAt(0) + "" + color.charAt(2) + "" + color.charAt(4);
		return "#" + color;
	}
	

	/**
	 * @param r - red [0-255]
	 * @param g - green [0-255]
	 * @param b - blue [0-255]
	 * @param a - alpha [0-255]
	 * @return hex code of color<br>
	 * Possible returns:<br>
	 * <code>
	 * #rrggbbaa - if (alpha < 255) <br>
	 * #rrggbb - if (r/g/b<sub>first</sub> != r/g/b<sub>second</sub>)<br>
	 * #rgb - if (r/g/b<sub>first</sub> == r/g/b<sub>second</sub>)<br>
	 * </code>
	 */
	public static String toHex(int r, int g, int b, int a) {
		//if(a == 255) return toHex(r, g, b);
		if(a == 255) return String.format("#%02x%02x%02x", r, g, b);
		return String.format("#%02x%02x%02x%02x", r, g, b, a);
	}

	/** 
	 * @return alpha from rgba integer
	 * @author hortiSquash
	 **/
	public static int alpha(int rgba) {
		return (rgba) & 0xFF;
	}
}
