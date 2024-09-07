package logic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import logic.MarchingSquares.Vec2;
import logic.cases.Case;
import logic.cases.CornerCase;
import logic.cases.GeneralYCase;
import logic.cases.Lcase;
import logic.cases.PigeonCase;
import logic.cases.TCase;
import logic.cases.VCase;
import logic.cases.YCase;
import main.Debug;

public class MulticolorsMarchingSquares {
	
	static final Vec4[][] vecs;
	static ArrayList<Case> cases = new ArrayList<Case>();

	public boolean tCase = true;
	public boolean yCase = true;
	public boolean vCase = true;
	public boolean lCase = true;
	public boolean cornerCase = true;
	public boolean givenamecase2 = true;
	public boolean generalYCase = true;
	@Deprecated 
	public boolean pigeonCase = false; // Supersized by generalYcase 
	
	public boolean debugImage = new File("debug").exists();
	
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
		
		for (var f : new File("cases").listFiles()) {
			try {
				cases.add(new Case(f));
			} catch (NumberFormatException | IOException e) {
				System.err.println("Err to load case: " + f);
				e.printStackTrace();
			}
		}
	}

	public final int w, h; // real size
	public Node[][] grid; // x2 + 1 size
	public int[][] rgbs; // in real size
	public int[][] colors; // x2 + 1 size
	
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
		if(debugImage) Debug.image(w*2, h*2, scale);
		
		grid = new Node[gridWidth()][gridHeight()];
		colors = new int[gridWidth()][gridHeight()];
		// Applying main cases
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				boolean outY = y+1 >= h;
				boolean outX = x+1 >= w;
				int[] mask = createMaskArray(new int[] {
                        rgbs[x][y],
						outX ? 1 : rgbs[x+1][y],
						outY ? 1 : rgbs[x][y+1],
						outX || outY ? 1 : rgbs[x+1][y+1]
				});
				int[] hasColor = new int[4];
				for (int i = 0; i < mask.length; i++) {
					hasColor[mask[i]] = 1;
				}
				int colorsCount = IntStream.of(hasColor).sum()-1;
				int key = toId(mask);
				Debug.rgb(rgbs[x][y]);
				Debug.fillRect(x*2, y*2, 1, 1);
				Debug.rgb(outX ? -1 : rgbs[x+1][y]);
				Debug.fillRect(x*2+1, y*2, 1, 1);
				Debug.rgb(outY ? -1 : rgbs[x][y+1]);
				Debug.fillRect(x*2, y*2+1, 1, 1);
				Debug.rgb(outX || outY ? -1 : rgbs[x+1][y+1]);
				Debug.fillRect(x*2+1, y*2+1, 1, 1);

				Debug.color(Color.DARK_GRAY);
				Debug.drawRect(x*2, y*2, 2, 2);
				
				Vec4[] vecs = new Vec4[MulticolorsMarchingSquares.vecs[key].length];
				colors[x*2][y*2] = colorsCount;
				colors[x*2+1][y*2+1] = colorsCount;
				Debug.color(Color.green.darker().darker());
				for (int i = 0; i < vecs.length; i++) {
					vecs[i] = MulticolorsMarchingSquares.vecs[key][i].copy().add(x*2, y*2);
					Node n1 = node(vecs[i].x1, vecs[i].y1);
					Node n2 = node(vecs[i].x2, vecs[i].y2);
					n1.link(n2);
				}
			}
		}
		// Post cases
		
		// Sharpen T-cases
		if(tCase) TCase.apply(this);
		for (int i = 0; i < cases.size(); i++) {
//			cases.get(i).apply(this);	
		}
		// Sharpen Y-cases
		if(yCase) YCase.apply(this);
		if(vCase) VCase.apply(this);
		if(lCase) Lcase.apply(this);
		// Sharpen "\_/" cases
		if(cornerCase) CornerCase.apply(this);
		// Sharpen "|\"-cases
		if(givenamecase2)
		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
				Node n = grid[x][y];

				if(n == null) continue;
				if(!(n.links() == 2 || n.links() == 3)) continue;

				Node n1 = n.get(0);
				Node n2 = n.get(1);

				/*
				//TODO
				if(n.links() == 3) {
					if(!n.diagonal(n1)){
						n1 = n.get(2);
					}
					if(!n.diagonal(n2)){
						n2 = n.get(2);
					}
				}
				*/

				if(!n.diagonal(n1)) continue;
				if(!n.diagonal(n2)) continue;

				if(n1.links() != 2) continue;
				if(n2.links() != 2) continue;

				Node r1 = n1.next(n);
				Node r2 = n2.next(n);

				if(n1.diagonal(n2) || r1.diagonal(r2)) continue;
				if(n2.diagonal(r2) == n1.diagonal(r1)) continue;

				if(n1.diagonal(r1)) {
					var tmp = n1;
					n1 = n2;
					n2 = tmp;

					tmp = r1;
					r1 = r2;
					r2 = tmp;
				}

				int dx1 = r1.x - n1.x;
				int dy1 = r1.y - n1.y;

				int dx2 = r2.x - n2.x;
				int dy2 = r2.y - n2.y;

				Node ns = node(n.x - dx2, n.y - dy2);
				Node ns2 = node(n1.x - dx1, n1.y - dy1);

				n1.unlink(n);
				n.link(ns);
				ns.link(ns2);
				ns2.link(n1);

				/**
				 * 		   NS             NS
				 * 		   |  \           |  \
				 * 		  NS2  N         NS2  N
				 * 		   | /  \         | /  \
				 * 		  N1    N2       N2    N1
				 * 		   |      \       |     \
				 * 		  R1      R2     R2     R1
				 */
			}
		}
		
		// Sharpen ᛉ-cases
		if(pigeonCase) PigeonCase.apply(this);
        if(generalYCase) GeneralYCase.apply(this);

		// Borders

		int left = 1;
		int top = 1;
		int right = w*2-3;
		int bottom = h*2-3;
		
		for (int x = left; x < right; x++) {
			node(x, top).link(node(x+1, top));
			node(x, bottom).link(node(x+1, bottom));
//			removeNode(x, 0);
//			removeNode(x, 1);
//			removeNode(x, 2);
			removeNode(x, h*2);
			removeNode(x, h*2-1);
			removeNode(x, h*2-2);
		}
		for (int y = top; y < bottom; y++) {
			node(left, y).link(node(left, y+1));
			node(right, y).link(node(right, y+1));
			removeNode(w*2, 0);
			removeNode(w*2, y);
			removeNode(w*2-1, y);
			removeNode(w*2-2, y);
		}
		removeNode(right+2,bottom+2);
		removeNode(right+1,bottom+2);
		removeNode(right+2,bottom+1);

		node(left, top+1).unlink(node(left+1, top));
		node(right,top+1).unlink(node(right-1, top));

		node(left, bottom-1).unlink(node(left+1, bottom));
		node(right,bottom-1).unlink(node(right-1, bottom));

		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
