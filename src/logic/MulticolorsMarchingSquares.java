package logic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

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

	static final Vec4[][] vecs; // [id 256][lines ...]
	static final int[][][] vecsColors; // [id 256][lines ...][inner, outer]
	static ArrayList<Case> cases = new ArrayList<Case>();

	public boolean tCase = false;
	public boolean yCase = false;
	public boolean vCase = false;
	public boolean lCase = false;
	public boolean cornerCase = false;
	public boolean givenamecase2 = false;
	public boolean generalYCase = false;
	@Deprecated 
	public boolean pigeonCase = false; // Supersized by generalYcase 
	
	public boolean debugImage = new File("debug").exists();
	
	static { // Generating cases
		vecs = new Vec4[256][];
		vecsColors = new int[256][][];
		
		/* 1000 */
		create(new int[] {0,0,0,0}, new Vec4[] {}, new Vec2[] {}, false, false);
		/* 1000 */
		create(new int[] {1,0,0,0}, new Vec4[] {new Vec4(0,1, 1,0)}, new Vec2[] {new Vec2(0,3)}, false, false);
		create(new int[] {1,0,0,0}, new Vec4[] {new Vec4(0,1, 1,0)}, new Vec2[] {new Vec2(0,3)}, true,	 false);
		create(new int[] {1,0,0,0}, new Vec4[] {new Vec4(0,1, 1,0)}, new Vec2[] {new Vec2(0,3)}, false, true);
		create(new int[] {1,0,0,0}, new Vec4[] {new Vec4(0,1, 1,0)}, new Vec2[] {new Vec2(0,3)}, true,	 true);
//		/* 1100 */
		create(new int[] {1,1,0,0}, new Vec4[] {new Vec4(0,1, 1,1), new Vec4(1,1, 2,1)}, new Vec2[] {new Vec2(0,2),new Vec2(1,3)}, false, false);
		create(new int[] {1,1,0,0}, new Vec4[] {new Vec4(0,1, 1,1), new Vec4(1,1, 2,1)}, new Vec2[] {new Vec2(0,2),new Vec2(1,3)}, false, true);
		/* 1010 */
		create(new int[] {1,0,1,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(1,1, 1,2)}, new Vec2[] {new Vec2(1,0),new Vec2(1,0)}, false, false);
		create(new int[] {1,0,1,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(1,1, 1,2)}, new Vec2[] {new Vec2(1,0),new Vec2(1,0)}, false, true);
		/* 1200 */
		create(new int[] {1,2,0,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(0,1, 1,1), new Vec4(1,1, 2,1)}, new Vec2[] {new Vec2(1,0),new Vec2(0,2),new Vec2(1,3)}, false,	false);
		create(new int[] {1,2,0,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(0,1, 1,1), new Vec4(1,1, 2,1)}, new Vec2[] {new Vec2(1,0),new Vec2(0,2),new Vec2(1,3)}, false,	true);
		/* 1020 */
		create(new int[] {1,0,2,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(0,1, 1,1), new Vec4(1,1, 1,2)}, new Vec2[] {new Vec2(1,0),new Vec2(0,2),new Vec2(3,2)}, false,	false);
		create(new int[] {1,0,2,0}, new Vec4[] {new Vec4(1,0, 1,1), new Vec4(0,1, 1,1), new Vec4(1,1, 1,2)}, new Vec2[] {new Vec2(1,0),new Vec2(0,2),new Vec2(3,2)}, true,	false);
		/* 1001 */
		create(new int[] {1,0,0,1}, new Vec4[] {new Vec4(1,0, 0,1), new Vec4(1,2, 2,1)}, new Vec2[] {new Vec2(1,0),new Vec2(1,0)}, false, false);
		create(new int[] {1,0,0,1}, new Vec4[] {new Vec4(1,0, 0,1), new Vec4(1,2, 2,1)}, new Vec2[] {new Vec2(1,0),new Vec2(1,0)}, false, true);
		/* 1002 */
		create(new int[] {1,0,0,2}, new Vec4[] {new Vec4(1,0, 0,1), new Vec4(1,2, 2,1)}, new Vec2[] {new Vec2(1,0),new Vec2(1,3)}, false, false);
		create(new int[] {1,0,0,2}, new Vec4[] {new Vec4(1,0, 0,1), new Vec4(1,2, 2,1)}, new Vec2[] {new Vec2(1,0),new Vec2(1,3)}, false, true);
		/* 0123 */
		create(new int[] {0,1,2,3}, new Vec4[] {new Vec4(0,1, 1,1), new Vec4(1,1, 2,1), new Vec4(1,0, 1,1), new Vec4(1,1, 1,2)}, 
									new Vec2[] {new Vec2(0,2),		new Vec2(1,3),		new Vec2(1,0),		new Vec2(3,2)}, false,	false);
		
		for (var f : new File("cases").listFiles()) {
			try {
				cases.add(new Case(f));
			} catch (NumberFormatException | IOException e) {
				System.err.println("Err to load case: " + f);
				e.printStackTrace();
			}
		}
		
		Color[] colors = {new Color(255,0,0),new Color(127,255,0),new Color(0,255,255),new Color(127,0,255)};
		float iScale = 16;
		Debug.image(2*16+1, 2*16+1, (int) iScale*2);
		Debug.g.setStroke(new java.awt.BasicStroke(3f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.CAP_ROUND));
		for (int i1 = 0; i1 < 4; i1++) {
			for (int i2 = 0; i2 < 4; i2++) {
				for (int i3 = 0; i3 < 4; i3++) {
					for (int i4 = 0; i4 < 4; i4++) {
						int id = toId(i1,i2,i3,i4);
						float ix = id%16*(2 + 1f/iScale);
						float iy = id/16*(2 + 1f/iScale);
						
//						Debug.image(ix, iy, 10);	
						Debug.color(colors[i1]);
						Debug.fillRect(ix,iy, 1, 1);
						Debug.color(colors[i2]);
						Debug.fillRect(ix+1,iy, 1, 1);
						Debug.color(colors[i3]);
						Debug.fillRect(ix,iy+1, 1, 1);
						Debug.color(colors[i4]);
						Debug.fillRect(ix+1,iy+1, 1, 1);
						Debug.color(0,0,0,220);
						Debug.fillRect(ix,iy,2,2);
						
						Color[] lColors = {colors[i1],colors[i2],colors[i3],colors[i4]};
						
						for (int vi = 0; vi < vecs[id].length; vi++) {
							Vec4 v = vecs[id][vi];
							if(vecsColors[id] != null && vecsColors[id].length != 0) {
								for (int i = 0; i <= 1; i++) {
									Vec4 line = v.copy();
									float x1 = ix + v.x1;
									float x2 = ix + v.x2;
									float y1 = iy + v.y1;
									float y2 = iy + v.y2;
									double angle = Math.atan2(y1-y2, x1-x2);
									angle += i == 0 ? Math.PI/4f : -Math.PI/4f;
									float delta = 1f/iScale;
									x1 += Math.cos(angle)*delta;
									x2 += Math.cos(angle)*delta;
									y1 += Math.sin(angle)*delta;
									y2 += Math.sin(angle)*delta;
									
									Debug.color(lColors[vecsColors[id][vi][i]]);
									Debug.line(x1,y1,x2,y2);
								}
								System.out.println(Arrays.toString(vecsColors[id][vi]) + "\t" + Arrays.toString(lColors) + "\t" + i1 + " " + i2 + " " + i3 + " " + i4 + "\t#" + id);
							}
						}
//						Debug.image(2, 2, 10);	
//						Debug.color(colors[i1]);
//						Debug.fillRect(0, 0, 1, 1);
//						Debug.color(colors[i2]);
//						Debug.fillRect(1, 0, 1, 1);
//						Debug.color(colors[i3]);
//						Debug.fillRect(0, 1, 1, 1);
//						Debug.color(colors[i4]);
//						Debug.fillRect(1, 1, 1, 1);
//						Debug.color(0,0,0,200);
//						Debug.fillRect(0,0,2,2);
//						
//						Debug.write("debug/frags/frag" + id + ".png");
					}
				}
			}
		}
		Debug.write("debug/frag.png");
	}

	public final int w, h; // real size
	public Node[][] grid; // x2 + 1 size
