package logic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

import logic.MarchingSquares.Vec2;
import logic.MarchingSquares.VecPathArea;
import logic.cases.Case;
import logic.cases.CornerCase;
import logic.cases.Lcase;
import logic.cases.PigeonCase;
import logic.cases.TCase;
import logic.cases.VCase;
import logic.cases.YCase;
import main.Debug;
import main.Main;
import svg.SvgElement;

public class MulticolorsMarchingSquares {
	
	static final Vec4[][] vecs;
	static ArrayList<Case> cases = new ArrayList<Case>();

	boolean tCase = true;
	boolean yCase = true;
	boolean vCase = true;
	boolean lCase = true;
	boolean cornerCase = true;
	boolean givenamecase2 = true;
	boolean givenamecase3 = true;
	
	private boolean warnings = false;
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
		
		grid = new Node[w*2+1][h*2+1];
		colors = new int[w*2+1][h*2+1];
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
		if(givenamecase3) PigeonCase.apply(this);

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
	
	public ArrayList<VecPathArea> getSvgPaths() {
		ArrayList<VecPathArea> paths = new ArrayList<>();
		
		boolean[][] used = new boolean[w*2][h*2];
		
//		Debug.createFrame();
		int scale = 5;
		debug = new BufferedImage(w*2*scale, h*2*scale, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) debug.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int lx, ly;

		Vec2 lastEmpty = null;
		int warnsCount = -3;
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
					if(warnings && Math.abs(lastEmpty.x() - x) > 1) {
						if(warnsCount < 0 && warnings) System.err.println("x diff is > 1 : " + lastEmpty.x() + ";" + lastEmpty.y() + " & " + x + ";" + y);
						warnsCount++;
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
						lx = n.x;
						ly = n.y;
						if(next == null) break;
						if(next == nodes.get(0)) break;
					}
					nodes.add(n);

					nodes = simplify(nodes);
					
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
					
					Polygon polygon = new Polygon(xpoints, ypoints, nodes.size()); // to check points color and mark "used"
					int rgb = rgbs[x/2][y/2];

					// Searching the most frequent color from a shape (TODO: it's bad but working now)
					HashMap<Integer, Vec1> counter = new HashMap<Integer, Vec1>();
					int maxCount = 0;

					int area = 0;
					for (int uy = minY-1; uy <= maxY; uy++) {
						for (int ux = minX-1; ux <= maxX; ux++) {
							if(polygon.contains(ux, uy)) {
								area++;
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
					//element.attribute("fill", Colors.toHex(rgb)); // Colors.toHex(rgbs[x/2][y/2])
					element.attribute("fill", Colors.toHex(rgb));
					if(Colors.alpha(rgb) != 255){
//						element.attribute("fill-opacity", Colors.alpha(rgb));
					}
//					if(rgb != 0xFF) {
//						System.out.println(rgb);
//					} else {
//					}

					VecPathArea tobeadded = new VecPathArea(element, minX, maxX, minY, maxY, area);

					if(paths.isEmpty() || !paths.contains(tobeadded)) {
						if(!d.toString().equals("Z")) {
							paths.add(tobeadded);
						}
					}
				}
			}
		}
		if(warnings && warnsCount >= 0) System.err.println(warnsCount + " warings hidden");
//		try {
//			ImageIO.write(debug, "png", new File("debug.png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return paths;
	}

	public boolean hasNode(int x, int y) {
		return grid[x][y] != null;
	}


}
