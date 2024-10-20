package logic.cases;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

import javax.imageio.ImageIO;

import logic.Geometry;
import logic.MulticolorsMarchingSquares;
import logic.Node;
import logic.Node.Link;
import logic.Vec2;
import main.Debug;

public class Case {
	
	/**
	 * TODO: add more transforms
	 * TODO: disable transform that duplicates transforming for current case
	 */
	
	static enum MaskTypes {
		
		MASK(true, true), 
		CREATE(false, true), 
		REMOVE(true, false);

		private boolean before; // if node/link must be on source grid
		private boolean after; // if node/link must be on output grid

		MaskTypes(boolean before, boolean after) {
			this.before = before;
			this.after = after;
		}
	
		private Color color() {
			return ordinal() == 0 ? Color.lightGray : (ordinal() == 1 ? Color.blue : Color.red);
		}
	}

//	static Vec2[] rotations = {
//			new Vec2(1, 0),
//			new Vec2(1, 1),
//			new Vec2(0, 1),
//			new Vec2(-1, 1),
//			new Vec2(-1, 0),
//			new Vec2(-1, -1),
//			new Vec2(0, -1),
//			new Vec2(1, -1)
//	};

	private static int deltaId(int dx, int dy) {
		return dx+1 + (dy+1)*3;
//		for (int i = 0; i < rotations.length; i++) {
//			if(rotations[i].x == dx && rotations[i].y == dy) {
//				return i;
//			}
//		}
//		return -1;
	}
	
	static enum Transforms {

		FlipX((x,y) -> -x, (x,y) -> y),
		FlipY((x,y) -> x, (x,y) -> -y),
		FlipXY((x,y) -> -x, (x,y) -> -y),
		Rotate90((x,y) -> y, (x,y) -> x),
		Rotate270((x, y) -> -y, (x, y) -> x),
		Rotate180((x, y) -> -y, (x, y) -> -x),
		FlipYRotate90((x, y) -> y, (x, y) -> -x),
		None((x,y) -> x, (x,y) -> y);

		private Transform tx, ty;

		Transforms(Transform tx, Transform ty) {
			this.tx = tx;
			this.ty = ty;
		}

		int x(MaskLink l) {
			return tx.get(l.dx, l.dy);
		}
		
		int y(MaskLink l) {
			return ty.get(l.dx, l.dy);
		}

		int x(int x, int y) {
			return tx.get(x, y);
		}

		int y(int x, int y) {
			return ty.get(x, y);
		}
		
	}
	
	private interface Transform {
		int get(int x, int y);
	}

	private NodeMask root;
	private String name;
	public Graphics2D g;
	int gscl = 5;
	
	private ArrayList<NodeMask> nodes = new ArrayList<>();
	private ArrayList<MaskLink> mlinks = new ArrayList<>();

	public Case(File file) throws IOException, NumberFormatException {
		this.name = file.getName();
		List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));
		HashMap<Long, NodeMask> masksMap = new HashMap<>();
		for (int i = 0; i < lines.size(); i++) {
			String[] data = lines.get(i).split(" ");
			int type = Integer.parseInt(data[0]);
			int x = Integer.parseInt(data[1]);
			int y = Integer.parseInt(data[2]);
			int dx = Integer.parseInt(data[3]);
			int dy = Integer.parseInt(data[4]);
			for (int j = 0; j < 2; j++) {
				long packed = Vec2.pack(x, y);
				NodeMask mask = masksMap.get(packed);
				if(mask == null) {
					mask = new NodeMask(x, y);
					masksMap.put(packed, mask);
				}
				mask.rawLink(dx, dy, MaskTypes.values()[type]);
				x += dx;
				y += dy;
				dx = -dx;
				dy = -dy;
			}
		}
		ArrayList<NodeMask> masks = new ArrayList<Case.NodeMask>(masksMap.values());

		for (var m : masks) {
			for (var raw : m.links) {
				long packed = Vec2.pack(m.x + raw.dx, m.y + raw.dy);
				NodeMask link = masksMap.get(packed);
				if(link == null) {
					throw new IOException("Links read error at '" + file.getName() + "': can't find link at (" 
														+ (m.x + raw.dx) + "," + (m.y + raw.dy) + ") #" + packed + " with source at (" + m.x + "," + m.y + ")");
				}
				raw.node = link;
//				link.addLink(m, raw.type);
//				m.addLink(link);
			}
		}
		
		
		masks.sort((m1,m2) -> m2.beforeLinks() - m1.beforeLinks());
		
		this.root = masks.get(0);
