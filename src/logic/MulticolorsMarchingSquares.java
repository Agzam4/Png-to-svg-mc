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

import javax.imageio.ImageIO;

import logic.MarchingSquares.Vec2;
import logic.MarchingSquares.VecPathArea;
import logic.cases.Case;
import main.Main;
import svg.SvgElement;

public class MulticolorsMarchingSquares {
	
	static final Vec4[][] vecs;
	static ArrayList<Case> cases = new ArrayList<Case>();

	boolean tCase = false;
	boolean yCase = false;
	boolean vCase = false;
	boolean lCase = false;
	boolean cornerCase = false;
	boolean givenamecase2 = false;
	boolean givenamecase3 = false;
	private boolean warnings = false;
	private boolean debugImage = true;
	
	
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
		BufferedImage debug = debugImage ? new BufferedImage(w*2*scale, h*2*scale, BufferedImage.TYPE_INT_RGB) : null;
		Graphics2D g = debugImage ? (Graphics2D) debug.getGraphics() : null;
		if(debugImage) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		grid = new Node[w*2+1][h*2+1];
		colors = new int[w*2+1][h*2+1];
		// Applying main cases
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				boolean outY = y+1 >= h;
				boolean outX = x+1 >= w;


				int[] mask = createMaskArray(new int[] {
                        rgbs[x][y],
						outX ? 0 : rgbs[x+1][y],
						outY ? 0 : rgbs[x][y+1],
						outX || outY ? 0 : rgbs[x+1][y+1]
				});

				int[] hasColor = new int[4];
				for (int i = 0; i < mask.length; i++) {
					hasColor[mask[i]] = 1;
				}
				int colorsCount = IntStream.of(hasColor).sum()-1;

				int key = toId(mask);

				Vec4[] vecs = new Vec4[MulticolorsMarchingSquares.vecs[key].length];

				colors[x*2][y*2] = colorsCount;
				colors[x*2+1][y*2+1] = colorsCount;
				
				if(debugImage) g.setColor(Color.green.darker().darker());
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

					if(n.diagonal(n0) || n.diagonal(n1) || n.diagonal(n2)) continue;

					int hc = (h0?1:0) + (h1?1:0) + (h2?1:0);

					Node r;
					if(hc == 1) { // "├" case
						r = h0 ? n0 : (h1 ? n1 : n2);
					} else { // "T" case
						r = h0 ? (h1 ? n2 : n1) : n0;
					}

					if(r.links() != 2) continue;
					Node rr = r.next(n);

