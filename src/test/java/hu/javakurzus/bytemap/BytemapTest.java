package hu.javakurzus.bytemap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

public class BytemapTest {

	private Bytemap classUnderTest;

	@Before
	public void setup() {
		classUnderTest = new Bytemap(100, 100);
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

		assertThat(
				segment1
						+ segment2,
				equalTo(16777216));

		assertThat(classUnderTest.parseValue(line[0]), equalTo((byte) 1));
		assertThat(classUnderTest.parseValue(line[1]), equalTo((byte) 1));

	}

}
