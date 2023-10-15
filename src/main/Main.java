package main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import logic.Converter;

public class Main {

	/*
	 * Settings
	 */
	public static boolean multithreads = true;
	public static boolean inkscapeMode = true;
	public static boolean grid = true;
	public static boolean sourceImage = true;
	public static int freeProcessors = 1;
	public static float scale = .5f;
	public static String svgTab = "\t"; // null for one-line SVG
	
	
	public static void main(String[] args) throws IOException {
		
		int threads = Runtime.getRuntime().availableProcessors() - freeProcessors;
		if(threads < 1) threads = 1;
		
		if(multithreads) service = Executors.newFixedThreadPool(threads);
		
		File source = new File("source");
		
		eachFile(source);
		isEnd = true;
		
	}

	private static ExecutorService service;
	static int await = 0;
	static boolean isEnd = false;

	private static void eachFile(File file) throws IOException {
		for (File f : file.listFiles()) {
			if(f.isDirectory()) {
				eachFile(f);
			} else if(f.getName().endsWith(".png")) {
				await++;
				Runnable r = () -> {
					Converter.converter(f, f.getName().substring(0, f.getName().length()-4));
					await--;
					if(isEnd && await == 0) {
						System.exit(0);
					}
				};
				
				if(multithreads) {
					service.submit(r);
				} else {
					r.run();
				}
			}
		}
	}
}
