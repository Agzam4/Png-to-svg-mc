package logic;

import java.awt.Color;
import java.util.ArrayList;

public class Node {

	private ArrayList<Link> links = new ArrayList<Link>(); // Need replace to HashMap or Set or array?
	public ArrayList<Edge> edges = new ArrayList<Edge>();

	public final int x, y;
	
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @param index - index of link
	 * @return link by index
	 * @author Agzam4
	 */
	public Link get(int index) {
		return links.get(index);
	}

	/**
	 * @return connected node on the opposite side of "previous"
	 * @author Agzam4
	 */
	public Node next(Node previous) {
		return this.get(0).target != previous ? this.get(0).target : this.get(1).target;
	}

	/**
	 * Removing link from current node and making same action for linked node
	 * @param linked
	 * @author Agzam4
	 */
	public void unlink(Node linked) {
		for (int i = 0; i < links.size(); i++) {
			if(links.get(i).target == linked) {
				links.remove(i);
				break;
			}
		}
		for (int i = 0; i < linked.links.size(); i++) {
			if(linked.links.get(i).target == this) {
				linked.links.remove(i);
				break;
			}
		}
	}
	
	/**
	 * Tmp method for legacy code
	 */
	@Deprecated
	public void link(Node n) {
		link(n, Color.magenta.getRGB(), Color.magenta.getRGB());//(int) (Math.random()*Integer.MAX_VALUE), (int) (Math.random()*Integer.MAX_VALUE));
	}
	
	public void link(Node n, int rgbr, int rgbl) {
		if(n == this) return;
		if(Math.abs(x - n.x) > 1) System.err.println("Link x size > 1");
		if(Math.abs(y - n.y) > 1) System.err.println("Link y size > 1");
		// is i need rewrite rgbs then linking again?
		if(!contains(n)) links.add(new Link(this, n, rgbr, rgbl));
		if(!n.contains(this)) n.links.add(new Link(n, this, rgbl, rgbr));
	}
	
	/**
	 * Checking is Node has link to other Node
	 * @param n - target node
	 * @return {@code true} if has and {@code false} otherside
	 * @author Agzam4
	 */
	public boolean contains(Node n) {
		for (int i = 0; i < links.size(); i++) {
			if(links.get(i).target == n) return true;
		}
		return false;
	}

	/**
	 * Searching Node link index
	 * @param n - target node
	 * @return index of link or {@code -1} of not found
	 * @author Agzam4
	 */
	public int indexOf(Node n) {
		for (int i = 0; i < links.size(); i++) {
			if(links.get(i).target == n) return i;
		}
		return -1;
	}
	
	@Override
	public String toString() {
		return x + " " + y;
	}

	public boolean diagonal(Node n) {
		return (x-n.x)*(y-n.y) != 0;
	}

	public boolean contains(ArrayList<Node> links) {
		for (Node l : links) if(contains(l)) return true;
		return false;
	}

	public int links() {
		return links.size();
	}

	/**
	 * Removes all links from current Node and links to current node from links
	 * @author Agzam4
	 */
	public void removeLinks() {
		for (int lid = 0; lid < links(); lid++) {
			Link l = get(lid);
			// Removing link to current node of "l"
			for (int i = 0; i < l.target.links.size(); i++) {
				if(l.target.links.get(i).target == this) {
					l.target.links.remove(i);
					break;
				}
			}
		}
		links.clear();
	}

	/**
	 * Validation of link by distance
	 * @param n - target Node
	 * @return {@code true} if it can be linked and {@code false} otherside
	 * @author Agzam4
	 */
	public boolean canLink(Node n) {
		return Math.abs(x - n.x) <= 1 && Math.abs(y - n.y) <= 1;
	}

	@Deprecated
	public void linkOneAvailable(Node... ns) {
        for (Node n : ns) {
            if (!n.canLink(this)) continue;
            n.link(this, 0, 0);
            break;
        }
	}
	
	@Deprecated
	public ArrayList<Link> getLinks() {
		return links;
	}
	
	public class Link {

		private final Node from;
		public final Node target;
		public final int rgbl, rgbr;
		
		private Link(Node from, Node target, int rgbr, int rgbl) {
			this.from = from;
			this.target = target;
			this.rgbr = rgbr;
			this.rgbl = rgbl;
		}
		
		@Override
		public String toString() {
			return "(" + from + ")=>(" + target + ")";
		}
	}

}