					int rdx = r.x - rr.x;
					int rdy = r.y - rr.y;
					if(!r.diagonal(rr)) continue;
					n.unlink(r);
					r.link(node(r.x + rdx, r.y + rdy));
				}
			}

		
		for (int i = 0; i < cases.size(); i++) {
			cases.get(i).g = g;
			cases.get(i).apply(this);	
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

				if(n0.links() != 2 || n1.links() != 2 || n2.links() != 2) continue;

				if(n.diagonal(n0) || n.diagonal(n1) || n.diagonal(n2)) continue;

				// Radicals
				Node nr0 = n0.next(n);
				Node nr1 = n1.next(n);
				Node nr2 = n2.next(n);

				if((n0.diagonal(nr0)?1:0) + (n1.diagonal(nr1)?1:0) + (n2.diagonal(nr2)?1:0) != 2) continue;
				
				if(debugImage) {
					g.setColor(new Color(0,0,255,50));
					g.fillOval(n.x*scale - scale/2, n.y*scale - scale/2, scale, scale);
					g.setColor(new Color(0,180,255,150));

					g.fillOval(n0.x*scale - scale/2, n0.y*scale - scale/2, scale, scale);
					g.fillOval(n1.x*scale - scale/2, n1.y*scale - scale/2, scale, scale);
					g.fillOval(n2.x*scale - scale/2, n2.y*scale - scale/2, scale, scale);

					g.setColor(new Color(0,255,255,150));
				}

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
				
				Node s1 = n0.diagonal(nr0) ? (n1.diagonal(nr1) ? n2 : n1) : n0;
				//we already tested the links earlier, since s1 ∈ {n0, n1, n2}
				if(debugImage) g.setColor(new Color(255,0,255,150));
				if(debugImage) g.fillOval(s1.x*scale - scale/2, s1.y*scale - scale/2, scale, scale);

				int isWrongSide = 0;
				for(Node nx : n.links){
					if(nx == s1) continue; //for n0,n1,n2 that arent s1
					if(!s1.diagonal(nx.next(n))) isWrongSide++; //skip if s1 is diagonal to rx
				}
				if(isWrongSide == 2) continue;

				Node s2 = s1.next(n);
				if(s2.links() != 2) continue;
				if(debugImage) g.setColor(new Color(255,0,255,50));
				if(debugImage) g.fillOval(s2.x*scale - scale/2, s2.y*scale - scale/2, scale, scale);

				Node s3 = s2.next(s1);
				if(s3.links() != 2) continue;
				if(debugImage) g.setColor(new Color(255,0,127,50));
				if(debugImage) g.fillOval(s3.x*scale - scale/2, s3.y*scale - scale/2, scale, scale);

				Node sr = s3.next(s2);
				if(debugImage) g.fillOval(sr.x*scale - scale/2, sr.y*scale - scale/2, scale, scale);

				Node ns = node(s3.x*2 - sr.x, s3.y*2 - sr.y);

				//it cant link to itself so why bother
				s1.link(n0);
				s1.link(n1);
				s1.link(n2);
				removeNode(n);
				
				System.out.println("y applyed");

				if(s2 != ns) {
					ns.link(s3);
					ns.link(s1);
					removeNode(s2);
				}
			}
		}
		
		if(vCase) 
		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
				Node n = grid[x][y];
				if(n == null) continue;
				if(n.links() != 2) continue;

				Node n1 = n.get(0);
				Node n2 = n.get(1);

				if(n1.links() != 2) continue;
				if(n2.links() != 2) continue;
				
				if(n1.diagonal(n) || n2.diagonal(n)) continue;

				Node r1 = n1.next(n);
				Node r2 = n2.next(n);

				if(!n1.diagonal(r1) || !n2.diagonal(r2)) continue;
				
				if(debugImage) g.setColor(new Color(255,0,255,150));
				if(debugImage) g.fillOval(n.x*scale - scale/2, n.y*scale - scale/2, scale, scale);

				int dx1 = r1.x-n1.x;
				int dy1 = r1.y-n1.y;
				int dx2 = r2.x-n2.x;
				int dy2 = r2.y-n2.y;

				int x1 = n1.x - dx1;
				int y1 = n1.y - dy1;
				int x2 = n2.x - dx2;
				int y2 = n2.y - dy2;

				if(x1 != x2) continue;
				if(y1 != y2) continue;
				
				Node ns = node(x1, y1);

				ns.link(n1);
				ns.link(n2);
				removeNode(n);

				if(debugImage) g.setColor(new Color(116,0,255,150));
				if(debugImage) g.fillOval(x1*scale - scale/2, y1*scale - scale/2, scale, scale);
				/**
				 * 		       NS
				 * 		      /  \
				 * 		    N1-N_-N2
				 * 		   /        \
				 * 		  R1         R2
				 */
			}
		}

		if(lCase)
		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
				Node n = grid[x][y];
				if(n == null) continue;
				/**
				 * N1
				 * | \
				 * |  N
				 * | / \
				 * N3   N2
				 */
				if(n.links() != 2) continue;

				Node n1 = n.get(0);
				Node n2 = n.get(1);

				if(!n1.diagonal(n) || !n2.diagonal(n)) continue;

				if(n1.links() != 3 && n2.links() != 3) continue;
				if(n1.links() != 3) {
					n1 = n.get(1);
					n2 = n.get(0);
				}
				int dx = n.x - n1.x;
				int dy = n.y - n1.y;
				if(grid[n1.x + dx][n1.y] != null && grid[n1.x + dx*2][n1.y] != null) {
					Node n3 = grid[n1.x + dx*2][n1.y];
					ArrayList<Node> links = (ArrayList<Node>) n3.links.clone();
					for(Node l : links) {
						if(!l.diagonal(n) || !l.diagonal(n3)) continue;
						grid[n1.x + dx*2][n1.y].link(n);
						removeNode(n1.x + dx, n1.y);
						if(debugImage) break;
					}
				}
				if(grid[n1.x][n1.y + dy] != null && grid[n1.x][n1.y + dy*2] != null) {
					Node n3 = grid[n1.x][n1.y + dy*2];
					ArrayList<Node> links = (ArrayList<Node>) n3.links.clone();
					for(Node l : links) {
						if(!l.diagonal(n) || !l.diagonal(n3)) continue;
						grid[n1.x][n1.y + dy*2].link(n);
						removeNode(n1.x, n1.y + dy);
						if(debugImage) break;
					}
				}


			}
		}

		// Sharpen "\_/" cases
		if(cornerCase)
		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
				Node n = grid[x][y];
				if(n == null) continue;
				if(n.links() != 2) continue;

				Node n1 = n.get(0);
				Node n2 = n.get(1);

				if(n1.links() != 2) continue;
				if(n2.links() != 2) continue;

				if(n.diagonal(n1) == n.diagonal(n2)) continue; //stay if one is diagonal and not the other
				if(n.diagonal(n1)){
					Node tmp = n1;
					n1 = n2;
					n2 = tmp;
				}
				Node r2 = n2.next(n);
				if(n2.diagonal(r2)) continue;
				if(!n2.diagonal(n)) continue;

				int dx = r2.x-n2.x;
				int dy = r2.y-n2.y;
				if(2*n.x-n1.x != n2.x-dx || 2*n.y-n1.y != n2.y-dy){
					System.err.println("Corner-case error: " + n.x + " " + n.y);
					continue;
				}


				Node ns = node(n2.x - dx, n2.y - dy);

				n.unlink(n2);
				ns.link(n);
				ns.link(n2);

				/**
				 * 		  R2-N2-NS
				 * 		       \|
				 * 		        N
				 * 		        |
				 * 		        N1
				 */
			}
		}

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
		if(givenamecase3)
		for (int y = 0; y < h*2+1; y++) {
			for (int x = 0; x < w*2+1; x++) {
				Node n = grid[x][y];
				if(n == null) continue;
				if(n.links() != 4) continue;

				Node n0 = n.get(0);
				Node n1 = n.get(1);
				Node n2 = n.get(2);
				Node n3 = n.get(3);

				if(n.diagonal(n0)) continue;
				if(n.diagonal(n1)) continue;
				if(n.diagonal(n2)) continue;
				if(n.diagonal(n3)) continue;

				if(n0.links() != 2) continue;
				if(n1.links() != 2) continue;
				if(n2.links() != 2) continue;
				if(n3.links() != 2) continue;

				//reorder the nodes so that n0/n1 are opposite, and n2/n3 are opposite
				if(n0.diagonal(n1)){
					var tmp = n1;
					if(n1.diagonal(n3)){ //n3 is opposite of n0
						n1 = n3;
						n3 = tmp;
					}else{ //n3 is same side as n1 -> n2 is opposite of n0
						n1 = n2;
						n2 = tmp;
					}
				}

				Node r0 = n0.next(n);
				Node r1 = n1.next(n);
				Node r2 = n2.next(n);
				Node r3 = n3.next(n);

				if(((n0.diagonal(r0)?1:0) + (n1.diagonal(r1)?1:0) + (n2.diagonal(r2)?1:0) + (n3.diagonal(r3)?1:0)) != 2) continue;

				if(!r0.diagonal(n1)) { //n0 n1 vertical line, n2 n3 arms
					if(r2.diagonal(r3)) continue; //skip if r2 r3 arent aligned, meaning the two arms are opposite side

					n.unlink(n2);
					n.unlink(n3);
					if(!n0.diagonal(r2)){ //n0 on the side of the arms, so n1 is opposite
						n1.link(n2);
						n1.link(n3);
					}else{
						n0.link(n2);
						n0.link(n3);
					}

				}else{ //n2 n3 vertical line, n0 n1 arms
					if(r0.diagonal(r1)) continue; //skip if r0 r1 arent aligned, meaning the two arms are opposite side

					n.unlink(n0);
					n.unlink(n1);
					if(!n2.diagonal(r1)){ //n2 on the side of the arms, so n3 is opposite
						n3.link(n0);
						n3.link(n1);
					}else{
						n2.link(n0);
						n2.link(n1);
					}
				}

				/**
				 *         R0
				 *         |
				 *         N0
				 *         |
				 *      N2-N-N3
				 *     /   |   \
				 *   R2    N1   R3
				 *         |
				 *         R1
				 * or
				 *         R2
				 *         |
				 *         N2
				 *         |
				 *      N0-N-N1
				 *     /   |   \
				 *   R0    N3   R1
				 *         |
				 *         R3
				 */
			}
		}

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
				if(debugImage) g.setColor(new Color(255,255,255,100));
				if(debugImage) g.fillRect(x*scale, y*scale, scale/5, scale/5);
				Node n = grid[x][y];
				if(n == null) continue;
				if(debugImage) g.setColor(new Color(255,0,0,100));
				for (var l : n.links) {
					if(Math.abs(n.x-l.x) > 1) {
						if(debugImage) g.setColor(new Color(0,255,0));
						System.err.println("Link x size >2: " + n.x + " " + n.y);
					}
					if(Math.abs(n.y-l.y) > 1) {
						if(debugImage) g.setColor(new Color(0,0,255));
						System.err.println("Link y size >2");
					}
					if(debugImage) g.drawLine(n.x*scale, n.y*scale, l.x*scale, l.y*scale);
				}
			}
		}
		if(debugImage) {
			g.dispose();
			try {
				ImageIO.write(debug, "png", new File("debug/tmp-" + save + ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
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
					element.attribute("fill", Colors.toHex( Colors.RGBAtoRGB(rgb) ));
					if(Colors.alpha(rgb) != 255){
						element.attribute("fill-opacity", Colors.alpha(rgb));
					}
//					if(rgb != 0xFF) {
//						System.out.println(rgb);
//					} else {
//					}

					VecPathArea tobeadded = new VecPathArea(element, minX, maxX, minY, maxY, area);

					if(paths.isEmpty() || !paths.contains(tobeadded)) {
						paths.add(tobeadded);
					}
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

	public boolean hasNode(int x, int y) {
		return grid[x][y] != null;
	}


}
