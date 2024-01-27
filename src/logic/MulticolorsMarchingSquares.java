package logic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import logic.MarchingSquares.Vec2;
import logic.MarchingSquares.VecPathArea;
import main.Debug;
import main.Main;
import svg.SvgElement;

public class MulticolorsMarchingSquares {
	
	static final Vec4[][] vecs;

	boolean tCase = true;
	boolean yCase = true;
	
	
	static { // Generating cases
		vecs = new Vec4[256][];
		
		/* 1000 */
		create(new int[] {0,0,0,0}, new Vec4[] {}, false, false);
		/* 1000 */
		create(new int[] {1,0,0,0}, new Vec4[] {new Vec4(0,1, 1,0)}, false,	false);
		create(new int[] {1,0,0,0}, new Vec4[] {new Vec4(0,1, 1,0)}, true,	false);
		create(new int[] {1,0,0,0}, new Vec4[] {new Vec4(0,1, 1,0)}, false,	true);
		create(new int[] {1,0,0,0}, new Vec4[] {new Vec4(0,1, 1,0)}, true,	true);
//		/* 1100 */
		create(new int[] {1,1,0,0}, new Vec4[] {new Vec4(0,1, 1,1), new Vec4(1,1, 2,1)}, false,	false);
		create(new int[] {1,1,0,0}, new Vec4[] {new Vec4(0,1, 1,1), new Vec4(1,1, 2,1)}, false,	true);
		/* 1010 */
		create(new int[] {1,0,1,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(1,1, 1,2)}, false,	false);
		create(new int[] {1,0,1,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(1,1, 1,2)}, false,	true);
		/* 1200 */
		create(new int[] {1,2,0,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(0,1, 1,1), new Vec4(1,1, 2,1)}, false,	false);
		create(new int[] {1,2,0,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(0,1, 1,1), new Vec4(1,1, 2,1)}, false,	true);
		/* 1020 */
		create(new int[] {1,0,2,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(0,1, 1,1), new Vec4(1,1, 1,2)}, false,	false);
		create(new int[] {1,0,2,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(0,1, 1,1), new Vec4(1,1, 1,2)}, true,	false);
		/* 1001 */
		create(new int[] {1,0,0,1}, new Vec4[] {new Vec4(1,0, 0,1), new Vec4(1,2, 2,1)}, false,	false);
		create(new int[] {1,0,0,1}, new Vec4[] {new Vec4(1,0, 0,1), new Vec4(1,2, 2,1)}, false,	true);
		/* 1002 */
		create(new int[] {1,0,0,2}, new Vec4[] {new Vec4(1,0, 0,1), new Vec4(1,2, 2,1)}, false,	false);
		create(new int[] {1,0,0,2}, new Vec4[] {new Vec4(1,0, 0,1), new Vec4(1,2, 2,1)}, false,	true);
		/* 0123 */
		create(new int[] {0,1,2,3}, new Vec4[] {new Vec4(0,1, 1,1), new Vec4(1,1, 2,1), new Vec4(1,0, 1,1), new Vec4(1,1, 1,2)}, false,	false);
	}

	private final int w, h; // real size
	private Node[][] grid; // x2 + 1 size
	private int[][] rgbs; // in real size
	private int[][] colors; // x2 + 1 size
	
	public MulticolorsMarchingSquares(int[][] rgbs) {
		this.rgbs = rgbs;
		h = rgbs[0].length;
		w = rgbs.length;
	}
	
