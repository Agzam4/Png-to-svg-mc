package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import logic.Node;

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

	
	public static BufferedImage image = null;
	public static Graphics2D g = null;
	public static int scl;
	
	public static void image(int w, int h, int scale) {
		image = new BufferedImage(w*scale, h*scale, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		scl = scale;
	}

	public static void color(Color c) {
		if(g != null) g.setColor(c);
	}

	public static void color(int r, int g, int b, int a) {
		color(new Color(r, g, b, a));
	}
	
	public static void color(int r, int g, int b) {
		color(new Color(r, g, b));
	}

	public static void fillOval(int x, int y, float w, float h) {
		if(g == null) return;
		g.fillOval((int) (x*scl - w*scl/2), (int) (y*scl - h*scl/2), (int) (w*scl), (int) (h*scl));
	}

	public static void fillOval(Node n) {
		fillOval(n.x, n.y, 1, 1);
	}

	public static void fillRect(int x, int y, float w, float h) {
		if(g == null) return;
		g.fillRect((int) (x*scl), (int) (y*scl), (int) (w*scl), (int) (h*scl));
	}

	public static void fillRect(Node n) {
		fillRect(n.x, n.y, 1, 1);
	}

	public static void line(int x1, int y1, int x2, int y2) {
		if(g != null) g.drawLine(x1*scl, y1*scl, x2*scl, y2*scl);
	}

	public static void line(Node n1, Node n2) {
		line(n1.x, n1.y, n2.x, n2.y);
	}

	public static void write(String path) {
		g.dispose();
		try {
			if(new File(path).getParentFile().exists()) ImageIO.write(image, "png", new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public static void maskColor(int i) {
		if(g == null) return;
		if(i == -1) g.setColor(Color.magenta);
		if(i == 0) g.setColor(Color.red);
		if(i == 1) g.setColor(Color.green);
		if(i == 2) g.setColor(Color.blue);
		if(i == 3) g.setColor(Color.yellow);
	}

	public static void drawRect(int x, int y, float w, float h) {
		if(g == null) return;
		g.drawRect((int) (x*scl), (int) (y*scl), (int) (w*scl), (int) (h*scl));
	}

	public static void rgb(int rgb) {
		color(new Color(rgb));
	}
}
