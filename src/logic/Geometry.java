package logic;

public class Geometry {
	
	/**
	 * {@code
	 * [1][2][3]
	 * [0] C [4]
	 * [7][6][5]
	 * }
	 */
	public static final Vec2[] delta = {
			new Vec2(-1,  0), 
			new Vec2(-1, -1), 
			new Vec2(0, -1), 
			new Vec2(1, -1),  
			new Vec2(1,  0), 
			new Vec2(1,  1), 
			new Vec2(0,  1), 
			new Vec2(-1,  1)
	};
	

	public static Vec2 delta(int angle) {
		angle = angle%delta.length;
		if(angle < 0) angle = (angle+delta.length)%delta.length;
		return delta[angle];
	}
	
	public static final String[] deltaNames = {
			"left", "top left", "top", "top right", "right", "down right", "down", "down left"
	};

	/**
	 * @param dx - delta x
	 * @param dy - delta y
	 * @return angle (id) by delta
	 */
	public static int angle(int dx, int dy) {
		if(dx > 1) dx = 1;
		if(dy > 1) dy = 1;
		for (int i = 0; i < delta.length; i++) {
			if(delta[i].x == dx && delta[i].y == dy) return i;
		}
		System.err.println("Angle not found: " + dx + " " + dy);
		return -1;
	}

	/**
	 * @param x1 - "from" x position
	 * @param y1 - "from" y position
	 * @param x2 - "to" x position
	 * @param y2 - "to" y position
	 * @return
	 */
	public static int angle(int x1, int y1, int x2, int y2) {
		return angle(x2-x1, y2-y1);
	}
}