	/**
	 * Main generating
	 * @param save - save path
	 * @return self
	 */
	public MulticolorsMarchingSquares create(String save) {
		int scale = 5;
		BufferedImage debug = new BufferedImage(w*2*scale, h*2*scale, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) debug.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		grid = new Node[w*2+1][h*2+1];
		colors = new int[w*2+1][h*2+1];
		// Applying main cases
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				boolean outY = y < 0 || y+1 >= h;
				boolean outX = x < 0 || x+1 >= w;


				int[] mask = createMaskArray(new int[] {
						(x < 0 || y < 0) ? 0 : rgbs[x][y],
						(outX  || y < 0) ? 0 : rgbs[x+1][y],
						(x < 0 ||  outY) ? 0 : rgbs[x][y+1],
						(outX  ||  outY) ? 0 : rgbs[x+1][y+1]
				});

				int[] hasColor = new int[4];
				for (int i = 0; i < mask.length; i++) {
					hasColor[mask[i]] = 1;
				}
				int colorsCount = IntStream.of(hasColor).sum()-1;

				int key = toId(mask);

				Vec4[] vecs = new Vec4[MulticolorsMarchingSquares.vecs[key].length];

				colors[x*2][y*2] = Math.max(colorsCount, colors[x*2][y*2]);
				colors[x*2+1][y*2+1] = Math.max(colorsCount, colors[x*2+1][y*2+1]);
				
				g.setColor(Color.green.darker().darker());
				for (int i = 0; i < vecs.length; i++) {
					vecs[i] = MulticolorsMarchingSquares.vecs[key][i].copy().add(x*2, y*2);
//					g.drawLine(vecs[i].x1*scale, vecs[i].y1*scale, vecs[i].x2*scale, vecs[i].y2*scale);
					Node n1 = node(vecs[i].x1, vecs[i].y1);
					Node n2 = node(vecs[i].x2, vecs[i].y2);
					n1.link(n2);
//					colors[vecs[i].x1][vecs[i].y1] = Math.max(colorsCount, colors[vecs[i].x1][vecs[i].y1]);
//					colors[vecs[i].x2][vecs[i].y2] = Math.max(colorsCount, colors[vecs[i].x2][vecs[i].y1]);
				}
			}
		}
		// Post cases
		
		// Sharpen T-cases
		if(tCase)
		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
				Node n = grid[x][y];
				if(n == null) continue;
				if(n.links.size() != 3) continue;

				Node n0 = n.links.get(0);
				Node n1 = n.links.get(1);
				Node n2 = n.links.get(2);

				// is horizontal
				boolean h0 = y-n0.y == 0;
				boolean h1 = y-n1.y == 0;
				boolean h2 = y-n2.y == 0;

				if((x-n0.x)*(y-n0.y) != 0) continue;
				if((x-n1.x)*(y-n1.y) != 0) continue;
				if((x-n2.x)*(y-n2.y) != 0) continue;

				int hc = (h0?1:0) + (h1?1:0) + (h2?1:0);
				
				if(hc == 1) { // "|-" case
					Node r = h0 ? n0 : (h1 ? n1 : n2);
					
					if(r.links() != 2) continue;
					Node rr = r.get(0) == n ? r.get(1) : r.get(0);
					
					int rdx = r.x - rr.x;
					int rdy = r.y - rr.y;
					if(rdy == 0) continue;
					n.unlink(r);
					r.link(node(r.x + rdx, r.y + rdy)); // rr.link ?
				} else { // "T" case
					Node r = h0 ? (h1 ? n2 : n1) : n0; 
					
					if(r.links() != 2) continue;
					Node rr = r.get(0) == n ? r.get(1) : r.get(0);
					
					int rdx = r.x - rr.x;
					int rdy = r.y - rr.y;
					if(rdx == 0) continue;
					n.unlink(r);
					r.link(node(r.x + rdx, r.y + rdy)); // rr.link ?
				}
			}
		}

		// Sharpen Y-cases
		if(yCase)
		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
				Node n = grid[x][y];
				if(n == null) continue;
				if(n.links() != 3) continue;

				Node n0 = n.get(0);
				Node n1 = n.get(1);
				Node n2 = n.get(2);
				

				if(n0.links() <= 1 || n1.links() <= 1 || n2.links() <= 1) continue;
				
				if((x-n0.x)*(y-n0.y) != 0) continue;
				if((x-n1.x)*(y-n1.y) != 0) continue;
				if((x-n2.x)*(y-n2.y) != 0) continue;

				// Radicals
				Node nr0 = n0.get(0) == n ? n0.get(1) : n0.get(0);
				Node nr1 = n1.get(0) == n ? n1.get(1) : n1.get(0);
				Node nr2 = n2.get(0) == n ? n2.get(1) : n2.get(0);

				if(n0.links() != 2 || n1.links() != 2 || n1.links() != 2) continue;
				int diagonals = (n0.diagonal(nr0)?1:0) + (n1.diagonal(nr1)?1:0) + (n2.diagonal(nr2)?1:0);
				if(diagonals != 2) continue;

				g.setColor(new Color(0,0,255,50));
				g.fillOval(n.x*scale - scale/2, n.y*scale - scale/2, scale, scale);
				g.setColor(new Color(0,180,255,150));

				g.fillOval(n0.x*scale - scale/2, n0.y*scale - scale/2, scale, scale);
				g.fillOval(n1.x*scale - scale/2, n1.y*scale - scale/2, scale, scale);
				g.fillOval(n2.x*scale - scale/2, n2.y*scale - scale/2, scale, scale);

				g.setColor(new Color(0,255,255,150));

