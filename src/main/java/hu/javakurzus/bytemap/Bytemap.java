package hu.javakurzus.bytemap;

public class Bytemap {

	private static final int OBJECT_REF_SIZE = 12;

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
			rows[i] = getHomoLine(width, (byte) 0);
		}
	}

	public int getHeigth() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setValue(int x, int y, int length, byte value) {
		checkRanges(x, y);

		int startBlockIndex = findBlockIndexForFrom(0, y, x);
		int endBlockIndex = findBlockIndexForFrom(startBlockIndex, y, x + length - 1);

		if (startBlockIndex == endBlockIndex) {
			if (x == findBlockIndexStart(y, startBlockIndex)) {
				int startBlockLength = parseLength(rows[y][startBlockIndex]);
				if (length == startBlockLength) {
					rows[y][startBlockIndex] = changeBlockValue(rows[y][startBlockIndex], value);
					return;
				} else {
					int extendedLength = rows[y].length + 1;
					int[] extendedRow = new int[extendedLength];
					System.arraycopy(rows[y], 0, extendedRow, 0, startBlockIndex - 1);
					System.arraycopy(rows[y], startBlockIndex, extendedRow, startBlockIndex + 1, extendedLength
							- (startBlockIndex));

					extendedRow[startBlockIndex] = buildBlock(length, value);

					byte startBlockOriginalValue = parseValue(rows[y][startBlockIndex]);
					extendedRow[startBlockIndex + 1] = buildBlock(startBlockLength - length, startBlockOriginalValue);
					return;
				}
			}
		}

		throw new IllegalStateException("This should not have happened. Ever.");
	}

	private int changeBlockValue(int data, byte value) {
		return buildBlock(parseLength(data), value);
	}

	private int findBlockIndexStart(int y, int blockIndex) {
		int result = 0;
		for (int i = 0; i < blockIndex; i++) {
			result += parseLength(rows[y][blockIndex]);
		}
		return result;
	}

	private int findBlockIndexForFrom(int pointerStart, int y, int x) {
		int pointerBlockStart = pointerStart;
		int pointer = 0;
		while (pointer < width) {
			int actual = rows[y][pointer];
			int actualBlockLength = parseLength(actual);
			if (pointerBlockStart + actualBlockLength > x) {
				return pointer;
			} else {
				pointer++;
				pointerBlockStart += actualBlockLength;
			}
		}

		throw new IllegalStateException("This should not have happened. Ever.");
	}

	public byte getValue(int x, int y) {
		checkRanges(x, y);
		int[] row = rows[y];

		int actual = row[findBlockIndexForFrom(0, y, x)];
		return parseValue(actual);

	}

	private void checkRanges(int x, int y) {
		if (y < 0 || y >= height) {
			throw new IllegalArgumentException(String.format(
					"Provided y value %d is out of bounds, should be in range 0..%d", y, height - 1));
		}
		if (y < 0 || y >= width) {
			throw new IllegalArgumentException(String.format(
					"Provided X value %d is out of bounds, should be in range 0..%d", x, width - 1));
		}
	}

	public int[] getHomoLine(int length, byte value) {
		if (length < MAX_HOMO_LENGTH) {
			return new int[] { buildBlock(length, value) };
		} else {
			int[] remaining = getHomoLine(length - MAX_HOMO_LENGTH, value);
			int[] result = new int[remaining.length + 1];
			System.arraycopy(remaining, 0, result, 1, remaining.length);
			result[0] = buildBlock(MAX_HOMO_LENGTH, value);
			return result;
		}
	}

	public int buildBlock(int length, byte value) {
		return (length << 8) | value;
	}

	public int parseLength(int data) {
		return data >>> 8;
	}

	public byte parseValue(int data) {
		return (byte) (data & 0xFF);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int memory = getMemoryFootprint();
		String compr = String.format("%d%%", (int) ((float) memory / (width * height * 4) * 100));
		sb.append(String.format("Bytemap of size %d × %d consuming %d bytes (compr. %s)%n", width, height, memory,
				compr));
		for (int y = 0; y < height; y++) {
			sb.append(printRow(y));
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	public String printRow(int y) {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < width; x++) {
			sb.append(Byte.toString(getValue(x, y)));
		}
		return sb.toString();
	}

	private int getMemoryFootprint() {
		int memory = OBJECT_REF_SIZE;
		for (int i = 0; i < height; i++) {
			memory += rows[i].length * 4 + OBJECT_REF_SIZE;
		}
		return memory;
	}

	public void temporaryTestHook(int i, int[] js) {
		rows[i] = js;
	}
}
