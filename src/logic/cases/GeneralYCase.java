package logic.cases;

import logic.MulticolorsMarchingSquares;
import logic.Node;

public class GeneralYCase {

	/**
	 * <pre>
	 * {@code
	 * ╔════════════════════╗
	 * ║          |         ║
	 * ║     n0 - n - n1    ║
	 * ║    /     |    \    ║
	 * ║  nr0          nr1  ║
	 * ╚════════════════════╝
	 *            ↓
	 * ╔════════════════════╗
	 * ║          s1        ║
	 * ║       /  |  \      ║
	 * ║     n0 - n - n1    ║
	 * ║    /     |    \    ║
	 * ║  nr0    s2    nr1  ║
	 * ╚════════════════════╝}
	 * @author hortiSquash
	 */

	public static void apply(MulticolorsMarchingSquares mc) {
		for (int y = 0; y < mc.h*2+1; y++) {
			for (int x = 0; x < mc.w*2+1; x++) {
				Node n = mc.grid[x][y];
				if(n == null) continue;
				if(n.links() < 2) continue;
				outer:
				for(int i = 0; i < n.links() - 1; i++) {
					Node n0 = n.get(i);

					if(n0.links() != 2) continue;
					if(n0.diagonal(n)) continue;
					Node nr0 = n0.next(n);
					if(!nr0.diagonal(n)) continue; //only nr that are (2, 1) away from n

					for(int j = i + 1; j < n.links(); j++) {
						Node n1 = n.get(j);

						if(n1.links() != 2) continue;
						if(n1.diagonal(n)) continue;
						Node nr1 = n1.next(n);
						if(!nr1.diagonal(n)) continue; // Only nr that are (2, 1) away from n

						if(nr0.diagonal(nr1)) continue;
						// Only if arms are on the same side

						int x0 = 2*n0.x - nr0.x;
						int x1 = 2*n1.x - nr1.x;
						int y0 = 2*n0.y - nr0.y;
						int y1 = 2*n1.y - nr1.y;
						if((x0 != x1) || (y0 != y1)) {
							System.err.println("General Y case: ("+n.x+","+n.y+") different s1 found");
							continue;
						}
						Node s1 = mc.node(x0, y0);

						Node s2 = null;
						for(Node nx : n.links) {
							if(!nx.diagonal(nr0)) { // If between the arms of the Y
								s2 = nx;
							} else {
								nx.link(s1);
							}
						}

						if(s2 != null) {
							n.link(s1);
							n.link(s2);
						}

						n.unlink(n0);
						n.unlink(n1);

						break outer;
					}
				}
			}
		}
	}
}