//				g.fillOval(nr0.x*scale - scale/2, nr0.y*scale - scale/2, scale, scale);
//				g.fillOval(nr1.x*scale - scale/2, nr1.y*scale - scale/2, scale, scale);
//				g.fillOval(nr2.x*scale - scale/2, nr2.y*scale - scale/2, scale, scale);
				
				/**
				 * 		R
				 * 		 \
				 * 		  R1   NS
				 * 		  |\  /  \
				 * 		  N-S1-S2-S3
				 * 		  |/        \
				 * 		  R2         SR
				 * 		 /
				 * 		R
				 */
				
				Node s2 = n0.diagonal(nr0) ? (n1.diagonal(nr1) ? nr2 : nr1) : nr0;
				g.setColor(new Color(255,0,255,50));
				g.fillOval(s2.x*scale - scale/2, s2.y*scale - scale/2, scale, scale);
				
				if(s2.links() != 2) continue;
				
				Node s3 = s2.get(0).contains(n) ? s2.get(1) : s2.get(0);
				g.setColor(new Color(255,0,127,50));
				g.fillOval(s3.x*scale - scale/2, s3.y*scale - scale/2, scale, scale);
				
				if(s3.links() != 2) continue;

				Node sr = s3.get(0) == s2 ? s3.get(1) : s3.get(0);
				g.fillOval(sr.x*scale - scale/2, sr.y*scale - scale/2, scale, scale);
				
				Node s1 = s2.get(0) == s3 ? s2.get(1) : s2.get(0);
				Node ns = node(s3.x*2 - sr.x, s3.y*2 - sr.y);

				g.setColor(new Color(255,0,255,150));

				g.fillOval(s1.x*scale - scale/2, s1.y*scale - scale/2, scale, scale);
				
				ns.link(s3);
				ns.link(s1);

				nr0.linkOneAvalible(s1,s2,s3);
				nr1.linkOneAvalible(n0,n1,n2);
				nr2.linkOneAvalible(n0,n1,n2);

				n0.linkOneAvalible(n1,n2);
				n1.linkOneAvalible(n0,n2);
				n2.linkOneAvalible(n0,n1);

//				nr1.link(s1);
//				nr2.link(s1);
				
				removeNode(s2);
				removeNode(n);
			}
		}
		
		// TODO: V-case
		
		// Borders

		for (int x = 0; x < w*2-2; x++) {
			node(x, 0).link(node(x+1, 0));
			node(x, h*2-2).link(node(x+1, h*2-2));
			removeNode(x, h*2);
			removeNode(x, h*2-1);
		}
		for (int y = 0; y < h*2-2; y++) {
			node(0, y).link(node(0, y+1));
			node(w*2-2, y).link(node(w*2-2, y+1));
			removeNode(w*2, y);
			removeNode(w*2-1, y);
		}
		

		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
