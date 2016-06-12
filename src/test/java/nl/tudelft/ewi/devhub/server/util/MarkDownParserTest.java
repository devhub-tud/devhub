package nl.tudelft.ewi.devhub.server.util;

import org.junit.Before;
import org.junit.Test;
import org.pegdown.PegDownProcessor;

import static org.junit.Assert.assertEquals;

/**
 * Created by Douwe Koopmans on 3-6-16.
 */
public class MarkDownParserTest {

	private MarkDownParser markDownParser;
	@Before
	public void setup() {
		markDownParser = new MarkDownParser(new PegDownProcessor(2000l));
	}

	@Test
	public void markdownToHtml() throws Exception {
		String testMd = "*Hello World!*";
		String expectedHtml = "<p><em>Hello World!</em></p>";

		assertEquals(expectedHtml, markDownParser.markdownToHtml(testMd));
	}

	@Test
	public void testTimeout() {
		markDownParser = new MarkDownParser(new PegDownProcessor(2000l));
		String testMd = "*Hello World!*";
		String expectedHtml = "<p><em>Hello World!</em></p>";
		assertEquals(expectedHtml, markDownParser.markdownToHtml(testMd));
	}
}