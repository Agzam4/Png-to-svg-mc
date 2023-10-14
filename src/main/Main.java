package main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import logic.Converter;

public class Main {

	static ExecutorService service;
	
	public static void main(String[] args) throws IOException {
		int freeProcessors = 0; // <-- change it if want 
		
		int threads = Runtime.getRuntime().availableProcessors() - freeProcessors;
		if(threads < 1) threads = 1;
		
		service = Executors.newFixedThreadPool(threads);
		
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
//				service.submit(() -> {
					String save = f.getName().substring(0, f.getName().length()-4);
//					MarchingSquares.debug = null;
					long start = System.nanoTime();
					try {
						Converter.converter(ImageIO.read(f), save);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println(save + " " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
//				});
				
					
//				if(skip > 0) {
//					skip--;
//					continue;
//				}
//				limit--;
//				if(limit < 0) return;
//				System.out.println(f.getPath());
			}
		}
	}
}