//				g.setColor(new Color(Color.HSBtoRGB(colors[x][y]/5f, 1f, 1f)));
				g.setColor(new Color(255,255,255,100));
				g.fillRect(x*scale, y*scale, scale/5, scale/5);
				Node n = grid[x][y];
				if(n == null) continue;
				g.setColor(new Color(255,0,0,100));
				for (var l : n.links) {
					if(Math.abs(n.x-l.x) > 1) {
						g.setColor(new Color(0,255,0));
						System.err.println("Link x size >2: " + n.x + " " + n.y);
					}
					if(Math.abs(n.y-l.y) > 1) {
						g.setColor(new Color(0,0,255));
						System.err.println("Link y size >2");
					}
					g.drawLine(n.x*scale, n.y*scale, l.x*scale, l.y*scale);
				}
			}
		}
		g.dispose();
		
		try {
			ImageIO.write(debug, "png", new File("debug/tmp-" + save + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}

	/**
	 * @param x - x-coordinate in {@link #grid}
	 * @param y - y-coordinate in {@link #grid}
	 * @return node by coordinates in {@link #grid} (creates new if it <code>null<code>)
	 */
	private Node node(int x, int y) {
		if(grid[x][y] == null) grid[x][y] = new Node(x, y);
		return grid[x][y];
	}

	/**
	 * Removing node and it links by coordinates in {@link #grid}
	 * @param x - x-coordinate in {@link #grid}
	 * @param y - y-coordinate in {@link #grid}
	 */
	private void removeNode(int x, int y) {
		if(grid[x][y] == null) return;
		grid[x][y].removeLinks();
		grid[x][y] = null;
	}

	/**
	 * Removing node and it links
	 */
	private void removeNode(Node node) {
		removeNode(node.x, node.y);
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
	
	public static void create(int[] points, Vec4[] vs, boolean flipX, boolean flipY) {
		if(flipX) {
			points[0] += points[1];
			points[1] = points[0] - points[1];
			points[0] -= points[1];

			points[2] += points[3];
			points[3] = points[2] - points[3];
			points[2] -= points[3];
			for (Vec4 v : vs) v.flipX(2);
		}
		if(flipY) {
			points[0] += points[2];
			points[2] = points[0] - points[2];
			points[0] -= points[2];

			points[1] += points[3];
			points[3] = points[1] - points[3];
			points[1] -= points[3];
			for (Vec4 v : vs) v.flipY(2);
		}
		setAll(points, vs);
	}

	private static String createMask(int[] ps) {
		String mask = Arrays.toString(ps);
		mask = mask.substring(1, mask.length()-1).replaceAll(", ", "");
		char l = 'a';
		for (int i = 0; i < mask.length(); i++) {
			char c = mask.charAt(i);
			if(c == 'a' || c == 'b' || c == 'c' || c == 'd') continue;
			mask = mask.replace(c, l);
			l++;
		}
		return mask;
	}
	

	private static int[] createMaskArray(int[] mask) {
		boolean[] changed = new boolean[mask.length];
		int index = 0;
		for (int i = 0; i < changed.length; i++) {
			if(changed[i]) continue;
			int e = mask[i];
			for (int j = 0; j < changed.length; j++) {
				if(mask[j] != e) continue;
				mask[j] = index;
				changed[j] = true;
			}
			index++;
		}
		return mask;
	}

	/*
	 *  TODO: 
	 *  Rewrite this terrible code
	 *  (although the speed is not important here but I want to rewrite it normally)
	 */
	private static void setAll(int[] ps, Vec4[] value) {
		String mask = createMask(ps);
		for (int i0 = 0; i0 < 4; i0++) for (int i1 = 0; i1 < 4; i1++) for (int i2 = 0; i2 < 4; i2++) for (int i3 = 0; i3 < 4; i3++) {
			int _ps[] = {i0,i1,i2,i3};
			String _mask = createMask(_ps);
			if(mask.equals(_mask)) {
				vecs[toId(i0, i1, i2, i3)] = value;
			}
		}
	}
	
	private static int toId(int... vs) {
		return vs[0] + vs[1]*4 + vs[2]*16 + vs[3]*64;
	}
	
	private static Vec2[] es = {
//			new Vec2(-1, -1), new Vec2(0, -1), new Vec2(1, -1),
//			new Vec2(-1,  0), 				   new Vec2(1,  0),
//			new Vec2(-1,  1), new Vec2(0,  1), new Vec2(1,  1)
			new Vec2(-1,  0), 
			new Vec2(-1, -1), 
			new Vec2(0, -1), 
			new Vec2(1, -1),  
			new Vec2(1,  0), 
			new Vec2(1,  1), 
			new Vec2(0,  1), 
			new Vec2(-1,  1)
	};

	
	private static String[] esNames = {
//			"top left", "top", "top right",
//			"left", "right",
//			"down left", "down", "down right"
			"left", "top left", "top", "top right", "right", "down right", "down", "down left"
	};
	
	
	private static int angle(int dx, int dy) {
		for (int i = 0; i < es.length; i++) {
			if(es[i].x() == dx && es[i].y() == dy) return i;
		}
		System.err.println("Angle not found: " + dx + " " + dy);
		return -1;
	}
	
	/**
	 * Search node links by clockwise
	 * @param node
	 * @param angle - start search angle
	 * @return null if not found
	 */
	private Node findNextNode(Node node, int angle) {
//		if(angle == -1) return null;
		for (int a = 0; a < es.length; a++) {
			int aa = ((a+angle)%es.length+es.length)%es.length;
			int x = node.x + es[aa].x();
			int y = node.y + es[aa].y();
			for (Node n : node.links) {
				if(n.x == x && n.y == y) return n;
			}
		}
		return null;
	}

	
	/**
	 * Search node links by counterclockwise
	 * @param node
	 * @param angle - start search angle
	 * @return null if not found
	 */
	private Node findNextNodeBack(Node node, int angle) {
//		if(angle == -1) {
//			System.out.println("Wrong angle");
//			return null;
//		}
		for (int a = 0; a < es.length; a++) {
			int aa = ((angle-a)%es.length+es.length)%es.length;
			int x = node.x + es[aa].x();
			int y = node.y + es[aa].y();
//			debug.setRGB(x, y, Color.HSBtoRGB(a/8f, .5f, .5f));
			if(x < 0 || y < 0) continue;
			if(grid[x][y] != null) {
				if(grid[x][y].contains(node)) return grid[x][y];
			}
//			for (Node n : node.links) {
//				if(n.x == x && n.y == y) return n;
//			}
		}
		System.out.println("Nodes not found");
		return null;
	}
	BufferedImage debug;
	
	public ArrayList<VecPathArea> getSvgPaths() {
		ArrayList<VecPathArea> paths = new ArrayList<>();
		
		boolean[][] used = new boolean[w*2][h*2];
		
//		Debug.createFrame();
		int scale = 5;
//		debug = new BufferedImage(w*2*scale, h*2*scale, BufferedImage.TYPE_INT_RGB);
//		Graphics2D g = (Graphics2D) debug.getGraphics();
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int lx, ly;

		Vec2 lastEmpty = null;
		for (int y = 0; y < h*2-1; y++) {
			for (int x = 0; x < w*2-1; x++) {
				if(grid[x][y] == null) {
					if(used[x][y]) continue;
					lastEmpty = new Vec2(x, y);
					continue;
				}
				if(grid[x][y].links() != 2) continue;
				if(grid[x][y].links() > 2) continue;
				if(lastEmpty != null) {
					if(Math.abs(lastEmpty.x() - x) > 1) {
						System.err.println("x diff is > 1 : " + lastEmpty.x() + " " + x);
						lastEmpty = null;
						continue;
					}
					ArrayList<Node> nodes = new ArrayList<Node>();
					Node n = grid[x][y];
					Node next = findNextNode(n, angle(-lastEmpty.x()+x, -lastEmpty.y()+y));
					lastEmpty = null;
					lx = x;
					ly = y;
					while (true) {
						if(next == null) break;
						nodes.add(n);
						int angle = angle(n.x-next.x, n.y-next.y);
						if(angle == -1) break;
						if(next.links() == 2) {
							Node tmp = next.get(0) == n ? next.get(1) : next.get(0);
							n = next;
							next = tmp;
						} else {
							n = next;
							next = findNextNodeBack(next, angle-1);
						}
//						g.setColor(Color.black);
//						g.setStroke(new BasicStroke(2f));
//						g.drawLine(n.x*scale-1, n.y*scale-1, lx*scale, ly*scale);
//						Debug.repaint(debug);
						lx = n.x;
						ly = n.y;
						if(next == null) break;
						if(next == nodes.get(0)) break;
					}
					nodes.add(n);
					
					StringBuilder d = new StringBuilder();
					int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
					int maxX = 0, maxY = 0;
					int[] xpoints = new int[nodes.size()], ypoints = new int[nodes.size()];
					for (int i = 0; i < nodes.size(); i++) {
						n = nodes.get(i);
						xpoints[i] = n.x;
						ypoints[i] = n.y;
						used[n.x][n.y] = true;
						d.append(i == 0 ? 'M' : 'L');
						d.append(Strings.toString((n.x)*Main.scale));
						d.append(',');
						d.append(Strings.toString((n.y)*Main.scale));
						d.append(' ');

						minX = Math.min(minX, n.x);
						maxX = Math.max(maxX, n.x);

						minY = Math.min(minY, n.y);
						maxY = Math.max(maxY, n.y);
					}
					d.append('Z');
					
					Polygon polygon = new Polygon(xpoints, ypoints, nodes.size()); // to check points color and mark "used"
					int rgb = rgbs[x/2][y/2];

					// Searching the most frequent color from a shape (TODO: it's bad but working now)
					HashMap<Integer, Vec1> counter = new HashMap<Integer, Vec1>();
					int maxCount = 0;
					
					for (int uy = minY-1; uy <= maxY; uy++) {
						for (int ux = minX-1; ux <= maxX; ux++) {
							if(polygon.contains(ux, uy)) {
								int key = rgbs[(ux+1)/2][(uy-0)/2];
								Vec1 count = counter.get(key);
								if(count == null) {
									count = new Vec1(0);
									counter.put(key, count);
								}
								count.add(1);
								maxCount = Math.max(maxCount, count.i);
//								used[ux][uy] = true;
							}
						}
					}
					
					for (int key : counter.keySet()) {
						Vec1 v1 = counter.get(key);
						if(v1.i == maxCount) {
							rgb = key;
							break;
						}
					}
					
					for (int uy = minY-1; uy <= maxY; uy++) {
						for (int ux = minX-1; ux <= maxX; ux++) {
							if(polygon.contains(ux, uy)) {
//								if(grid[ux][uy] == null) continue;
								int key = rgbs[(ux)/2][(uy-0)/2];
								if(key == rgb) used[ux][uy] = true;
							}
						}
					}
					
					
					SvgElement element = new SvgElement("path");
					element.attribute("d", d);
//					element.attribute("x", x/2f);
//					element.attribute("y", y/2f);
//					element.attribute("width", .5f);
//					element.attribute("filter", "drop-shadow(1px 1px 1px black)");
					element.attribute("fill", Colors.toHex(rgb)); // Colors.toHex(rgbs[x/2][y/2])
					
					paths.add(new VecPathArea(element, x, x, y, y, 1));
				}
			}
		}
//		try {
//			ImageIO.write(debug, "png", new File("debug.png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return paths;
	}
	
}
