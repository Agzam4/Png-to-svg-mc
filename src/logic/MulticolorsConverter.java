package logic;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import main.Log;
import main.Main;
import svg.SvgElement;

public class MulticolorsConverter {

	static int tmp = 0;
	
	public static void converter(File file, String save) {
		long start = System.nanoTime();

		BufferedImage source = null;
		try {
			source = ImageIO.read(file);
			if(source.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
				if(!Main.changeType) {
					Log.warn("[blue]@[] not has 4BYTE_ABGR type! (Current encoding: @), enable [blue]changeType[] to rewrite files", save, source.getType());
					Log.info("Enable this option for current session?");
					Log.info("[blue]Y/Yes[] - for this file");
					Log.info("[blue]A/All[] - for all next files");
					Log.info("[red]The files will be rewritten[]");
					String c = Main.scanner.nextLine();
					if(c.equalsIgnoreCase("A") || c.equalsIgnoreCase("All")) {
						Main.changeType = true;
					} else if(!c.equalsIgnoreCase("Y") && !c.equalsIgnoreCase("Yes")) {
						return;
					}
				}
				BufferedImage otherType = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g = (Graphics2D) otherType.getGraphics();
				g.drawImage(source, 0,0, null);
				g.dispose();
				ImageIO.write(otherType, "png", file);
				Log.info("[red]File [blue]@[] was rewritten (type changed to 4BYTE_ABGR)[]", save);
			}
			
			
		} catch (IOException e) {
			Log.err("@ - err to read file", file.getName());
			return;
		}
		
		int w = source.getWidth()+2;
		int h = source.getHeight()+2;

		HashMap<Integer, boolean[][]> colors = new HashMap<Integer, boolean[][]>();

		Integer[][] colorsMap = new Integer[w*2+2][h*2+2];

		
		Raster raster = source.getRaster();

		int[][] rgbs = new int[w][h];

		byte[] buffer = new byte[4];
		for (int y = -1; y < h-1; y++) {
			for (int x = -1; x < w-1; x++) {
				if(x < 0 || y < 0 || x >= w-2 || y >= h-2) {
					buffer[0] = 123;
					buffer[1] = 0;
					buffer[2] = 0;
					buffer[3] = 123;
					rgbs[x+1][y+1] = 0xFF337755;
					continue;
				} else {
					raster.getDataElements(x, y, 1, 1, buffer);
				}
				if(buffer[0] == 1) buffer[0] = 0;
				if(buffer[3] == 0) {
					buffer[0] = 1;
					buffer[1] = 0;
					buffer[2] = 0;
					buffer[3] = 0;
				}
				
				rgbs[x+1][y+1] = //source.getRGB(x, y);
						(buffer[0] & 0xFF) << 24 |
						(buffer[1] & 0xFF) << 16 |
						(buffer[2] & 0xFF) << 8 |
						(buffer[3] & 0xFF);
			}
		}
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = rgbs[x][y];

				for (int cy = 0; cy < 2; cy++) {
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
		
		w -= 2;
		h -= 2;

		SvgElement svg = new SvgElement("svg")
				.attribute("version", "2.0")
				.attribute("xmlns", "http://www.w3.org/2000/svg")
				.attribute("xmlns:xlink", "http://www.w3.org/1999/xlink")
				.attribute("viewBox", "0 0 @ @", w*2*Main.scale, h*2*Main.scale);

		if(Main.inkscapeMode) {
			svg.attribute("xmlns:sodipodi", "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd")
			.attribute("xmlns:inkscape", "http://www.inkscape.org/namespaces/inkscape");
		}

		if(colors.entrySet().size() > 150) {
			Log.warn("Too many colors, skipping (@/150)", colors.entrySet().size());
			return;
		}

		svg.add(SvgConverter.getSvgPaths(new MulticolorsMarchingSquares(rgbs).create(save)));

		if(Main.sourceImage) {
			SvgElement image = new SvgElement("image")
					.attribute("href", "data:image/png;base64," + Strings.toBase64(source))
					.attribute("width", w*2*Main.scale)
					.attribute("height", h*2*Main.scale)
//					.attribute("x", (0)*Main.scale)
//					.attribute("y", (0)*Main.scale)
					.attribute("opacity", ".5")
					.attribute("style", "image-rendering:pixelated");

			if(Main.inkscapeMode) {
				SvgElement layerSource = new SvgElement("g")
						.attribute("inkscape:groupmode", "layer")
						.attribute("inkscape:label", "Source image")
						.attribute("sodipodi:insensitive", true)
						.attribute("display", "none")
						.attribute("opacity", ".5");

				layerSource.add(image);
				svg.add(layerSource);
			} else {
				svg.add(image);
			}
		}

		if(Main.grid) {
			if(Main.inkscapeMode) {
				svg.add(new SvgElement("sodipodi:namedview")
						.attribute("inkscape:snap-global", true)
						.attribute("inkscape:pagecheckerboard", true)
						.attribute("units", "px")
						.attribute("showgrid", "true").add(new SvgElement("inkscape:grid")
								.attribute("type", "xygrid")
								.attribute("empspacing", 2)
								.attribute("spacingy", Main.scale)
								.attribute("spacingx", Main.scale)
								));
			} else {
				SvgElement plain = new SvgElement("g")
						.attribute("stroke", "#00000011")
						.attribute("stroke-width", ".1%");
				SvgElement bold = new SvgElement("g")
						.attribute("stroke", "#00000044")
						.attribute("stroke-width", ".1%");

				for (int y = 0; y < h*2; y++) {
					(y%2 == 0 ? bold : plain).add(new SvgElement("line")
							.attribute("x1", 0*Main.scale)
							.attribute("y1", y*Main.scale)
							.attribute("x2", w*2*Main.scale)
							.attribute("y2", y*Main.scale)
							);
				}
				for (int x = 0; x < w*2; x++) {
					(x%2 == 0 ? bold : plain).add(new SvgElement("line")
							.attribute("x1", x*Main.scale)
							.attribute("y1", 0)
							.attribute("x2", x*Main.scale)
							.attribute("y2", h*2*Main.scale)
							);
				}
				svg.add(plain);
				svg.add(bold);
			}
		}

		byte bs[] = svg.toString().getBytes(StandardCharsets.UTF_8);
		try {
			File output =  new File(Main.output.getAbsoluteFile() + file.getAbsolutePath().substring(Main.source.getAbsolutePath().length()));
			output.getParentFile().mkdirs();
			String outpath = output.getAbsolutePath();
			outpath = outpath.substring(0, outpath.length()-4);
			Files.write(Paths.get(outpath + "-multi.svg"), bs);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Log.info("[green]@[] @x@ @ bytes [green]@ms[]", save, w,h, bs.length, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
	}
}
