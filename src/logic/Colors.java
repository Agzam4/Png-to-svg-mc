package logic;

import java.awt.Color;

public class Colors {

	public static String toHex(int rgba) {
		return toHex(
				 (rgba >> 24) & 0xFF, 
				 (rgba >> 16) & 0xFF,
				 (rgba >> 8) & 0xFF, 
				 (rgba) & 0xFF);
	}
	
	public static String toHex(Color color) {
		return toHex(color.getRed(), color.getGreen(), color.getBlue());
	}

	public static String toHex(int r, int g, int b) {
		String color = String.format("%02x%02x%02x", r, g, b);
		boolean c6 = false;
		for (int i = 0; i < 3; i++) {
			if(color.charAt(i*2) != color.charAt(i*2+1)) {
				c6 = true;
				break;
			}
		}
		if(c6) return "#" + color.charAt(0) + "" + color.charAt(2) + "" + color.charAt(4);
		return "#" + color;
	}
	

	public static String toHex(int r, int g, int b, int a) {
		if(a == 255) return toHex(r, g, b);
		return String.format("#%02x%02x%02x%02x", r, g, b, a);
	}
	
	
}
