package logic;

public class Vec1 {
	
	public int i;
	
	public Vec1(int i) {
		this.i = i;
	}

	public Vec1 add(int i) {
		this.i += i;
		return this;
	}

	@Override
	public String toString() {
		return i + "";
	}

	public Vec1 copy() {
		return new Vec1(i);
	}
}
