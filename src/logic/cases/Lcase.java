package logic.cases;

import java.util.ArrayList;

import logic.MulticolorsMarchingSquares;
import logic.Node;

public class Lcase {

	/**
	 * <pre>
	 * {@code
	 * 	╔══════════════╗
	 * 	║              ║
	 * 	║   N1         ║
	 * 	║   | \        ║
	 * 	║   |  N       ║
	 * 	║   | / \      ║
	 * 	║   R1   N2    ║
	 * 	║              ║
	 * 	╚══════════════╝}
	 * 
	 * @author hortiSquash
	 */
	public static void apply(MulticolorsMarchingSquares mc) {
		for (int y = 0; y < mc.h*2+1; y++) {
			for (int x = 0; x < mc.w*2+1; x++) {
				Node n = mc.grid[x][y];
				if(n == null) continue;
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
				if(mc.grid[n1.x + dx][n1.y] != null && mc.grid[n1.x + dx*2][n1.y] != null) {
					Node n3 = mc.grid[n1.x + dx*2][n1.y];
					@SuppressWarnings("unchecked")
					ArrayList<Node> links = (ArrayList<Node>) n3.links.clone();
					for(Node l : links) {
						if(!l.diagonal(n) || !l.diagonal(n3)) continue;
						mc.grid[n1.x + dx*2][n1.y].link(n);
						mc.removeNode(n1.x + dx, n1.y);
					}
				}
				if(mc.grid[n1.x][n1.y + dy] != null && mc.grid[n1.x][n1.y + dy*2] != null) {
					Node n3 = mc.grid[n1.x][n1.y + dy*2];
					@SuppressWarnings("unchecked")
					ArrayList<Node> links = (ArrayList<Node>) n3.links.clone();
					for(Node l : links) {
						if(!l.diagonal(n) || !l.diagonal(n3)) continue;
						mc.grid[n1.x][n1.y + dy*2].link(n);
						mc.removeNode(n1.x, n1.y + dy);
					}
				}
			}
		}
	}

}
