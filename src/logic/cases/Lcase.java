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
	 * @author Agzam4 and hortiSquash
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

				if(n1.links() != 3 && n2.links() != 3) continue; // At least one of the two has 3 links
				if(n1.links() != 3) {
					n1 = n.get(1);
					n2 = n.get(0);
				}
				int dx = n.x - n1.x;
				int dy = n.y - n1.y;
				Node n3 = mc.grid[n1.x + dx*2][n1.y];
				Node n4 = mc.grid[n1.x + dx][n1.y];
				if(n4 == null || n3 == null) {
					n3 = mc.grid[n1.x][n1.y + dy*2];
					n4 = mc.grid[n1.x][n1.y + dy];
				}
				if(n4 == null || n3 == null) continue;
				@SuppressWarnings("unchecked")
				ArrayList<Node> links = (ArrayList<Node>) n3.links.clone();
				for(Node l : links) {
					if(!l.diagonal(n) || !l.diagonal(n3)) continue;
					n3.link(n);
					n4.removeLinks();
				}
			}
		}
	}

}
