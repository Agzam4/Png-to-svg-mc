package main;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import logic.Converter;
import logic.MarchingSquares;

public class Main {

	public static void main(String[] args) throws IOException {
		
		File source = new File("source"); ///walls /walls
		
		eachFile(source);
		
	}
	

//	static int skip = 5, limit = 1;
	
	private static void eachFile(File file) throws IOException {
//		if(limit < 0) return;
		for (File f : file.listFiles()) {
			if(f.isDirectory()) {
				eachFile(f);
			} else if(f.getName().endsWith(".png")) {
//				if(skip > 0) {
//					skip--;
//					continue;
//				}
//				limit--;
//				if(limit < 0) return;
//				System.out.println(f.getPath());
				MarchingSquares.save = f.getName().substring(0, f.getName().length()-4);
				MarchingSquares.debug = null;
				long start = System.nanoTime();
				System.out.print(MarchingSquares.save);
				Converter.converter(ImageIO.read(f));
				System.out.println(" " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
			}
		}
	}
}
