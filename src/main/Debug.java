package main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Debug {

	public static final Scanner scanner = new Scanner(System.in);
	
	public static boolean pause() {
		return !scanner.nextLine().isEmpty();
	}
	

	public static JFrame debug;
	public static JLabel label;
	public static BufferedImage buffer;

	public static void createFrame() {
		debug = new JFrame();
		debug.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		debug.setAlwaysOnTop(true);
		debug.setBounds(50, 50, 500, 500);
		label = new JLabel();
		debug.getContentPane().add(label);
		debug.setVisible(true);
	}
	
	public static void repaint(BufferedImage img) {
//		if(buffer == null) {
//			buffer = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
//		}
//		Graphics2D g = (Graphics2D) buffer.getGraphics();
//		g.drawImage(img, 0, 0, null);
//		g.dispose();
		label.setIcon(new ImageIcon(img));
		debug.setSize(img.getWidth(), img.getHeight());
	}
}
