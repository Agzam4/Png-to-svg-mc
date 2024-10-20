package logic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import logic.Node.Link;
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
	@Deprecated public boolean tCase = false;
	@Deprecated public boolean yCase = false;
	@Deprecated public boolean vCase = false;
	@Deprecated public boolean lCase = false;
	@Deprecated public boolean cornerCase = false;
	@Deprecated public boolean givenamecase2 = false;
	@Deprecated public boolean generalYCase = false;
	@Deprecated public boolean pigeonCase = false; // Supersized by generalYcase 
	
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
//									Vec4 line = v.copy();
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
//								System.out.println(Arrays.toString(vecsColors[id][vi]) + "\t" + Arrays.toString(lColors) + "\t" + i1 + " " + i2 + " " + i3 + " " + i4 + "\t#" + id);
							}
						}
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
				int[] srcRgb = new int[] {
                        rgbs[x][y],
						outX ? Colors.transparent : rgbs[x+1][y],
						outY ? Colors.transparent : rgbs[x][y+1],
						outX || outY ? Colors.transparent : rgbs[x+1][y+1]
				};
				int[] mask = createMaskArray(new int[] {
                        rgbs[x][y],
						outX ? Colors.transparent : rgbs[x+1][y],
						outY ? Colors.transparent : rgbs[x][y+1],
						outX || outY ? Colors.transparent : rgbs[x+1][y+1]
				});
				int[] hasColor = new int[4];
				for (int i = 0; i < mask.length; i++) {
					hasColor[mask[i]] = 1;
				}
				int colorsCount = IntStream.of(hasColor).sum()-1;
				int key = toId(mask);
				Debug.color(Colors.toDebugColor(rgbs[x][y]));
				Debug.fillRect(x*2, y*2, 1, 1);
				Debug.color(Colors.toDebugColor(outX ? -1 : rgbs[x+1][y]));
				Debug.fillRect(x*2+1, y*2, 1, 1);
				Debug.color(Colors.toDebugColor(outY ? -1 : rgbs[x][y+1]));
				Debug.fillRect(x*2, y*2+1, 1, 1);
				Debug.color(Colors.toDebugColor(outX || outY ? -1 : rgbs[x+1][y+1]));
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
					int rgb1 = srcRgb[vecsColors[key][i][0]], rgb2 = srcRgb[vecsColors[key][i][1]];
					n1.link(n2, rgb1, rgb2);
				}
			}
		}
		// Post cases
		
		// Sharpen T-cases
		if(tCase) TCase.apply(this);
		/** @Deprecated **/
		for (int i = 0; i < cases.size(); i++) {
			cases.get(i).apply(this);	
		}
		// Sharpen Y-cases
		if(yCase) YCase.apply(this);
		if(vCase) VCase.apply(this);
		if(lCase) Lcase.apply(this);
		// Sharpen "\_/" cases
		if(cornerCase) CornerCase.apply(this);
		// Sharpen "|\"-cases
		/** @Deprecated
		if(givenamecase2)
		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
				Node n = grid[x][y];

				if(n == null) continue;
				if(!(n.links() == 2 || n.links() == 3)) continue;

				Node n1 = n.get(0).target;
				Node n2 = n.get(1).target;

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
				*

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

				/*
				 * 		   NS             NS
				 * 		   |  \           |  \
				 * 		  NS2  N         NS2  N
				 * 		   | /  \         | /  \
				 * 		  N1    N2       N2    N1
				 * 		   |      \       |     \
				 * 		  R1      R2     R2     R1
				 *
			}
		}
		*/
		
		// Sharpen ᛉ-cases
		if(pigeonCase) PigeonCase.apply(this);
        if(generalYCase) GeneralYCase.apply(this);

		// Borders

		int left = 1;
		int top = 1;
		int right = w*2-3;
		int bottom = h*2-3;
		
		// FIXME: colors
		for (int x = left; x <= w*2; x++) {
			if(left <= x && x < bottom) {
				node(x, top).link(node(x+1, top), Colors.transparent, rgbs[Math.min(x/2+1, h-2)][top/2+1]);
				node(x, bottom).link(node(x+1, bottom), rgbs[Math.min(x/2+1, h-2)][bottom/2], Colors.transparent);
			}
			removeNode(x, 0);
			removeNode(x, h*2);
			removeNode(x, h*2-1);
			removeNode(x, h*2-2);
		}
		for (int y = top; y <= h*2; y++) {
			if(top <= y && y < bottom) {
				node(left, y).link(node(left, y+1), rgbs[left/2+1][Math.min(y/2+1, h-2)], Colors.transparent);
				node(right, y).link(node(right, y+1), Colors.transparent, rgbs[right/2][Math.min(y/2+1, h-2)]);
			}
			removeNode(0, y);
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

		for (int y = 0; y < gridHeight(); y++) {
			for (int x = 0; x < gridWidth(); x++) {
//				g.setColor(new Color(Color.HSBtoRGB(colors[x][y]/5f, 1f, 1f)));
				Debug.color(0,0,0,220);
				Debug.fillRect(x, y, 1f, 1f);
				
				Debug.color(255,255,255,10);
				Debug.fillRect(x, y, 1f/5f, 1f/5f);
				Node n = grid[x][y];
				if(n == null) continue;
				Debug.color(255,0,0,255);
				n.eachLink(l -> {
					if(Math.abs(n.x-l.target.x) > 1) {
						Debug.color(0,255,0);
						System.err.println("Link x size >2: " + n.x + " " + n.y);
					}
					if(Math.abs(n.y-l.target.y) > 1) {
						Debug.color(0,0,255);
						System.err.println("Link y size >2");
					}
					Debug.color(Colors.toDebugColor(l.rgbr));
					float x1 = n.x;
					float y1 = n.y;
					float x2 = l.target.x;
					float y2 = l.target.y;
					double angle = Math.atan2(y1-y2, x1-x2);
					angle += Math.PI/2f;
					float delta = 1/5f;
					x1 += Math.cos(angle)*delta;
					x2 += Math.cos(angle)*delta;
					y1 += Math.sin(angle)*delta;
					y2 += Math.sin(angle)*delta;
					Debug.line(x1,y1,x2,y2);
				});
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
				if(grid[x][y].links() != 2) continue;
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
					for (Link link : c.getLinks()) {
						if(link.target == c) continue;
						if(link.target == last) continue;
						last = c;
						c.unlink(link.target);
						c = link.target;
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

	public boolean hasNodePosition(int x, int y) {
		if(x < 0 || y < 0) return false;
		if(x >= gridWidth() || y >= gridHeight()) return false;
		return true;
	}

}
