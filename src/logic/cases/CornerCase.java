package logic.cases;

import logic.MulticolorsMarchingSquares;
import logic.Node;

public class CornerCase {


	/**
	 * <pre>
	 * {@code
	 * 	╔══════════════╗
	 * 	║              ║
	 * 	║   R2-N2-NS   ║
	 * 	║        \|    ║
	 * 	║         N    ║
	 * 	║         |    ║
	 * 	║         N1   ║
	 * 	║              ║
	 * 	╚══════════════╝}
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


				Node ns = mc.node(n2.x - dx, n2.y - dy);

				n.unlink(n2);
				ns.link(n);
				ns.link(n2);
			}
		}		
	}

}
