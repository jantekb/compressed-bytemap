package hu.javakurzus.bytemap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

public class BytemapTest {

	private Bytemap classUnderTest;

	@Before
	public void setup() {
		classUnderTest = new Bytemap(15, 1);
		
		classUnderTest.temporaryTestHook(0,
				new int[] { classUnderTest.buildBlock(5, (byte) 0), classUnderTest.buildBlock(5, (byte) 1),
						classUnderTest.buildBlock(5, (byte) 2) });
	}

	@Test
	public void testWidthAndHeightSetting() {
		Bytemap map = new Bytemap(1, 2);

		assertThat(map.getHeigth(), equalTo(2));
		assertThat(map.getWidth(), equalTo(1));
	}

	@Test
	public void testGetHomoLineForValuesThatFit24bits() {
		int[] line = classUnderTest.getHomoLine(100, (byte) 1);

		assertThat(line.length, equalTo(1));
		assertThat(classUnderTest.parseLength(line[0]), equalTo(100));
		assertThat(classUnderTest.parseValue(line[0]), equalTo((byte) 1));

		line = classUnderTest.getHomoLine(0, (byte) 0);
		assertThat(line.length, equalTo(1));
		assertThat(classUnderTest.parseLength(line[0]), equalTo(0));
		assertThat(classUnderTest.parseValue(line[0]), equalTo((byte) 0));

		line = classUnderTest.getHomoLine(42, (byte) 13);
		assertThat(line.length, equalTo(1));
		assertThat(classUnderTest.parseLength(line[0]), equalTo(42));
		assertThat(classUnderTest.parseValue(line[0]), equalTo((byte) 13));
	}

	@Test
	public void testGetHomoLineForValuesThatExceeds24bits() {
		int biglength = 16777216;

		int[] line = classUnderTest.getHomoLine(biglength, (byte) 1);

		assertThat(line.length, equalTo(2));

		int segment1 = classUnderTest.parseLength(line[0]);
		int segment2 = classUnderTest.parseLength(line[1]);

		assertThat(segment1 + segment2, equalTo(16777216));

		assertThat(classUnderTest.parseValue(line[0]), equalTo((byte) 1));
		assertThat(classUnderTest.parseValue(line[1]), equalTo((byte) 1));

	}

	@Test
	public void setValueWhenExactlyOneFullBlockIsModified() {
		assertThat(classUnderTest.printRow(0), equalTo("000001111122222"));

		classUnderTest.setValue(5, 0, 5, (byte) 9);
		assertThat(classUnderTest.printRow(0), equalTo("000009999922222"));

		classUnderTest.setValue(0, 0, 5, (byte) 7);
		assertThat(classUnderTest.printRow(0), equalTo("777779999922222"));

		classUnderTest.setValue(10, 0, 5, (byte) 8);
		assertThat(classUnderTest.printRow(0), equalTo("777779999988888"));
	}
	
	@Test
	public void setValueWhenChangeFitsIntoOneBlockAndIsAlignedToBlockStart() {
		assertThat(classUnderTest.printRow(0), equalTo("000001111122222"));
		
		classUnderTest.setValue(0, 0, 2, (byte) 9);
		assertThat(classUnderTest.printRow(0), equalTo("990001111122222"));
		
		classUnderTest.setValue(5, 0, 3, (byte) 8);
		assertThat(classUnderTest.printRow(0), equalTo("990008881122222"));
		
		classUnderTest.setValue(10, 0, 1, (byte) 3);
		assertThat(classUnderTest.printRow(0), equalTo("990008881132222"));
		
	}
	
	
	@Test
	public void setValueWhenChangeSpansOverMultipleBlocksAndIsAlignedToBlockStart_first() {
		classUnderTest = new Bytemap(20, 1);
		classUnderTest.temporaryTestHook(0,
				new int[] { classUnderTest.buildBlock(5, (byte) 0),
				            classUnderTest.buildBlock(5, (byte) 1),
						    classUnderTest.buildBlock(5, (byte) 2),
						    classUnderTest.buildBlock(5, (byte) 3) });
		
		assertThat(classUnderTest.printRow(0), equalTo("00000111112222233333"));
		
		classUnderTest.setValue(0, 0, 10, (byte) 9);
		assertThat(classUnderTest.printRow(0), equalTo("99999999992222233333"));
		
	}
	
	@Test
	public void setValueWhenChangeSpansOverMultipleBlocksAndIsAlignedToBlockStart_middle() {
		classUnderTest = new Bytemap(20, 1);
		classUnderTest.temporaryTestHook(0,
				new int[] { classUnderTest.buildBlock(5, (byte) 0),
				classUnderTest.buildBlock(5, (byte) 1),
				classUnderTest.buildBlock(5, (byte) 2),
				classUnderTest.buildBlock(5, (byte) 3) });
		
		assertThat(classUnderTest.printRow(0), equalTo("00000111112222233333"));
		
		classUnderTest.setValue(5, 0, 10, (byte) 7);
		assertThat(classUnderTest.printRow(0), equalTo("00000777777777733333"));
		
	}
	
	@Test
	public void setValueWhenChangeSpansOverMultipleBlocksAndIsAlignedToBlockStart_last() {
		classUnderTest = new Bytemap(20, 1);
		classUnderTest.temporaryTestHook(0,
				new int[] { classUnderTest.buildBlock(5, (byte) 0),
				classUnderTest.buildBlock(5, (byte) 1),
				classUnderTest.buildBlock(5, (byte) 2),
				classUnderTest.buildBlock(5, (byte) 3) });
		
		assertThat(classUnderTest.printRow(0), equalTo("00000111112222233333"));
		
		classUnderTest.setValue(10, 0, 10, (byte) 7);
		assertThat(classUnderTest.printRow(0), equalTo("00000111117777777777"));
		
	}

	@Test
	public void setValueWhenChangeSpansOverMultipleBlocksAndIsAlignedToBlockStartButEndsInsideEndblock_first() {
		classUnderTest = new Bytemap(10, 1);
		classUnderTest.temporaryTestHook(0,
				new int[] { classUnderTest.buildBlock(5, (byte) 0), classUnderTest.buildBlock(5, (byte) 1) });

		assertThat(classUnderTest.printRow(0), equalTo("0000011111"));

		classUnderTest.setValue(0, 0, 7, (byte) 7);
		assertThat(classUnderTest.printRow(0), equalTo("7777777111"));

	}

	@Test
	public void setValueWhenChangeSpansOverMultipleBlocksAndIsAlignedToBlockStartButEndsInsideEndblock_middle() {
		classUnderTest = new Bytemap(15, 1);
		classUnderTest.temporaryTestHook(0,
				new int[] { classUnderTest.buildBlock(5, (byte) 0), classUnderTest.buildBlock(5, (byte) 1),
						classUnderTest.buildBlock(5, (byte) 2) });

		assertThat(classUnderTest.printRow(0), equalTo("000001111122222"));

		classUnderTest.setValue(5, 0, 7, (byte) 3);
		assertThat(classUnderTest.printRow(0), equalTo("000003333333222"));

	}


}