//	public Edge[][] edges; // links of nodes, must be directed to +x, +y
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
		int scale = 10;
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
					int rgb1, rgb2;
					if(n1.diagonal(n2)) {
						if(n1.x > n2.x) { // Sorting by x
							n2 = node(vecs[i].x1, vecs[i].y1);
							n1 = node(vecs[i].x2, vecs[i].y2);
						}
						if(n1.x == 0) {
							
						}
						rgb1 = rgb(0,0);
						rgb2 = rgb(0,0);
						if(rgb1 == rgb2) {
							Debug.color(Color.magenta);
							System.err.println("Rgb sides is same: " + n1 + " to " + n2 
									+ " (" + Colors.toDebugHex(rgb1) + " | " + Colors.toHex(rgb2) + ") delta: ");
						}
					} else {
						int angle = Geometry.angle(vecs[i].x1, vecs[i].y1, vecs[i].x2, vecs[i].y2);
						Vec2 center = new Vec2(vecs[i].x1 + vecs[i].x2, vecs[i].y1 + vecs[i].y2); // center of Vec4*2
						Vec2 side1 = center.copy().add(Geometry.delta(angle+2)); // 2 is 90 degree
						Vec2 side2 = center.copy().add(Geometry.delta(angle-2));
						int r = 0;
						rgb1 = rgb((side1.x+r)/4, (side1.y+r)/4);
						rgb2 = rgb((side2.y+r)/4, (side2.y+r)/4);
						if(rgb1 == rgb2) {
							Debug.color(Color.magenta);
							Debug.line((side1.x+r/2f)/2f, (side1.y+r/2f)/2f, (side2.x+r/2f)/2f, (side2.y+r/2f)/2f);
							System.err.println("Rgb sides is same: " + n1 + " to " + n2 
									+ " (" + Colors.toDebugHex(rgb1) + " | " + Colors.toHex(rgb2) + ") delta: "
									+ Geometry.delta(angle+2) + " | " + Geometry.delta(angle-2)
									+ " [" + (side1.x+r)/4f + "][" + (side1.y+r)/4f + "] | [" + (side2.x+r)/4f + "][" + (side2.y+r)/4f + "]\t"
									+ side1.x/4f + "\t" + side1.y/4f + "\t| " + side2.x/4f + "\t" + side2.y/4f);
						}
					}
					
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
		
		// FIXME: uncomment
