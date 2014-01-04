package hu.javakurzus.bytemap;

public class Bytemap {

	private final int width;
	
	private final int height;

	private int[][] rows;

	private static int MAX_HOMO_LENGTH = (int) Math.pow(2, 24) - 1;

	public Bytemap(int width, int height) {
		this.width = width;
		this.height = height;

		initialize();

	}

	private void initialize() {
		this.rows = new int[height][];

		for (int i = 0; i < height; i++) {

			rows[i] = new int[] { 0 };
		}
	}

	public int getHeigth() {
		return height;
	}
	
	public int getWidth() {
		return width;
	}

	public int[] getHomoLine(int length, byte value) {
		if (length < MAX_HOMO_LENGTH) {
			return new int[] { (length << 8) + value };
		} else {
			int[] remaining = getHomoLine(length - MAX_HOMO_LENGTH, value);
			int[] result = new int[remaining.length + 1];
			System.arraycopy(remaining, 0, result, 1, remaining.length);
			result[0] = (MAX_HOMO_LENGTH << 8) | value;
			return result;
		}
	}

	public int parseLength(int data) {
		return data >>> 8;
	}

	public byte parseValue(int data) {
		return (byte) (data & 0xFF);
	}
}
