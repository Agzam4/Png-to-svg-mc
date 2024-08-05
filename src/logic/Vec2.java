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
	
	public static long pack(int x, int y) {
		return (((long)x) << 32) | (y & 0xffffffffL);
	}

	public Vec2 invert() {
		x = -x;
		y = -y;
		return this;
	}
}
