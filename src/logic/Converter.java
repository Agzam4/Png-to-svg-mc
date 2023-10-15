package logic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.util.Base64;

import logic.MarchingSquares.VecPathArea;

public class Converter {

	public static String base64encoder(File file) {
        String base64File = "";
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a file from file system
            byte fileData[] = new byte[(int) file.length()];
            imageInFile.read(fileData);
            base64File = Base64.getEncoder().encodeToString(fileData);
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the file " + ioe);
        }
        return base64File;
    }
	
	public static void converter(File file, String save) throws IOException {
		BufferedImage source;
		source = ImageIO.read(file);

		Raster raster = source.getRaster();

		int w = raster.getWidth(); // 32n
		int h = raster.getHeight(); // 32n
		
		HashMap<Integer, boolean[][]> colors = new HashMap<Integer, boolean[][]>();

		Integer[][] colorsMap = new Integer[w*2+2][h*2+2];
		
		int[][] rgbs = new int[h][w];
		//System.out.println("Image: " + w + "x" + h);

		byte[] buffer = new byte[4];
    	for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
	    		raster.getDataElements(x, y, 1, 1, buffer);
	    		if(buffer[3] == 0) {
	    			buffer[0] = 0;
	    			buffer[1] = 0;
	    			buffer[2] = 0;
	    		}
	    		rgbs[x][y] = 
	    				(buffer[0] & 0xFF) << 24 |
	    				(buffer[1] & 0xFF) << 16 |
	    				(buffer[2] & 0xFF) << 8 |
	    				(buffer[3] & 0xFF);
			}
    	}

    	
//		for (int i = 0; i < rgbs.length; i++) {
//			System.out.println("getDataElements: " + i);
//			raster.getDataElements(i, 0, rgbs[i]);
//			raster.
//		}
		//System.out.println("ok");
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = rgbs[x][y];
				
				for (int cy = -1; cy < 2; cy++) {
					for (int cx = 0; cx < 2; cx++) {
						if(x+cx >= w || y+cy >= h) continue;
						if(x+cx < 0 || y+cy < 0) continue;
						if(rgb == rgbs[x+cx][y+cy]) {
							colorsMap[x*2 + cx + 2][y*2 + cy + 2] = rgb;
						}
					}
				}
				
				boolean[][] map = colors.get(rgb);
				if(map == null) {
					map = new boolean[w+1][h+1];
					colors.put(rgb, map);
				}
				map[x+1][y+1] = true;
			}
		}

		StringBuilder svg = new StringBuilder();
		///header
		svg.append(
			"""
			<svg id="svg" viewBox="0 0 @ @" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
			""".replaceFirst("@", w+"").replaceFirst("@", h+""));
		//grid
		svg.append("""
				<sodipodi:namedview
					inkscape:snap-global="true"
					units="px"
					showgrid="true">
					<inkscape:grid
						empspacing="2"
						spacingy="0.5"
						spacingx="0.5"
						type="xygrid"
					/>
				</sodipodi:namedview>
			""");


		if(colors.entrySet().size() > 20) {
			System.err.println("To many colors, skipping");
			return;
		}
		
		ArrayList<VecPathArea> paths = new ArrayList<>();
		
		colors.entrySet().forEach(e -> {
			if((e.getKey() & 0xFF) == 0) return;
			paths.addAll(new MarchingSquares(e.getValue()).colorsMap(colorsMap).create(e.getKey(), save, new Color(e.getKey())).getSvgPaths(e.getKey()));
		});
		
		
		paths.sort(new Comparator<VecPathArea>() {

			@Override
			public int compare(VecPathArea p1, VecPathArea p2) {
				return p2.boundsArea() - p1.boundsArea();
			}
		});
		
		//layer with the paths
		svg.append("""
			<g
				inkscape:groupmode="layer"
				inkscape:label="Paths">
			""");
		for (VecPathArea p : paths) {
			svg.append("\t"+p.svg());
		}
		svg.append("</g>\n");

		//layer with the reference image in base64
		svg.append("""
			<g
				inkscape:groupmode="layer"
				inkscape:label="Reference"
				style="display:none; opacity:0.0"
				sodipodi:insensitive="true">
			""");
		String imagebase64 = base64encoder(file);
		svg.append(	
			"""
				<image
					xlink:href="data:image/png;base64,@"
					style="image-rendering:pixelated"/>
			""".replaceFirst("@", imagebase64));
		svg.append("</g>\n");

		svg.append("</svg>");
		
		try {
			File f = new File("svg");
			f.mkdirs();
//			System.out.println(f.getAbsolutePath());
			Files.write(Paths.get(f.getAbsolutePath() + "/" + save + ".svg"), svg.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
//		colors.entrySet().forEach(e -> {
//			for (int y = 0; y < h; y++) {
//				for (int x = 0; x < w; x++) {
//					System.out.print(e.getValue()[x][y] ? "\u2593" : " ");
//					System.out.print(e.getValue()[x][y] ? "\u2593" : " ");
//				}
//				System.out.println();
//			}
//			System.out.println();
//		});
		
		
		
	}
}
