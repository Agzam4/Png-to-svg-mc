package logic.cases;

import logic.MulticolorsMarchingSquares;
import logic.Node;

public class PigeonCase {

	/*
	 * <pre>
	 * {@code
	 * ╔═════════════════╗
	 * ║                 ║
	 * ║        R0       ║
	 * ║        |        ║
	 * ║        N0       ║
	 * ║        |        ║
	 * ║     N2-N-N3     ║
	 * ║    /   |   \    ║
	 * ║  R2    N1   R3  ║
	 * ║        |        ║
	 * ║        R1       ║ 
	 * ║                 ║
	 * ╚═════════════════╝
	 *        [O R]
	 * ╔═════════════════╗
	 * ║                 ║
	 * ║       R2        ║
	 * ║       |         ║
	 * ║       N2        ║
	 * ║       |         ║
	 * ║    N0-N-N1      ║
	 * ║   /   |   \     ║
	 * ║ R0    N3   R1   ║
	 * ║       |         ║
	 * ║       R3        ║
	 * ║                 ║
	 * ╚═════════════════╝} 
	 * 
	 * @author hortiSquash
	 */
	public static void apply(MulticolorsMarchingSquares mc) {
		for (int y = 0; y < mc.h*2+1; y++) {
			for (int x = 0; x < mc.w*2+1; x++) {
				Node n = mc.grid[x][y];
				if(n == null) continue;
				if(n.links() != 4) continue;

				Node n0 = n.get(0);
				Node n1 = n.get(1);
				Node n2 = n.get(2);
				Node n3 = n.get(3);

				if(n.diagonal(n0)) continue;
				if(n.diagonal(n1)) continue;
				if(n.diagonal(n2)) continue;
				if(n.diagonal(n3)) continue;

				if(n0.links() != 2) continue;
				if(n1.links() != 2) continue;
				if(n2.links() != 2) continue;
				if(n3.links() != 2) continue;

				//reorder the nodes so that n0/n1 are opposite, and n2/n3 are opposite
				if(n0.diagonal(n1)){
					var tmp = n1;
					if(n1.diagonal(n3)){ //n3 is opposite of n0
						n1 = n3;
						n3 = tmp;
					}else{ //n3 is same side as n1 -> n2 is opposite of n0
						n1 = n2;
						n2 = tmp;
					}
				}

				Node r0 = n0.next(n);
				Node r1 = n1.next(n);
				Node r2 = n2.next(n);
				Node r3 = n3.next(n);

				if(((n0.diagonal(r0)?1:0) + (n1.diagonal(r1)?1:0) + (n2.diagonal(r2)?1:0) + (n3.diagonal(r3)?1:0)) != 2) continue;

				if(!r0.diagonal(n1)) { //n0 n1 vertical line, n2 n3 arms
					if(r2.diagonal(r3)) continue; //skip if r2 r3 arent aligned, meaning the two arms are opposite side

					n.unlink(n2);
					n.unlink(n3);
					if(!n0.diagonal(r2)) { //n0 on the side of the arms, so n1 is opposite
						n1.link(n2);
						n1.link(n3);
					} else {
						n0.link(n2);
						n0.link(n3);
					}

				} else { //n2 n3 vertical line, n0 n1 arms
					if(r0.diagonal(r1)) continue; //skip if r0 r1 arent aligned, meaning the two arms are opposite side
					n.unlink(n0);
					n.unlink(n1);
					if(!n2.diagonal(r1)) { //n2 on the side of the arms, so n3 is opposite
						n3.link(n0);
						n3.link(n1);
					} else {
						n2.link(n0);
						n2.link(n1);
					}
				}
			}
		}		
	}

}