//				g.setColor(new Color(Color.HSBtoRGB(colors[x][y]/5f, 1f, 1f)));
				Debug.color(255,255,255,100);
				Debug.fillRect(x, y, 1f/5f, 1f/5f);
				Node n = grid[x][y];
				if(n == null) continue;
				Debug.color(255,0,0,100);
				for (var l : n.links) {
					if(Math.abs(n.x-l.x) > 1) {
						Debug.color(0,255,0);
						System.err.println("Link x size >2: " + n.x + " " + n.y);
					}
					if(Math.abs(n.y-l.y) > 1) {
						Debug.color(0,0,255);
						System.err.println("Link y size >2");
					}
					Debug.line(n, l);
				}
			}
		}
		Debug.write("debug/tmp-" + save + ".png");
		return this;
	}

	public int gridHeight() {
		return h*2+1;
	}

	public int gridWidth() {
		return w*2+1;
	}

	/**
	 * @param x - x-coordinate in {@link #grid}
	 * @param y - y-coordinate in {@link #grid}
	 * @return node by coordinates in {@link #grid} (creates new if it <code>null<code>)
	 */
	public Node node(int x, int y) {
		if(grid[x][y] == null) grid[x][y] = new Node(x, y);
		return grid[x][y];
	}

	/**
	 * Removing node and it links by coordinates in {@link #grid}
	 * @param x - x-coordinate in {@link #grid}
	 * @param y - y-coordinate in {@link #grid}
	 */
	public void removeNode(int x, int y) {
		if(grid[x][y] == null) return;
		grid[x][y].removeLinks();
		grid[x][y] = null;
	}

	/**
	 * Removing node and it links
	 */
	public void removeNode(Node node) {
		removeNode(node.x, node.y);
	}

	/**
	 * 
	 *
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
	 * <pre>
	 * Points:{@code
	 * [0][1]
	 * [2][3]}
	 * @author Agzam4
	 */
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
		if(dx > 1) dx = 1;
		if(dy > 1) dy = 1;
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

	/**
	 * @param path -
	 * @return path without unnecessary points
	 */
	private ArrayList<Node> simplify(ArrayList<Node> path) {
		ArrayList<Node> simple = new ArrayList<Node>();

		int ldx = 0; // last dX
		int ldy = 0; // last dY

		// Initialize ldx and ldy with the first element
		if(path.size() > 0) {
			Node first = path.get(0);
			Node second = path.get(path.size()-1);
			ldx = first.x - second.x;
			ldy = first.y - second.y;
		}

		for (int i = 0; i < path.size(); i++) {
			Node c = path.get(i); // current
			Node n = path.get((i+1) % path.size()); // next

			int dx = c.x - n.x;
			int dy = c.y - n.y;

			if(dx * ldy != ldx * dy) { // Angle not same
				simple.add(c);
			}
			ldx = dx;
			ldy = dy;
		}

		return simple;
	}
	
	public boolean hasNode(int x, int y) {
		return grid[x][y] != null;
	}


}
