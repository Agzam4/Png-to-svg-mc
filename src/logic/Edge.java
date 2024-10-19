package logic;

public class Edge {

	public int rgb1, rgb2;
	public int angle = 0;
	public final Node from, target;
	
	public Edge(Node from, Node target) {
		this.from = from;
		this.target = target;
		this.angle = Geometry.angle(from.x, from.y, target.x, target.y);
	}
}
