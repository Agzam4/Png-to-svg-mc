package main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import logic.MulticolorsConverter;

public class Main {

	/*
	 * Settings
	 */
	public static boolean multithreads = false;
	public static boolean inkscapeMode = true;
	public static boolean changeType = false; // Save all images as TYPE_4BYTE_ABGR, can be enabled in runtime
	public static boolean grid = false;
	public static boolean sourceImage = false;
	public static int freeProcessors = 1;
	public static float scale = 1;
	public static String svgTab = "\t"; // null for one-line SVG
	public static File output = new File("svg"); // output files directory
	public static File source = new File("source"); // source files directory
	public static Scanner scanner = new Scanner(System.in);
	
	public static void main(String[] args) throws IOException {
		int threads = Runtime.getRuntime().availableProcessors() - freeProcessors;
		if(threads < 1) threads = 1;
		
		if(multithreads) service = Executors.newFixedThreadPool(threads);
		
		eachFile(source);
		isEnd = true;
		scanner.close();
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
					MulticolorsConverter.converter(f, f.getName().substring(0, f.getName().length()-4));
//					Converter.converter(f, f.getName().substring(0, f.getName().length()-4));
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
