package nl.tudelft.ewi.devhub.server.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Douwe Koopmans on 3-6-16.
 */
public class MarkDownParserTest {
	private String testMd;
	private String expectedHtml;

	@Before
	public void setup() {
		testMd = "*Hello World!*";
	}

	@After
	public void tearDown() {
		testMd = null;
		expectedHtml = null;
		MarkDownParser.setProcessorTimeout(2000L);
	}

	@Test
	public void markdownToHtml() throws Exception {
		expectedHtml = "<p><em>Hello World!</em></p>";

		assertEquals(expectedHtml, MarkDownParser.markdownToHtml(testMd));
	}

	@Test
	public void testTimeout() {
		MarkDownParser.setProcessorTimeout(2);
		expectedHtml = testMd;

		assertEquals(expectedHtml, MarkDownParser.markdownToHtml(testMd));
	}
}