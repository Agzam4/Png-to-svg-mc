package potrace;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import javax.imageio.ImageIO;

import logic.Colors;
import main.Log;
import svg.SvgElement;

public class PotraceRunner {

	public static final PotraceRunner instance = new PotraceRunner(new File("potrace"));
//	private ExecutorService executor = Executors.newFixedThreadPool(1);
	 
	public final File root;
	
	public PotraceRunner(File root) {
		this.root = root;
	}
	
	public SvgElement svg(BufferedImage img) {
		
		SvgElement svg = new SvgElement("svg")
				.attribute("version", "2.0")
				.attribute("xmlns", "http://www.w3.org/2000/svg")
				.attribute("xmlns:xlink", "http://www.w3.org/1999/xlink")
				.attribute("viewBox", "0 0 @ @", img.getWidth()*5, img.getHeight()*5);
		
		HashSet<Integer> colors = new HashSet<>();
		for (int sy = 0; sy < img.getHeight(); sy++) {
			for (int sx = 0; sx < img.getWidth(); sx++) {
				int targetRgb = img.getRGB(sx, sy);
				if(colors.contains(targetRgb)) continue;
				colors.add(targetRgb);
				
				BufferedImage gray = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
				
				for (int y = 0; y < img.getHeight(); y++) {
					for (int x = 0; x < img.getWidth(); x++) {
						int rgb = img.getRGB(x, y);
						if(rgb == targetRgb) continue;
						gray.setRGB(x, y, Color.white.getRGB());
					}
				}
				
				String name = "tmp";// + colors.size();
				try {
					ImageIO.write(gray, "png", new File(root.getAbsolutePath() + "/" + name + ".png"));
					ImageIO.write(gray, "bmp", new File(root.getAbsolutePath() + "/" + name + ".bmp"));
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
				
				var path = path(name);
				var color = new Color(targetRgb);
				if(color.getAlpha() == 0) continue;
				path.attribute("fill", Colors.toHex(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
				svg.add(path);
			}
		}
		return svg;
	}
	
	public SvgElement path(String name) {
		try {
			var output = run(name);
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
	
	public String run(String name) throws IOException {
		String output = name + ".svg";
		File exe = new File(root.getAbsoluteFile() + "/potrace.exe");
		
		ProcessBuilder builder = new ProcessBuilder(exe.getAbsolutePath(), "-s", "--flat", "-u", "5", name + ".bmp").directory(root);
		Process process = builder.start();
		Log.info("Running [blue]potrace[] PID: [blue]@[]", process.pid());
		
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
	
	
	
}
