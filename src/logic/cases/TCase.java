package logic.cases;

import logic.MulticolorsMarchingSquares;
import logic.Node;

public class TCase {

	/**
	 * @author Agzam4
	 */
	public static void apply(MulticolorsMarchingSquares mc) {
		for (int y = 0; y < mc.h*2+1; y++) {
			for (int x = 0; x < mc.w*2+1; x++) {
				Node n = mc.grid[x][y];
				if(n == null) continue;
				if(n.links.size() != 3) continue;

				Node n0 = n.links.get(0);
				Node n1 = n.links.get(1);
				Node n2 = n.links.get(2);

				// is horizontal
				boolean h0 = y-n0.y == 0;
				boolean h1 = y-n1.y == 0;
				boolean h2 = y-n2.y == 0;

				if(n.diagonal(n0) || n.diagonal(n1) || n.diagonal(n2)) continue;

				int hc = (h0?1:0) + (h1?1:0) + (h2?1:0);

				Node r;
				if(hc == 1) { // "├" case
					r = h0 ? n0 : (h1 ? n1 : n2);
				} else { // "T" case
					r = h0 ? (h1 ? n2 : n1) : n0;
				}

				if(r.links() != 2) continue;
				Node rr = r.next(n);

				int rdx = r.x - rr.x;
				int rdy = r.y - rr.y;
				if(!r.diagonal(rr)) continue;
				n.unlink(r);
				r.link(mc.node(r.x + rdx, r.y + rdy));
			}
		}
	}
}
