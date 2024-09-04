package logic.cases;

import logic.MulticolorsMarchingSquares;
import logic.Node;
import main.Debug;

public class YCase {

	/**
	 * <pre>
	 * {@code
	 * 	╔═══════════════════════╗
	 * 	║                       ║
	 * 	║   R                   ║
	 * 	║    \                  ║
	 * 	║     R1   NS           ║
	 * 	║     |\  /  \          ║
	 * 	║     N-S1-S2-S3        ║
	 * 	║     |/        \       ║
	 * 	║     R2         SR     ║
	 * 	║    /                  ║
	 * 	║   R                   ║
	 * 	║                       ║
	 * 	╚═══════════════════════╝}
	 * @author Agzam4
	 */
	public static void apply(MulticolorsMarchingSquares mc) {
		for (int y = 0; y < mc.h*2+1; y++) {
			for (int x = 0; x < mc.w*2+1; x++) {
				Node n = mc.grid[x][y];
				if(n == null) continue;
				if(n.links() != 3) continue;

				Node n0 = n.get(0);
				Node n1 = n.get(1);
				Node n2 = n.get(2);

				if(n0.links() != 2 || n1.links() != 2 || n2.links() != 2) continue;

				if(n.diagonal(n0) || n.diagonal(n1) || n.diagonal(n2)) continue;

				// Radicals
				Node nr0 = n0.next(n);
				Node nr1 = n1.next(n);
				Node nr2 = n2.next(n);

				if((n0.diagonal(nr0)?1:0) + (n1.diagonal(nr1)?1:0) + (n2.diagonal(nr2)?1:0) != 2) continue;
				
				if(mc.debugImage) {
					Debug.color(0,0,255,50);
					Debug.fillOval(n);
					Debug.color(0,180,255,150);
					Debug.fillOval(n0);
					Debug.fillOval(n1);
					Debug.fillOval(n2);
					Debug.color(0,255,255,150);
				}
				
				Node s1 = n0.diagonal(nr0) ? (n1.diagonal(nr1) ? n2 : n1) : n0;
				// We already tested the links earlier, since s1 ∈ {n0, n1, n2}
				Debug.color(255,0,255,150);
				Debug.fillOval(s1);

				int isWrongSide = 0;
				for(Node nx : n.links){
					if(nx == s1) continue; //for n0,n1,n2 that arent s1
					if(!s1.diagonal(nx.next(n))) isWrongSide++; //skip if s1 is diagonal to rx
				}
				if(isWrongSide == 2) continue;

				Node s2 = s1.next(n);
				if(s2.links() != 2) continue;
				Debug.color(255,0,255,50);
				Debug.fillOval(s2);

				Node s3 = s2.next(s1);
				if(s3.links() != 2) continue;
				Debug.color(255,0,127,50);
				Debug.fillOval(s3);

				Node sr = s3.next(s2);
				Debug.fillOval(sr);

				Node ns = mc.node(s3.x*2 - sr.x, s3.y*2 - sr.y);

				//it cant link to itself so why bother
				s1.link(n0);
				s1.link(n1);
				s1.link(n2);
				mc.removeNode(n);
				
				if(s2 != ns) {
					ns.link(s3);
					ns.link(s1);
					mc.removeNode(s2);
				}
			}
		}		
	}

}
