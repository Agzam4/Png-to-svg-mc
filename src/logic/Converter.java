package logic;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;

import javax.imageio.ImageIO;

import logic.MarchingSquares.VecPathArea;
import main.Main;

public class Converter {
	
	@SuppressWarnings("unlikely-arg-type")
	public static Point converter(File file, String save) {
		
		BufferedImage source = null;
		try {
			source = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return new Point(0, 0);
		}
		
		int w = source.getWidth();
		int h = source.getHeight();
		
		HashMap<Integer, boolean[][]> colors = new HashMap<Integer, boolean[][]>();

		Integer[][] colorsMap = new Integer[w*2+2][h*2+2];
		
		Raster raster = source.getRaster();
		
		int[][] rgbs = new int[h][w];

		byte[] buffer = new byte[4];
    	for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
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
		// For debug: width="1320px" height="1320px"

		if(!Main.x2SizeMode) {
			w /= 2f;
			h /= 2f;
		}

		svg.append(
				"""
				<svg id="svg" viewBox="0 0 @ @" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape">
				""".replaceFirst("@", (w*2)+"").replaceFirst("@", (h*2) + ""));

		if(Main.grid) {
			if(Main.inkscapeMode) {
				svg.append("""
						<sodipodi:namedview
							inkscape:snap-global="true"
							units="px"
							showgrid="true">
							<inkscape:grid
								empspacing="@biggrid"
								spacingy="@grid"
								spacingx="@grid"
								type="xygrid"
							/>
						</sodipodi:namedview>
					"""
					.replaceFirst("@biggrid", Main.x2SizeMode? "4": "2")
					.replaceAll("@grid", Main.x2SizeMode? "1": "0.5"));
			} else {
				svg.append("<g stroke=\"#00000011\" stroke-width=\".1%\">");
				for (int y = 0; y < h; y++) {
					svg.append("""
							<line x1="0" y1="@" x2="@" y2="@"/>
						""".replaceFirst("@", y*2+"").replaceFirst("@", w*2+"").replaceFirst("@", y*2+""));
				}
				for (int x = 0; x < w; x++) {
					svg.append("""
							<line x1="@" y1="0" x2="@" y2="@"/>
						""".replaceFirst("@", x*2+"").replaceFirst("@", x*2+"").replaceFirst("@", h*2+""));
				}
				svg.append("</g>");
			}
		}
		
		if(colors.entrySet().size() > 10) {
			System.err.println("To many colors, skipping");
			return new Point(w, h);
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

		if(Main.inkscapeMode) {
			//main layer
			svg.append("""
					<g
						inkscape:groupmode="layer"
						inkscape:label="Paths">
					""");		
			for (VecPathArea p : paths) {
				svg.append("\t"+p.svg()+"\n");
			}
			svg.append("</g>");
		}else{
			for (VecPathArea p : paths) {
				svg.append(p.svg());
			}
		}
		
		if(Main.sourceImage) {
			if(Main.inkscapeMode) {
				svg.append("""
					\n<g
						inkscape:groupmode="layer"
						inkscape:label="Reference"
						style="display:none"
						opacity="0.5"
						sodipodi:insensitive="true">
						<image style="image-rendering:pixelated" height="@" width="@" xlink:href="data:image/png;base64,@"/>
					""".replaceFirst("@", w*2 + "").replaceFirst("@", h*2 + "").replaceFirst("@", toBase64(source)));
				svg.append("</g>");
			} else {
				svg.append("""
						<image opacity=".5" style="image-rendering: pixelated" height="@" width="@" href="data:image/png;base64,@"/>
						""".replaceFirst("@", toBase64(source)).replaceFirst("@", w*2 + "").replaceFirst("@", h*2 + ""));
			}
		}
		
		svg.append("</svg>");
		
		byte bs[] = svg.toString().getBytes(StandardCharsets.UTF_8);
//		System.out.println(bs.length + "bytes");
		
		try {
			File f = new File("svg");
			f.mkdirs();
//			System.out.println(f.getAbsolutePath());
			Files.write(Paths.get(f.getAbsolutePath() + "/" + save + ".svg"), bs);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return new Point(w, h);
		
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
	
	public static String toBase64(BufferedImage img) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", baos);
		} catch (IOException e) {
			return "";
		}
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}
}
