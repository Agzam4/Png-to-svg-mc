package logic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class MarchingSquares {

	final boolean[][] bs;
	final int w, h;
	
	public MarchingSquares(boolean[][] bs) {
		this.bs = bs;
		w = bs[0].length;
		h = bs.length;
	}
	
	static final int scale = 10;
	public static BufferedImage debug;
	static Color color = Color.white;
	public static String save = "debug";
	
	public MarchingSquares create() {
		if(debug == null) debug = new BufferedImage(w*scale*2, h*scale*2, BufferedImage.TYPE_INT_RGB);
		
		grid = new Node[w*2][h*2];
		
		Graphics2D g = (Graphics2D) debug.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.scale(scale, scale);
		for (int y = -1; y < h; y++) {
			for (int x = -1; x < w; x++) {
				boolean outY = y < 0 || y+1 >= h;
				boolean outX = x < 0 || x+1 >= w;

				boolean v1 = (x < 0 || outY) ? false : bs[x][y+1];
				boolean v2 = (outX || outY) ? false : bs[x+1][y+1];
				boolean v3 = (outX || y < 0) ? false : bs[x+1][y];
				boolean v4 = (x < 0 || y < 0) ? false : bs[x][y];

				int key = (v1?1:0) | ((v2?1:0) << 1) | ((v3?1:0) << 2) | ((v4?1:0) << 3);

				Vec4 vec1 = null;
				Vec4 vec2 = null;
				
				switch (key) {
				case 1 -> vec1 = new Vec4(0,1, 1,2);
				case 2 -> vec1 = new Vec4(2,1, 1,2);
				case 3 -> {
					vec1 = new Vec4(0,1, 1,1); vec2 = new Vec4(1,1, 2,1);
				}
				case 4 -> vec1 = new Vec4(1,0, 2,1);
				case 5 -> {
					vec1 = new Vec4(0,1, 1,2);
					vec2 = new Vec4(1,0, 2,1);
				}
				case 6 -> {
					vec1 = new Vec4(1,0, 1,1); vec2 = new Vec4(1,1, 1,2);
				}
				case 7 -> vec1 = new Vec4(0,1, 1,0);
				case 8 -> vec1 = new Vec4(0,1, 1,0);
				case 9 -> {
					vec1 = new Vec4(1,0, 1,1); vec2 = new Vec4(1,1, 1,2);
				}
				case 10 -> {
					vec1 = new Vec4(0,1, 1,2);
					vec2 = new Vec4(1,0, 2,1);
				}
				case 11 -> vec1 = new Vec4(1,0, 2,1);
				case 12 -> {
					vec1 = new Vec4(0,1, 1,1); vec2 = new Vec4(1,1, 2,1);
				}
				case 13 -> vec1 = new Vec4(2,1, 1,2);
				case 14 -> vec1 = new Vec4(0,1, 1,2);
				default -> {}
				}

//				g.drawString(key + "", x*scale*2, y*scale*2 + scale*2);
				g.setColor(color);
				
				if(vec1 == null && vec2 == null) continue;

				g.setStroke(new BasicStroke(scale/2f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
				if(vec1 != null) {
//					g.drawLine((x*2+vec1.x1)*scale, (y*2+vec1.y1)*scale, (x*2+vec1.x2)*scale, (y*2+vec1.y2)*scale);
					vec1.add(x*2, y*2);
					
					Node a1 = node(vec1.x1, vec1.y1);
					Node a2 = node(vec1.x2, vec1.y2);
					a1.link(a2);

//					if(a1.links.size() != 2) {
//						g.setColor(Color.red);
//						vec1.add(-x, -y);
//						g.fillRect(a1.x*2*scale, a1.y*2*scale, 1, 1);
//						
//						for (Node l : a1.links) {
//							g.setStroke(new BasicStroke(scale/5f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
//							g.drawLine(a1.x*2*scale, a1.y*2*scale, l.x*2*scale, l.y*2*scale);
//						}
//					}
				}

				g.setColor(color);
				g.setStroke(new BasicStroke(scale/2f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
				if(vec2 != null) {
//					g.drawLine((x*2+vec2.x1)*scale, (y*2+vec2.y1)*scale, (x*2+vec2.x2)*scale, (y*2+vec2.y2)*scale);
					vec2.add(x*2, y*2);
					
					Node b1 = node(vec2.x1, vec2.y1);
					Node b2 = node(vec2.x2, vec2.y2);
					b1.link(b2);

//					if(b1.links.size() != 2) {
//						g.setColor(Color.blue);
//						vec1.add(-x, -y);
//						g.fillRect(b1.x*2*scale,b1.y*2*scale, 1, 1);
//						
//						for (Node l : b1.links) {
//							g.setStroke(new BasicStroke(scale/5f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
//							g.drawLine(b1.x*2*scale, b1.y*2*scale, l.x*2*scale, l.y*2*scale);
//						}
//					}
				}
//				g.setColor(bs[x][y] ? Color.white : Color.darkGray);
//				g.fillOval(x*2*scale - scale/10, y*2*scale - scale/10, scale/5, scale/5);
			}
		}
		

		g.setStroke(new BasicStroke(scale/2f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
		for (int y = 0; y < h*2; y++) {
			for (int x = 0; x < w*2; x++) {
				if(grid[x][y] == null) continue;
				Node n = grid[x][y];
				for (Node l : n.links) {
					g.drawLine(n.x*scale, n.y*scale, l.x*scale, l.y*scale);
				}
			}
		}
		
		g.dispose();
		
		try {
			File file = new File("debug/" + save + ".png");
			file.mkdirs();
			ImageIO.write(debug, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public String toSvgGroup(StringBuilder svg) {
		
		svg.append(
				"""
				<g fill="@" stroke-width=".1" stroke="#333" stroke-linecap="round" stroke-linejoin="round">
				""".replaceFirst("@", "rgba(" + color.getRed() + " " + color.getGreen() + " " + color.getBlue() + ")")); //  / 90%
		
		searchIndex = 0;
		
		while (true) {
			
			ArrayList<Node> path = nextPath();
			if(path == null) break;
			svg.append("\n\t<path d=\"");
			for (int i = 0; i < path.size(); i++) {
				Node n = path.get(i);
				svg.append(i == 0 ? 'M' : 'L');
				svg.append(n.x);
				svg.append(',');
				svg.append(n.y);
				svg.append(' ');
			}

			svg.append("Z\"/>");
		}

		svg.append("\n</g>\n");
		return svg.toString();
	}
	
	int searchIndex = 0;
	
	private ArrayList<Node> nextPath() {
		for (int y = 0; y < h*2; y++) {
			for (int x = 0; x < w*2; x++) {
				if(grid[x][y] == null) continue;
				if(grid[x][y].links.size() != 2) continue;
				ArrayList<Node> path = new ArrayList<Node>();
				
				Node start = grid[x][y];
//				start.remove();
				
				Node c = start;
				Node last = null;
				while (true) {
					if(path.contains(c)) return path;
					path.add(c);
					for (Node link : c.links) {
						if(link == c) continue;
						if(link == last) continue;
//						if(path.contains(link)) return path;
						last = c;
						c.unlink(link);
						c = link;
						break;
					}
					if(c == start) return path;
					
				}
			}
		}
//		for (int i = searchIndex; i < w*h; i++) {
//			int x = i%(w*2);
//			int y = (i-x);
//			searchIndex++;
//			if(grid[x][y] == null) continue;
//			ArrayList<Node> path = new ArrayList<Node>();
//			
//			Node start = grid[x][y];
//			
//			Node c = start;
//			while (true) {
//				path.add(c);
//				for (Node link : c.links) {
//					if(link == c) continue;
//					if(path.contains(link)) return path;
//					c = link;
//					break;
//				}
//				
//			}
//		}
		return null;
	}
	
	private Node node(int x, int y) {
		if(grid[x][y] == null) {
			grid[x][y] = new Node(x, y);
		}
		return grid[x][y];
	}

	class Vec4 {
		
		int x1, y1, x2, y2;
		
		public Vec4(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}

		public void add(int x, int y) {
			x1 += x;
			x2 += x;
			
			y1 += y;
			y2 += y;
		}
	}
	
	Node[][] grid;
	
	
	class Node {
		
		ArrayList<Node> links = new ArrayList<Node>();

		int x, y;
		
		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void unlink(Node link) {
			links.remove(link);
			link.links.remove(this);
		}

//		public void remove() {
////			links = null;
//			grid[x][y] = null;
//		}

		public void link(Node n) {
			if(!links.contains(n)) links.add(n);
			if(!n.links.contains(this)) n.links.add(this);
		}
		
		@Override
		public String toString() {
			return x + " " + y;
		}
	}
}
