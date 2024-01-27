package logic;

public class Vec2 {
	
	public int x, y;
	
	public Vec2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vec2 add(int x, int y) {
		x += x;
		y += y;
		return this;
	}

	@Override
	public String toString() {
		return x + " " + y;
	}

	public Vec2 copy() {
		return new Vec2(x, y);
	}
}
