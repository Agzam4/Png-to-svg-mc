package logic;

import java.util.ArrayList;

public class Node {

	ArrayList<Node> links = new ArrayList<Node>();

	int x, y;
	
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Node get(int index) {
		return links.get(index);
	}

	public void unlink(Node link) {
		links.remove(link);
		link.links.remove(this);
	}
	
	public void link(Node n) {
		if(n == this) return;
		if(Math.abs(x - n.x) > 1) System.err.println("Link x size > 1");
		if(Math.abs(y - n.y) > 1) System.err.println("Link y size > 1");
		if(!links.contains(n)) links.add(n);
		if(!n.links.contains(this)) n.links.add(this);
	}
	
	@Override
	public String toString() {
		return x + " " + y;
	}

	public boolean diagonal(Node n) {
		return (x-n.x)*(y-n.y) != 0;
	}

	public boolean contains(Node n) {
		return links.contains(n);
	}

	public boolean contains(ArrayList<Node> links) {
		for (Node l : links) if(contains(l)) return true;
		return false;
	}

	public int links() {
		return links.size();
	}

	public void removeLinks() {
		for (Node l : links) {
			l.links.remove(this);
		}
		links.clear();
	}

	public boolean canLink(Node n) {
		return Math.abs(x - n.x) <= 1 && Math.abs(y - n.y) <= 1;
	}

	public void linkOneAvalible(Node... ns) {
		for (int i = 0; i < ns.length; i++) {
			if(!ns[i].canLink(this)) continue;
			ns[i].link(this);
			break;
		}
	}
}
