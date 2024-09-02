package logic.cases;

import logic.MulticolorsMarchingSquares;
import logic.Node;
import main.Debug;

public class VCase {

	/**
	 * <pre>
	 * {@code
	 * 	╔═══════════════════╗
	 * 	║                   ║
	 * 	║        NS         ║
	 * 	║       /  \        ║
	 * 	║     N1-N_-N2      ║
	 * 	║    /        \     ║
	 * 	║   R1         R2   ║
	 * 	║                   ║
	 * 	╚═══════════════════╝}
	 * @author Agzam4
	 */
	public static void apply(MulticolorsMarchingSquares mc) {
		for (int y = 0; y < mc.h*2+1; y++) {
			for (int x = 0; x < mc.w*2+1; x++) {
				Node n = mc.grid[x][y];
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
				
				Debug.color(255,0,255,150);
				Debug.fillOval(n);

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
				
				Node ns = mc.node(x1, y1);

				ns.link(n1);
				ns.link(n2);
				mc.removeNode(n);

				Debug.color(116,0,255,150);
				Debug.fillOval(ns);
			}
		}		
	}

}
