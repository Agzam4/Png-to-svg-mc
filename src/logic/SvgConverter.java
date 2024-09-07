package logic;

import java.util.ArrayList;
import java.util.HashMap;

import main.Debug;
import main.Main;
import svg.SvgElement;

public class SvgConverter {

	// TODO: replace atan2
	
	public static ArrayList<SvgElement> getSvgPaths(MulticolorsMarchingSquares mc) {
		ArrayList<VecPathArea> paths = new ArrayList<>();
		int pathId = 0;
		
		boolean[][][] visited = new boolean[mc.gridWidth()][mc.gridHeight()][];
		for (int y = 0; y < mc.gridHeight(); y++) {
			for (int x = 0; x < mc.gridWidth(); x++) {
				if(mc.grid[x][y] == null) continue;
				Node from = mc.grid[x][y];
				visited[x][y] = new boolean[from.links()];
				from.links.sort((l1,l2) -> Double.compare(Math.atan2(l1.y - from.y, l1.x - from.x), Math.atan2(l2.y - from.y, l2.x - from.x)));
			}
		}

		for (int y = 0; y < mc.gridHeight(); y++) {
			for (int x = 0; x < mc.gridWidth(); x++) {
				if(mc.grid[x][y] == null) continue;
				Node node = mc.grid[x][y];
				for (int i = 0; i < node.links(); i++) {
					if(visited[x][y][i]) continue;
					ArrayList<Node> path = new ArrayList<Node>();
					int index = i;
					Node n = node;
					Node next = null;
					do {
						path.add(n);
						visited[n.x][n.y][index] = true;
						next = n.links.get(index);
						index = (next.links.indexOf(n)+1)%next.links();
						n = next;
					} while (n != node);
					
					if(path.size() <= 1) continue;
					
					if(pathId > 0) {
						int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
						int maxX = 0, maxY = 0;
						ArrayList<Node> corners = new ArrayList<Node>();
						Node l = null;
						int pdx = 0, pdy = 0;
						for (var p : path) {
							minX = Math.min(minX, p.x);
							maxX = Math.max(maxX, p.x);
							minY = Math.min(minY, p.y);
							maxY = Math.max(maxY, p.y);
							if(l != null) {
								if(pdx != p.x - l.x || pdy != p.y - l.y) {
									corners.add(l);
									pdx = p.x - l.x;
									pdy = p.y - l.y;
								}
							}
							l = p;
						}
						corners.add(path.get(path.size()-1));
						
						int rgb = mc.rgbs[(n.x+1)/2][(n.y+3)/2];

						/*
						 *  Searching the most frequent color: 
						 *  under top links
						 *  over bottom links
						 *  right of left links
						 *  left of right links
						 *  of shape 
						 *  (FIXME: in theory it can be broken by cases)
						 */
						HashMap<Integer, Vec1> counter = new HashMap<Integer, Vec1>();
						int maxCount = 0;
						for (var p : path) {
							int dx = 0, dy = 0;
							if(p.y != minY && p.y != maxY) {
								if(p.x == minX) dx = 1;
								if(p.x == maxX) dx = -1;
							}
							if(p.x != minX && p.x != maxX) {
								if(p.y == minY) dy = 1;
								if(p.y == maxY) dy = -1;
							}
							if(dx != 0 || dy != 0) {
								int key = mc.rgbs[(p.x)/2+dx][((p.y)/2)+dy];
								Vec1 c = counter.get(key);
								if(c == null) {
									c = new Vec1(0);
									counter.put(key, c);
								}
								c.add(1);
								maxCount = Math.max(maxCount, c.i);
							}
						}
						for (int key : counter.keySet()) {
							Vec1 v1 = counter.get(key);
							if(v1.i == maxCount) {
								rgb = key;
								break;
							}
						}
						paths.add(new VecPathArea(corners, rgb, (maxX-minX)*(maxY-minY), minX, minY, maxX, maxY));
					}
					pathId++;
				}
			}
		}
		paths.sort((p1,p2) -> p2.boundsArea() - p1.boundsArea());
		
		Debug.write("fragments/frag@.png");
		
		ArrayList<SvgElement> svg = new ArrayList<SvgElement>();
		SvgElement defs = new SvgElement("defs");
		SvgElement mask = new SvgElement("mask");
		mask.add(new SvgElement("rect")
				.attribute("width", mc.gridWidth()*Main.scale)
				.attribute("height", mc.gridHeight()*Main.scale)
				.attribute("fill", "#fff")
			);
		mask.attribute("id", Strings.toString("alpha-mask"));
		boolean needMask = false;
		
		for (int i = 0; i < paths.size(); i++) {
			var path = paths.get(i);
			SvgElement element = new SvgElement("path");
			element.attribute("d", createPath(path.path));
			if(Main.stroke > 0) {
				element.attribute("stroke", "black");
				element.attribute("stroke-width", Main.stroke + "px");
			}
			element.attribute("fill", Colors.toHex(path.rgb));
			element.attribute("mask", "url(#alpha-mask)");
			if(Colors.alpha(path.rgb) != 255 && svg.size() > 0) {
				needMask = true;
				mask.add(element.copy().attribute("fill", "#000").attribute("stroke-width", null).attribute("stroke", null));
				defs.add(mask);
			} else {
				mask.add(element.copy().attribute("fill", "#fff").attribute("stroke", null));
				defs.add(mask);
			}
			if(Colors.alpha(path.rgb) > 0) svg.add(element);
		}
		if(needMask) {
			svg.add(0, defs);
		}
		return svg;
	}
	
	public static StringBuilder createPath(ArrayList<Node> path) {
		StringBuilder d = new StringBuilder();
		for (var p : path) {
			d.append(d.length() == 0? 'M' : 'L');
			d.append(Strings.toString((p.x-1)*Main.scale));
			d.append(',');
			d.append(Strings.toString((p.y-1)*Main.scale));
			d.append(' ');
		}
		d.append('Z');
		return d;
	}

	record VecPathArea(ArrayList<Node> path, int rgb, int boundsArea, int minX, int minY, int maxX, int maxY) {}
	
}
