package logic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import main.Main;
import svg.SvgElement;

public class MarchingSquares {

	public static boolean drawDebug = false;
	
	final boolean[][] bs;
	final int w, h;
	
	Integer[][] colors = null;
	
	public MarchingSquares(boolean[][] bs) {
		this.bs = bs;
		h = bs[0].length;
		w = bs.length;
	}

	public MarchingSquares colorsMap(Integer[][] colorsMap) {
		colors = colorsMap;
		return this;
	}
	
	static final int scale = 10;
	
	public Color color = Color.white;
	public String save = "debug";
	
	int rgb = 0;
	
	public MarchingSquares create(int rgb, String save, Color color) {
		this.save = save;
		this.rgb = rgb;
		
		BufferedImage debug = null;
		if(drawDebug) debug = new BufferedImage(w*scale*2, h*scale*2, BufferedImage.TYPE_INT_RGB);
		
		grid = new Node[w*2][h*2];
		
		Graphics2D g = drawDebug ? (Graphics2D) debug.getGraphics() : null;
		if(drawDebug) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Creating normal vectors for each pixel
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

				if(drawDebug) {
					g.setStroke(new BasicStroke(scale/5f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
					g.setColor(Color.darkGray);
					g.drawRect(x*2*scale, y*2*scale, 2*scale, 2*scale);
				}
				
				if(vec1 == null && vec2 == null) continue; // Do not allow add null

				if(drawDebug) g.setStroke(new BasicStroke(scale/2f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
				if(vec1 != null) {
					vec1.add(x*2, y*2);
					Node a1 = node(vec1.x1, vec1.y1);
					Node a2 = node(vec1.x2, vec1.y2);
					a1.link(a2);
				}
				if(drawDebug) {
					g.setColor(color);
					g.setStroke(new BasicStroke(scale/2f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
				}
				if(vec2 != null) {
					vec2.add(x*2, y*2);
					
					Node b1 = node(vec2.x1, vec2.y1);
					Node b2 = node(vec2.x2, vec2.y2);
					b1.link(b2);
				}
			}
		}
		

		if(drawDebug) {
			g.setColor(Color.white);
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
			
			for (int y = 0; y < h*2; y++) {
				for (int x = 0; x < w*2; x++) {

					g.setColor(colors[x][y] == null ? Color.darkGray : new Color(colors[x][y]));

					g.fillRect((x)*scale - scale/4, (y)*scale - scale/4, scale/2, scale/2);

				}
			}
			g.setColor(color);
			
			g.dispose();
			
			try {
				File file = new File("debug/" + save + "-" + color.getRGB()  + ".png");
				file.mkdirs();
				ImageIO.write(debug, "png", file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return this;
	}

	static int counter = 0;
	
	public ArrayList<VecPathArea> getSvgPaths(int rgb) {
		ArrayList<VecPathArea> paths = new ArrayList<>();
		
		String rgba = Colors.toHex(rgb);
		
		while (true) {
			
			ArrayList<Node> nodes = nextPath();
			if(nodes == null) break;
			if(nodes.size() < 2) continue;

			int x1 = nodes.get(0).x;
			int x2 = nodes.get(1).x;
			int y1 = nodes.get(0).y;
			int y2 = nodes.get(1).y;

			int dx = x1-x2;
			int dy = y1-y2;

			dx = Math.min(1, Math.max(-1, dx));
			dy = Math.min(1, Math.max(-1, dy));
			
			double angle = Math.atan2(dy, dx);
			angle += Math.PI/2;

			dx = (int) Math.round(Math.cos(angle));
			dy = (int) Math.round(Math.sin(angle));

//			dx += 1;
//			dy += 1;
			
			if(colors[x1 + dx][y1 + dy] != null) {
				if(colors[x1 + dx][y1 + dy] != rgb) {
					continue;
				}
			}
			
//			float dx = nodes.get(0).x - nodes.get(1).x;
//			float dy = nodes.get(0).y - nodes.get(1).y;
//			double angle = Math.atan2(dy, dx);
//			angle += Math.PI;
			
			ArrayList<Vec2> vpath = new ArrayList<Vec2>();
			for (Node n : nodes) {
				vpath.add(new Vec2(n.x, n.y));
			}
			
			vpath = sharpen(vpath);
			vpath = simplify(vpath);
			// stroke=\"#000\" 
			
			StringBuilder d = new StringBuilder();
			int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
			int maxX = 0, maxY = 0;
			for (int i = 0; i < vpath.size(); i++) {
				Vec2 n = vpath.get(i);
				d.append(i == 0 ? 'M' : 'L');
				d.append(Strings.toString((n.x-1)*Main.scale));
				d.append(',');
				d.append(Strings.toString((n.y-1)*Main.scale));
				d.append(' ');

				minX = Math.min(minX, n.x);
				maxX = Math.max(maxX, n.x);

				minY = Math.min(minY, n.y);
				maxY = Math.max(maxY, n.y);
			}
			d.append('Z');

			int w = maxX - minX;
			int h = maxY - minY;
			
			SvgElement svg = new SvgElement("path")
					.attribute("fill", rgba)
					.attribute("d", d);
			
			paths.add(new VecPathArea(svg, minX, maxX, minY, maxY, w*h));
		}
		
		// Remove groups in groups
		
//		ArrayList<VecPathArea> single = new ArrayList<>();
//		for (VecPathArea path : paths) {
//			boolean isSingle = true;
//			for (VecPathArea check : paths) {
//				if(check == path) continue;
//				if(path.minX < check.minX && check.maxX < path.maxX) continue;
//				if(path.minY < check.minY && check.maxY < path.maxY) continue;
//				isSingle = false;
//				break;
//			}
//			
//			if(isSingle) {
//				single.add(path);
//			}
//		}
		
		return paths;
		
	}
	
	record VecPathArea(SvgElement svg, int minX, int maxX, int minY, int maxY, int boundsArea) {}
	
	boolean nullCheck = false;
	
	/**
	 * @param path - closed path of normals vectors
	 * @return
	 */
	private ArrayList<Vec2> sharpen(ArrayList<Vec2> path) {
		int limit = 10;
		boolean skipNext = false;
		while (true) {
			boolean needReturnd = false;
			ArrayList<Vec2> sharpen = new ArrayList<Vec2>();
			for (int i = 0; i < path.size(); i++) {
				Vec2 c1 = element(path, i);
				Vec2 c2 = element(path, i+1);

				int cdx = c1.x-c2.x;
				int cdy = c1.y-c2.y;

				if(!skipNext) {
					sharpen.add(new Vec2(c1.x, c1.y));
				}
				skipNext = false;

				int ca = cdx*cdy;

				Vec2 p = element(path, i-1);
				Vec2 n = element(path, i+2);

				int pdx = c1.x-p.x;
				int pdy = c1.y-p.y;

				int ndx = n.x-c2.x;
				int ndy = n.y-c2.y;

				int pa = pdx*pdy;
				int na = ndx*ndy;
				
				int x1 = c1.x + pdx;
				int y1 = c1.y + pdy;

				int x2 = c2.x - ndx;
				int y2 = c2.y - ndy;

				Vec2 vec = new Vec2(x1, y1);
				if(x1 == x2 && y1 == y2) {
					if(colors[x1][y1] == null || nullCheck) { //x1%2 == 1 && y1%2 == 1) { // TODO
						sharpen.add(vec);
						needReturnd = true;
					}
					continue;
				}

				
				
				if(pa == na) continue;
				if(pa == ca) continue;
				if(na == ca || pa == ca || pa == na)// continue;
				{
					Vec2 nn = element(path, i+3);
					
					int nndx = nn.x-n.x;
					int nndy = nn.y-n.y;
					int nna = nndx*nndy;

					if(nna == 0) continue;
					if(pa == 0) continue;
					if(ca != 0) continue;
					if(na != 0) continue;

					x1 = c1.x + pdx;
					if(x1 < 1 || x1 >= w*2) continue;
					y1 = c1.y + pdy;
					if(y1 < 1 || y1 >= h*2) continue;

					x2 = n.x - nndx;
					y2 = n.y - nndy;
					
					if(x1 == x2 && y1 == y2 && (colors[x1][y1] == null || nullCheck)) { // TODO
						skipNext = true;
						sharpen.add(new Vec2(x1, y1));
						needReturnd = true;
					}
					continue;
				}
				if(x1 < 0 || x1 >= w*2) continue;
				if(y1 < 0 || y1 >= h*2) continue;
				if(x2 < 0 || x2 >= w*2) continue;
				if(y2 < 0 || y2 >= h*2) continue;

//				if((x1%2 == 1 && y1%2 == 1) || (x2%2 == 1 && y2%2 == 1)) {
				if(colors[x1][y1] == null || nullCheck) { // TODO
					sharpen.add(new Vec2(x1, y1));
//					needReturnd = true;
				}
				if(colors[x2][y2] == null || nullCheck) { // TODO
					sharpen.add(new Vec2(x2, y2));
//					needReturnd = true;
				}

				/*
				Vec2 nn = element(path, i+3);
				int nndx = nn.x-n.x;
				int nndy = nn.y-n.y;
				int nna = nndx*nndy;
				if(nna == 0) continue;
				if(pa == 0) continue;
				if(ca != 0) continue;
				if(na != 0) continue;
				int x3 = c1.x + pdx;
				if(x3 < 1 || x3 >= w*2) continue;
				int y3 = c1.y + pdy;
				if(y3 < 1 || y3 >= h*2) continue;
				int x4 = n.x - nndx;
				int y4 = n.y - nndy;
				if(x3 == x4 && y3 == y4 && colors[x3][y3] == null) {
//					sharpen.remove(vec);
					skepNext = true;
					sharpen.add(new Vec2(x3, y3));
					needReturnd = true;
					continue;
				}
				//*/
//				}
				
//				if(x2%2 == 1 && y2%2 == 1) {
//				}
			}
			path = sharpen;
			if(!needReturnd) break;
			limit--;
			if(limit < 0) break;
		}
		return path;
	}
	
	public void extendVectors(ArrayList<Vec2> path, int i1, int i2) {
		Vec2 c1 = element(path, i1);
		Vec2 c2 = element(path, i1+1);
		
		Vec2 n1 = element(path, i2);
		Vec2 n2 = element(path, i2+1);
	}
	
	private <T> T element(ArrayList<T> array, int index) {
		if(index >= 0) return array.get(index%array.size());
		return array.get((index%array.size()+array.size())%array.size());
	}
	
	/**
	 * @param path - 
	 * @return path without unnecessary points 
	 */
	private ArrayList<Vec2> simplify(ArrayList<Vec2> path) {
		ArrayList<Vec2> simple = new ArrayList<Vec2>();

        int ldx = 0; // last dX
        int ldy = 0; // last dY
        
        for (int i = 0; i < path.size(); i++) {
        	Vec2 c = path.get(i); // current
        	Vec2 n = path.get((i+1)%path.size()); // next

            int dx = c.x - n.x;
            int dy = c.y - n.y;

            if(dx*ldy != ldx*dy || i == 0) { // Angle not same
            	simple.add(c);
            }
            ldx = dx;
            ldy = dy;
        }
		return simple;
	}

	/**
	 * @return next closed path or <code>null<code> if it not found
	 */
	private ArrayList<Node> nextPath() {
		for (int y = 0; y < h*2; y++) {
			for (int x = 0; x < w*2; x++) {
				if(grid[x][y] == null) continue;
				if(grid[x][y].links.size() != 2) continue;
				ArrayList<Node> path = new ArrayList<Node>();
				
				Node start = grid[x][y];
				
				Node c = start;
				Node last = null;
				while (true) {
					if(path.contains(c)) {
						System.err.println("Duplicate");
						return path;
					}
					path.add(c);
					for (Node link : c.links) {
						if(link == c) continue;
						if(link == last) continue;
						last = c;
						c.unlink(link);
						c = link;
						break;
					}
					if(c == start) return path;
				}
			}
		}
		return null;
	}
	
	/**
	 * @param x - x-coordinate in {@link #grid}
	 * @param y - y-coordinate in {@link #grid}
	 * @return node by coordinates in {@link #grid} (creates new if it <code>null<code>
	 */
	private Node node(int x, int y) {
		if(grid[x][y] == null) grid[x][y] = new Node(x, y);
		return grid[x][y];
	}
	
	record Vec2(int x, int y) {}

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
		
		public void link(Node n) {
			if(!links.contains(n)) links.add(n);
			if(!n.links.contains(this)) n.links.add(this);
		}
		
		@Override
		public String toString() {
			return x + " " + y;
		}
	}
	
	
	/**
	 * @deprecated
	 */
	public String toSvgGroup(StringBuilder svg) {
		svg.append(
				"""
				<g fill="@" stroke-width="0" stroke="#333" stroke-linecap="round" stroke-linejoin="round">
				""".replaceFirst("@", "rgba(" + color.getRed() + " " + color.getGreen() + " " + color.getBlue() + ")")); //  / 90%
		
		while (true) {
			
			ArrayList<Node> nodes = nextPath();
			if(nodes == null) break;
			
			System.out.println(++counter + ") Paths: " + nodes.size());
			
			ArrayList<Vec2> vpath = new ArrayList<Vec2>();
			for (Node n : nodes) {
				vpath.add(new Vec2(n.x, n.y));
			}
			
			vpath = sharpen(vpath);
			
			System.out.println(counter + "] sharpen paths: " + nodes.size());
			
			vpath = simplify(vpath);
			
			svg.append("\n\t<path d=\"");
			for (int i = 0; i < vpath.size(); i++) {
				Vec2 n = vpath.get(i);
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
}
