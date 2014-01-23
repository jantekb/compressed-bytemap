package hu.javakurzus.bytemap;

import java.util.LinkedList;
import java.util.List;

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

	public void setValue(int x, int y, int length, int value) {
		if (value < 0 || value > 255) {
			throw new IllegalArgumentException("setValue can only handle values in 0..255 range, got " + value);
		}
		setValue(x, y, length, (byte) value);
	}

	public void setValue(int x, int y, int length, byte value) {
		checkRanges(x, y);

		int startBlockIndex = findBlockIndex(y, x);
		int endBlockIndex = findBlockIndex(y, x + length - 1);

		int startBlockBeginningPosition = findBlockIndexStart(y, startBlockIndex);
		int endBlockBeginningPosition = findBlockIndexStart(y, endBlockIndex);

		int endBlockOriginalLength = parseLength(rows[y][endBlockIndex]);

		int endBlockRemainingLength = endBlockOriginalLength - ((x + length) - endBlockBeginningPosition);
		int startBlockRemainingLength = x - startBlockBeginningPosition;

		int hasEndBlockRemainingPart = endBlockRemainingLength > 0 ? 1 : 0;
		int hasStartBlockRemainingPart = startBlockRemainingLength > 0 ? 1 : 0;

		byte startBlockOriginalValue = parseValue(rows[y][startBlockIndex]);
		byte endBlockOriginalValue = parseValue(rows[y][endBlockIndex]);

		int[] modifiedRow;

		int delta = endBlockIndex - startBlockIndex - hasEndBlockRemainingPart - hasStartBlockRemainingPart;

		modifiedRow = new int[rows[y].length - delta];
		System.arraycopy(rows[y], 0, modifiedRow, 0, startBlockIndex);

		modifiedRow[startBlockIndex] = buildBlock(startBlockRemainingLength, startBlockOriginalValue);
		modifiedRow[startBlockIndex + hasStartBlockRemainingPart] = buildBlock(length, value);

		int tailPos = startBlockIndex + hasStartBlockRemainingPart + hasEndBlockRemainingPart;
		if (hasEndBlockRemainingPart > 0) {
			modifiedRow[tailPos] = buildBlock(endBlockRemainingLength, endBlockOriginalValue);
		}

		if (endBlockIndex < rows[y].length - 1) {
			System.arraycopy(rows[y], endBlockIndex + 1, modifiedRow, tailPos + 1, rows[y].length - endBlockIndex - 1);
		}

		rows[y] = modifiedRow;
		return;

	}

	private int findBlockIndexStart(int y, int blockIndex) {
		int result = 0;
		for (int i = 0; i < blockIndex; i++) {
			result += parseLength(rows[y][i]);
		}
		return result;
	}

	private int findBlockIndex(int y, int x) {
		int pointerBlockStart = 0;
		int pointer = 0;
		while (pointer < width) {
			int actual = rows[y][pointer];
			int actualBlockLength = parseLength(actual);
			if (pointerBlockStart + actualBlockLength > x) {
				break;
			} else {
				pointer++;
				pointerBlockStart += actualBlockLength;
			}
		}
		return pointer;
	}

	public byte getValue(int x, int y) {
		checkRanges(x, y);
		int[] row = rows[y];

		int actual = row[findBlockIndex(y, x)];
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

	public void setRow(int y, String pattern) {
		if (!pattern.matches("\\d+")) {
			throw new IllegalArgumentException("Invalid pattern " + pattern + ", should be a string of digits");
		} else {
			char lastChar = pattern.charAt(0);
			int blockLength = 0;
			List<Integer> row = new LinkedList<Integer>();
			for (int i = 0; i < pattern.length(); i++) {
				if (pattern.charAt(i) != lastChar) {
					row.add(buildBlock(blockLength, Byte.parseByte(Character.toString(lastChar))));
					lastChar = pattern.charAt(i);
					blockLength = 1;
				} else {
					blockLength++;
				}
			}
			row.add(buildBlock(blockLength, Byte.parseByte(Character.toString(lastChar))));
			rows[y] = new int[row.size()];
			for (int i = 0; i < row.size(); i++) {
				rows[y][i] = row.get(i);
			}
		}
	}

	private int getMemoryFootprint() {
		int memory = OBJECT_REF_SIZE;
		for (int i = 0; i < height; i++) {
			memory += rows[i].length * 4 + OBJECT_REF_SIZE;
		}
		return memory;
	}

	public void addValue(int x, int y, int length, int value) {
		if (value < 0 || value > 255) {
			throw new IllegalArgumentException("addValue can only handle values in 0..255 range, got " + value);
		}
		addValue(x, y, length, (byte) value);
	}

	public void addValue(int x, int y, int length, byte value) {
		checkRanges(x, y);

		int startBlockIndex = findBlockIndex(y, x);
		int endBlockIndex = findBlockIndex(y, x + length - 1);

		int startBlockBeginningPosition = findBlockIndexStart(y, startBlockIndex);
		int endBlockBeginningPosition = findBlockIndexStart(y, endBlockIndex);

		int endBlockOriginalLength = parseLength(rows[y][endBlockIndex]);
		int startBlockOriginalLength = parseLength(rows[y][startBlockIndex]);

		int endBlockRemainingLength = endBlockOriginalLength - ((x + length) - endBlockBeginningPosition);
		int startBlockRemainingLength = x - startBlockBeginningPosition;

		int hasEndBlockRemainingPart = endBlockRemainingLength > 0 ? 1 : 0;
		int hasStartBlockRemainingPart = startBlockRemainingLength > 0 ? 1 : 0;

		byte startBlockOriginalValue = parseValue(rows[y][startBlockIndex]);
		byte endBlockOriginalValue = parseValue(rows[y][endBlockIndex]);

		int[] modifiedRow;

		int delta = hasEndBlockRemainingPart + hasStartBlockRemainingPart;

		modifiedRow = new int[rows[y].length + delta];
		System.arraycopy(rows[y], 0, modifiedRow, 0, startBlockIndex);

		modifiedRow[startBlockIndex] = buildBlock(startBlockRemainingLength, (byte) (startBlockOriginalValue));

		int trick = startBlockIndex == endBlockIndex ? endBlockRemainingLength : 0;

		modifiedRow[startBlockIndex + hasStartBlockRemainingPart] = buildBlock(startBlockOriginalLength
				- startBlockRemainingLength - trick, (byte) (startBlockOriginalValue + value));

		int tailPos = endBlockIndex + hasStartBlockRemainingPart + hasEndBlockRemainingPart;
		if (hasEndBlockRemainingPart > 0) {
			modifiedRow[tailPos] = buildBlock(endBlockRemainingLength, endBlockOriginalValue);
		}

		if (startBlockIndex != endBlockIndex) {
			modifiedRow[tailPos - hasEndBlockRemainingPart] = buildBlock(endBlockOriginalLength
					- endBlockRemainingLength, (byte) (endBlockOriginalValue + value));
			if (endBlockIndex < rows[y].length - 1) {
				System.arraycopy(rows[y], endBlockIndex + 1, modifiedRow, tailPos + 1, rows[y].length - endBlockIndex
						- 1);
			}

			if (endBlockIndex > startBlockIndex) {
				System.arraycopy(rows[y], startBlockIndex + 1, modifiedRow, startBlockIndex
						+ hasStartBlockRemainingPart + 1, endBlockIndex - startBlockIndex - 1);
			}

			for (int i = startBlockIndex + 1; i < endBlockIndex; i++) {
				modifiedRow[i + hasStartBlockRemainingPart] = incrementBlockValue(rows[y][i], value);
			}
		}

		rows[y] = modifiedRow;
	}

	private int incrementBlockValue(int i, int valueToAdd) {
		return buildBlock(parseLength(i), (byte) (parseValue(i) + valueToAdd));
	}
}
