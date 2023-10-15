package main;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import logic.Converter;

public class Main {

	/*
	 * Settings
	 */
	public static boolean multithreads = false;
	public static int freeProcessors = 1;

	public static boolean inkscapeMode = true;
	public static boolean grid = true;
	public static boolean sourceImage = true;
	public static boolean x2SizeMode = false;
	
	
	public static void main(String[] args) throws IOException {
		
		int threads = Runtime.getRuntime().availableProcessors() - freeProcessors;
		if(threads < 1) threads = 1;
		
		if(multithreads) service = Executors.newFixedThreadPool(threads);
		
		File source = new File("source/");
		
		eachFile(source);
		
	}

	private static ExecutorService service;

//	static int skip = 5, limit = 1;
	
	private static void eachFile(File file) throws IOException {
//		if(limit < 0) return;
		for (File f : file.listFiles()) {
			if(f.isDirectory()) {
				eachFile(f);
			} else if(f.getName().endsWith(".png")) {
				Runnable r = () -> {
					String save = f.getName().substring(0, f.getName().length()-4);
					long start = System.nanoTime();
					Point p = Converter.converter(f, save);
					System.out.println(save + " " + p.x + "x" + p.y + " " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
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
