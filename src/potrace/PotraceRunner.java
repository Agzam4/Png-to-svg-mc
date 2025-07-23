package potrace;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import javax.imageio.ImageIO;

import logic.Colors;
import logic.Images;
import main.Log;
import svg.SvgElement;

public class PotraceRunner {
	
	public static final PotraceRunner instance = new PotraceRunner(new File("potrace"));
	
	private HashMap<String, String> options = new HashMap<String, String>();
	
	public final File root;
	
	private int scale = 1;
	
	public PotraceRunner(File root) {
		this.root = root;
		reconfig();
	}

	public PotraceRunner scale(int scale) {
		if(scale < 1) scale = 1;
		this.scale = scale;
		return this;
	}
	
	public int unit() {
		return scale;
	}
	
	public SvgElement svg(BufferedImage img) {
		
		SvgElement svg = new SvgElement("svg")
				.attribute("version", "2.0")
				.attribute("xmlns", "http://www.w3.org/2000/svg")
				.attribute("xmlns:xlink", "http://www.w3.org/1999/xlink")
				.attribute("viewBox", "0 0 @ @", img.getWidth()*scale, img.getHeight()*scale);
		
		HashSet<Integer> colors = new HashSet<>();
		int[][] rgbs = Images.rgbs(img);
		
		for (int sy = 0; sy < img.getHeight(); sy++) {
			for (int sx = 0; sx < img.getWidth(); sx++) {
				int targetRgb = rgbs[sx][sy];
				if(Colors.alpha(targetRgb) == 0) continue;
				if(colors.contains(targetRgb)) continue;
				colors.add(targetRgb);
				
				BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
				
				for (int y = 0; y < img.getHeight(); y++) {
					for (int x = 0; x < img.getWidth(); x++) {
						int rgb = rgbs[x][y];
						if(rgb == targetRgb) continue;
						gray.setRGB(x, gray.getHeight()-y-1, Color.white.getRGB());
					}
				}
				
				String name = "tmp";// + colors.size();
				try {
					ImageIO.write(gray, "bmp", new File(root.getAbsolutePath() + "/" + name + ".bmp"));
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
				
				var path = path(name);
				var color = new Color(targetRgb);
				if(color.getAlpha() == 0) continue;
				path.attribute("fill", Colors.toHex(targetRgb));
				svg.add(path);
			}
		}
		return svg;
	}
	
	public SvgElement path(String name) {
		try {
			var output = execute(name);
			String file = Files.readString(Paths.get(output));
			int index = file.indexOf("d=\"")+3;
			SvgElement path = new SvgElement("path");
			path.attribute("d", file.substring(index, file.indexOf('"', index)));
			return path;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String execute(String name) throws IOException {
		String output = name + ".svg";
		File exe = new File(root.getAbsoluteFile() + "/potrace.exe");
		
		optionsCache[0] = exe.getAbsolutePath();
		optionsCache[optionsCache.length-1] = name + ".bmp";
		
		ProcessBuilder builder = new ProcessBuilder(optionsCache).directory(root);
		Process process = builder.start();
//		Log.info("Running [blue]potrace[] PID: [blue]@[] Args: [blue]@[]", process.pid(), Arrays.toString(optionsCache));
		
		new Thread(() -> {
			var out = new Scanner(process.getInputStream());
			while (out.hasNext()) {
				Log.info("[blue][I][] @", out.nextLine());
			}
			out.close();
		}, "input-stream").run();

		new Thread(() -> {
			var out = new Scanner(process.getErrorStream());
			while (out.hasNext()) {
				Log.info("[red][E][] @", out.nextLine());
			}
			out.close();
		}, "error-stream").run();
		
		return root.getAbsolutePath() + "/" + output;
	}
	
	
	private String[] optionsCache = {"", ""};

	public void reconfig() {
		
		ArrayList<String> opts = new ArrayList<String>();
		opts.add("-s"); // SVG format output
		opts.add("--flat"); // single path output
		
		opts.add("-u"); 
		opts.add(Integer.toString(scale));
		
		options.forEach((k,v) -> {
			opts.add(k);
			if(v != null) opts.add(v);
		});
		
		if(optionsCache.length != opts.size()+2) optionsCache = new String[opts.size()+2];
		for (int i = 0; i < opts.size(); i++) {
			optionsCache[i+1] = opts.get(i);
		}
	}
	
	public PotraceRunner config(String type, String args) {
		options.put(type, args);
		reconfig();
		return this;
	}

	public PotraceRunner clearConfig(String type) {
		options.remove(type);
		reconfig();
		return this;
	}
	
	
}