//		int dx = masks.get(0).x, dy = masks.get(0).y;
		root.depth = 0;
		root.updateDepth();
		
		BufferedImage debug = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) debug.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int scale = 30;
//		System.out.println("Nodses: " + NodeMask.nodes);
		
		for (var m : masks) {
//			m.x -= dx;
//			m.y -= dy;
			m.eachChildren(new BiConsumer<NodeMask, Integer>() {
				public void accept(NodeMask t, Integer d) {
					for (var l : m.links) {
						g.setColor(l.type.color());
						g.drawLine(m.x*scale + scale/2, m.y*scale + scale/2, l.node.x*scale + scale/2, l.node.y*scale + scale/2);
					}
					int gr = 255 - d*25;
					if(gr < 0 || gr > 255) System.err.println(gr + " (" + t.x + " " + t.y + ")");
					g.setColor(new Color(Color.HSBtoRGB(d*10/360f, 1f, 1f)));
					g.fillOval(t.x*scale, t.y*scale, scale, scale);
				}
			});
		}
		
		ImageIO.write(debug, "png", new File("debug/cases/" + file.getName() + ".png"));
		
		System.out.println("Case '" + name + "' loaded: root is " + root);
//		int maxKey = 0;
//		int maxCount = 0;
		int dx = root.x;
		int dy = root.y;
		
		for (NodeMask m : nodes) {
			m.x -= dx;
			m.y -= dy;
//			System.out.println(m);
		}
	}
	
	private class NodeMask {
		
		private int x, y;
		private int depth = -1;
		private ArrayList<MaskLink> links = new ArrayList<>();
//		private ArrayList<NodeMask> links = new ArrayList<>();
//		private ArrayList<MaskTypes> types = new ArrayList<>();
		boolean before = false;
		boolean after = false;
		
//		static int nodes = 0;
		
		private NodeMask(int x, int y) {
			this.x = x;
			this.y = y;
			nodes.add(this);
//			nodes++;
		}
		
//		public MaskTypes type() {
//			for (MaskTypes t : MaskTypes.values()) {
//				if(t.before == before && t.after == after) return t;
//			}
//			return null;
//		}

		public void rawLink(int dx, int dy, MaskTypes type) {
			links.add(new MaskLink(dx, dy, type));
//			types.add(type);
			before = before || type.before;
			after = after || type.after;
		}

//		public void addLink(NodeMask link) {
//			if(link.type != MaskTypes.CREATE.ordinal()) srcLinks++;
//			links.add(link);
//		}
		
		private void updateDepth() {
			for (var l : links) {
				if(l.node.depth == -1) l.node.depth = depth+1;
			}
			for (var l : links) {
				if(l.node.depth > depth) {
					l.node.updateDepth();
				}
			}
		}

		private void eachChildren(BiConsumer<NodeMask, Integer> cons) {
			cons.accept(this, depth);
			for (var l : links) {
				if(l.node.depth > depth) l.node.eachChildren(cons);
			}
		}
		
		
		@Override
		public String toString() {
			return "(" + before + ", " + x + ", " + y + ")";
		}

		/**
		 * Validation of mask
		 * @param m
		 * @param x - x-position of current mask node
		 * @param y - y-position of current mask node
		 * @param tf - Transform of case
		 * @param d - depth
		 * @return {@code true} if link valid
		 */
		public boolean test(MulticolorsMarchingSquares m, int x, int y, Transforms tf, int d) { // FIXME: checking not all
			if(!testLinks(m, x, y, tf, d)) return false;
			for (var l : links) {
				Debug.color(Color.magenta);
				if(!l.node.before) continue;
				if(l.node.depth > depth) {
					if(!l.node.test(m, x + tf.x(l), y + tf.y(l), tf, d+1)) {
//						g.setColor(Color.magenta);
//						g.drawLine(x*gscl, y*gscl, (x+tf.x(l))*gscl, (y+tf.y(l))*gscl);
						return false;
					} else {
//						g.setColor(Color.green);
//						g.drawLine(x*gscl, y*gscl, (x+tf.x(l))*gscl, (y+tf.y(l))*gscl);
					}
				}
			}
			return true;
		}

		public boolean testLinks(MulticolorsMarchingSquares m, int x, int y, Transforms tf, int d) {
			if(m.hasNode(x, y) != before) {
//				System.out.println("Wrong node");
//				g.fillRect(x*gscl - gscl/2 + gx, y*gscl - gscl/2 + gy, 1, 1);
				return false;
			}
			Node node = m.grid[x][y];
			boolean[] src = new boolean[9];
			boolean[] mask = new boolean[9];
			int beforeLinks = 0;
			MaskLink ml = null;
			for (var l : links) {
				if(!l.type.before) continue;
				beforeLinks++;
				ml = l;
//				if(!m.hasNodePosition(tf.x(l), tf.y(l))) continue;
				mask[deltaId(tf.x(l), tf.y(l))] = true;
			}

			node.eachLink(l -> {
				int dx = l.target.x - node.x;
				int dy = l.target.y - node.y;
				src[deltaId(dx, dy)] = true;
			});
			
//			if(d > 0) {
//				int id = 0;
//				g.setColor(new Color(0,255,255,50));
//				for (int gy = 0; gy < 3; gy++) {
//					for (int gx = 0; gx < 3; gx++) {
//						if(mask[id]) g.setColor(new Color(0,255,0,50));
//						else g.setColor(new Color(255,0,0,50));
//						g.fillRect(x*gscl - gscl/2 + gx + 3, y*gscl - gscl/2 + gy + 3, 1, 1);
//						id++;
//					}
//				}
//				id = 0;
//				for (int gy = 0; gy < 3; gy++) {
//					for (int gx = 0; gx < 3; gx++) {
//						if(src[id]) g.setColor(new Color(0,0,255,50));
//						else g.setColor(new Color(255,0,0,50));
//						g.fillRect(x*gscl - gscl/2 + gx, y*gscl - gscl/2 + gy, 1, 1);
//						id++;
//					}
//				}
//			}
			
			if(beforeLinks == 1) {
				for (var l : node.getLinks()) {
					int dx = l.target.x - node.x;
					int dy = l.target.y - node.y;
					if(dx == tf.x(ml) && dy == tf.y(ml)) return true;
				}
				return false;
			}
			for (int i = 0; i < mask.length; i++) {
				if(src[i] != mask[i]) {
//					System.out.println("Wrong mask");
					return false;
				}
			}

//			if(d == 0) {
//				int id = 0;
//				g.setColor(new Color(0,255,255,25));
//				for (int gy = 0; gy < 3; gy++) {
//					for (int gx = 0; gx < 3; gx++) {
//						if(mask[id]) g.setColor(new Color(0,255,0,25));
//						else g.setColor(new Color(255,0,0,25));
//						g.fillRect(x*gscl - gscl/2 + gx + 3, y*gscl - gscl/2 + gy + 3, 1, 1);
//						id++;
//					}
//				}
//				id = 0;
//				for (int gy = 0; gy < 3; gy++) {
//					for (int gx = 0; gx < 3; gx++) {
//						if(src[id]) g.setColor(new Color(0,0,255,25));
//						else g.setColor(new Color(255,0,0,25));
//						g.fillRect(x*gscl - gscl/2 + gx, y*gscl - gscl/2 + gy, 1, 1);
//						id++;
//					}
//				}
//			}
			
			return true;
		}

		public int beforeLinks() {
			int count = 0;
			for (var l : links) {
				if(l.type.before) count++;
			}
			return count;
		}
	}
	
	class MaskLink {
		
		private NodeMask node = null;
		private int dx, dy;
		private MaskTypes type;

		public MaskLink(int dx, int dy, MaskTypes type) {
			this.dx = dx;
			this.dy = dy;
			this.type = type;
			mlinks.add(this);
		}

	}
	
	public void apply(MulticolorsMarchingSquares mms) {
//		Debug.image(mms.gridWidth(), mms.gridHeight(), 10);
		for (int y = 0; y < mms.gridHeight(); y++) {
			for (int x = 0; x < mms.gridWidth(); x++) {
				if(mms.grid[x][y] == null) continue;
				for (Transforms transform : Transforms.values()) {
					try { // TODO: remove try/catch
						if(root.test(mms, x, y, transform, 0)) {
							System.out.println("Found at " + x + ";" + y + " " + transform);
							ArrayList<LinkPromise> promises = new ArrayList<Case.LinkPromise>();
							for (NodeMask n : nodes) {
								int nx = x + transform.x(n.x, n.y);
								int ny = y + transform.y(n.x, n.y);
								Node from = mms.grid[nx][ny];
								if(n.after) {
									if(from == null) from = mms.node(nx, ny);
								} else {
									if(from != null) mms.removeNode(nx, ny);
									continue;
								}
								
								for (MaskLink ml : n.links) {
									Node to = mms.grid[nx + transform.x(ml)][ny + transform.y(ml)];
									if(ml.type.after) {
										if(to == null) to = mms.node(nx + transform.x(ml), ny + transform.y(ml));
//										from.link(to);
										promises.add(new LinkPromise(from, to));
										Debug.color(Color.cyan);
										Debug.fillOval(from, .5f);
										Debug.line(from, to);
									} else if (to != null) {
										from.unlink(to);
									}
								}
							}
							while (true) {
								boolean changes = false;
								for (int i = 0; i < promises.size(); i++) {
									LinkPromise p = promises.get(i);
									int angle = 0;//p.from.angleTo(p.to);
									angle = p.from.angleTo(p.to);
									if(p.from.links() == 0 && p.to.links() == 0) continue;

									if(!p.hasRgbR) {
										Link next = p.from.findLinkBackwards(angle, 5); // make it by step?
										if(next != null) {
											p.rgbr(next.rgbl);//new Color(255,255,255,255).getRGB()); // next.rgbl
											changes = true;
										}
									}
									if(!p.hasRgbL) {
										Link next = p.from.findLinkForwards(angle, 5);
										if(next != null) {
											p.rgbl(next.rgbr);//new Color(255,255,255,255).getRGB());
											changes = true;
										}
									}
									angle = p.to.angleTo(p.from);
//									if(!p.hasRgbR) {
//										Link next = p.to.findLinkBackwards(angle, 5);
//										if(next != null) {
//											p.rgbr(new Color(255,255,255,255).getRGB());
//											changes = true;
//										}
//									}
//									if(!p.hasRgbL) {
//										Link next = p.from.findLinkBackwards(angle, 8);
//										if(next != null) {
//											p.rgbl(next.rgbl);
//											changes = true;
//										}
//									}
									if(p.hasRgbR && p.hasRgbL) {
										p.from.link(p.to, p.rgbr, p.rgbl);
										promises.remove(i);
										break;
									}
									
//									if(p.hasRgbR) {
//										p.from.link(p.to, p.rgbr, p.rgbl);
//										promises.remove(i);
//										break;
//									}
								}
								if(promises.size() <= 0) break;
//								System.out.println("Promises: " + promises);
								if(!changes) {
									System.err.println("No changes: " + promises);
									break;
								}
							}
							
							for (LinkPromise p : promises) {
								p.from.link(p.to, p.rgbr, p.rgbl);
							}
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
//		Debug.write("debug/cases/" + name + ".png");
	}
	
	class LinkPromise {
		
		final Node from, to;
		boolean hasRgbL, hasRgbR;
		int rgbl = Color.magenta.getRGB(), rgbr = Color.magenta.getRGB();
		int stepR, stepL;
		
		public LinkPromise(Node from, Node to) {
			this.from = from;
			this.to = to;
		}

		public void rgbr(int rgbr) {
			this.rgbr = rgbr;
			hasRgbR = true;
		}

		public void rgbl(int rgbl) {
			this.rgbl = rgbl;
			hasRgbL = true;
		}

		@Override
		public String toString() {
			return "(" + from + ")=>(" + to + ")";
		}
	}
}
