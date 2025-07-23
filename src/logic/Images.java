package logic;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class Images {

	public static int[][] rgbs(BufferedImage image) {
		Raster raster = image.getRaster();
		int[][] rgbs = new int[image.getWidth()][image.getHeight()];
		byte[] buffer = new byte[4];
    	for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
	    		raster.getDataElements(x, y, 1, 1, buffer);
	    		if(buffer[3] == 0) {
	    			buffer[0] = 0;
	    			buffer[1] = 0;
	    			buffer[2] = 0;
	    			buffer[3] = 0;
	    		}
	    		rgbs[x][y] = 
	    				(buffer[0] & 0xFF) << 24 |
	    				(buffer[1] & 0xFF) << 16 |
	    				(buffer[2] & 0xFF) << 8 |
	    				(buffer[3] & 0xFF);
			}
    	}
    	return rgbs;
	}
}
