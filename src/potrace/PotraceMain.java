package potrace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import main.Log;

public class PotraceMain {

	public static void main(String[] args) throws IOException {
		var svg = PotraceRunner.instance.svg(ImageIO.read(new File("potrace/input.png")));
		Log.info(svg);
		Files.writeString(Paths.get("potrace/output.svg"), svg.toString());
	}
}
