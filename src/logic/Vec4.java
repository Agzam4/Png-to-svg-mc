package logic;

public class Vec4 {
	
	public int x1, y1, x2, y2;
	
	public Vec4(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}

	public Vec4 add(int x, int y) {
		x1 += x;
		x2 += x;
		
		y1 += y;
		y2 += y;
		return this;
	}

	public void flipX(int x) {
		x1 = x-x1;
		x2 = x-x2;
	}
	
	public void flipY(int y) {
		y1 = y-y1;
		y2 = y-y2;
	}
	
	@Override
	public String toString() {
		return x1 + "" + y1 + " -> " + x2 + " " + y2;
	}

	public Vec4 copy() {
		return new Vec4(x1, y1, x2, y2);
	}
}