//		for (int x = left; x < right; x++) {
//			node(x, top).link(node(x+1, top));
//			node(x, bottom).link(node(x+1, bottom));
//			removeNode(x, 0);
//			removeNode(x, h*2);
//			removeNode(x, h*2-1);
//			removeNode(x, h*2-2);
//		}
//		for (int y = top; y < bottom; y++) {
//			node(left, y).link(node(left, y+1));
//			node(right, y).link(node(right, y+1));
//			removeNode(0, y);
//			removeNode(w*2, 0);
//			removeNode(w*2, y);
//			removeNode(w*2-1, y);
//			removeNode(w*2-2, y);
//		}
//		removeNode(right+2,bottom+2);
//		removeNode(right+1,bottom+2);
//		removeNode(right+2,bottom+1);
//
//		node(left, top+1).unlink(node(left+1, top));
//		node(right,top+1).unlink(node(right-1, top));
//
//		node(left, bottom-1).unlink(node(left+1, bottom));
//		node(right,bottom-1).unlink(node(right-1, bottom));

		for (int y = 0; y < gridHeight(); y++) {
			for (int x = 0; x < gridWidth(); x++) {
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

	private int rgb(int x, int y) {
		if(x >= rgbs.length) return 0; // TODO: return alpha
		if(y >= rgbs[x].length) return 0; // TODO: return alpha
		return rgbs[x][y];
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
	 * @param points - array with size 4
	 * Points:{@code
	 * [0][1]
	 * [2][3]}
	 * 
	 * @param vs - array of lines (x1,y1, x2,y2)
	 * @param colorIds - array of ids (same index of points) of colors for each line (inner,outer), array is equals to vs size
	 * @param flipX - if it case must me flipped by X-axis
	 * @param flipY - if it case must me flipped by Y-axis
	 * 
	 * @author Agzam4
	 */
	
	public static void create(int[] points, Vec4[] vs, Vec2[] colorIds, boolean flipX, boolean flipY) {
		if(flipX) {
			points[0] += points[1];
			points[1] = points[0] - points[1];
			points[0] -= points[1];

			points[2] += points[3];
			points[3] = points[2] - points[3];
			points[2] -= points[3];
			for (Vec4 v : vs) v.flipX(2).reverse();
			System.out.println("Ids before: " + Arrays.toString(colorIds));
			for (Vec2 cid : colorIds) {
				// x and y used as arrray (it not axis)
				if(cid.x == 0) cid.x = 1; else
				if(cid.x == 1) cid.x = 0; else 
				if(cid.x == 2) cid.x = 3; else 
				if(cid.x == 3) cid.x = 2; 
				if(cid.y == 0) cid.y = 1; else
				if(cid.y == 1) cid.y = 0; else 
				if(cid.y == 2) cid.y = 3; else 
				if(cid.y == 3) cid.y = 2; 
			}
			System.out.println("Ids After: " + Arrays.toString(colorIds));
		}
		if(flipY) {
			points[0] += points[2];
			points[2] = points[0] - points[2];
			points[0] -= points[2];

			points[1] += points[3];
			points[3] = points[1] - points[3];
			points[1] -= points[3];
			for (Vec4 v : vs) v.flipY(2).reverse();
			for (Vec2 cid : colorIds) {
				// x and y used as arrray (it not axis)
				if(cid.x == 0) cid.x = 2; else
				if(cid.x == 2) cid.x = 0; else 
				if(cid.x == 1) cid.x = 3; else 
				if(cid.x == 3) cid.x = 1; 
				if(cid.y == 0) cid.y = 2; else
				if(cid.y == 2) cid.y = 0; else 
				if(cid.y == 1) cid.y = 3; else 
				if(cid.y == 3) cid.y = 1; 
			}
		}
		setAll(points, vs, colorIds);
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
	private static void setAll(int[] ps, Vec4[] value, Vec2[] colors) {
		String mask = createMask(ps);
		for (int i0 = 0; i0 < 4; i0++) for (int i1 = 0; i1 < 4; i1++) for (int i2 = 0; i2 < 4; i2++) for (int i3 = 0; i3 < 4; i3++) {
			int _ps[] = {i0,i1,i2,i3};
			String _mask = createMask(_ps);
			if(mask.equals(_mask)) {
				int id = toId(i0, i1, i2, i3);
				vecs[id] = value;
				vecsColors[id] = new int[colors.length][2];
				for (int v = 0; v < colors.length; v++) {
					vecsColors[id][v][0] = colors[v].x;
					vecsColors[id][v][1] = colors[v].y;
				}
			}
		}
	}
	
	private static int toId(int... vs) {
		return vs[0] + vs[1]*4 + vs[2]*16 + vs[3]*64;
	}
	
	
	BufferedImage debug;
	
	public boolean hasNode(int x, int y) {
		return grid[x][y] != null;
	}

}
