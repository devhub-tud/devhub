package nl.tudelft.ewi.devhub.server.util;

import org.jukito.JukitoRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Created by Douwe Koopmans on 3-6-16.
 */
@RunWith(JukitoRunner.class)
public class MarkDownParserTest {

	@Inject
	private MarkDownParser markDownParser;

	@Test
	public void testSimpleMarkdownToHtml() throws Exception {
		String testMd = "*Hello World!*";
		String expectedHtml = "<p><em>Hello World!</em></p>";

		assertEquals(expectedHtml, markDownParser.markdownToHtml(testMd));
	}

	@Test
	public void testNoMarkdown() throws Exception {
		String testString = "foo bar";
		String expectedHtml = "<p>foo bar</p>";

		assertEquals(expectedHtml, markDownParser.markdownToHtml(testString));
	}

	@Test
	public void testXssAttempt() {
		String script = "alert(\"Hello World!\")";
		String expectedHtml = "<p>&lt;script&gt;alert(&quot;Hello World!&quot;)&lt;/script&gt;</p>";

		assertEquals(expectedHtml, markDownParser.markdownToHtml("<script>" + script + "</script>"));

		script = "alert(\\\"Hello World!\\\")";
		expectedHtml = "<p>&amp;lt;script&amp;gt;alert(&amp;quot;Hello World!&amp;quot;)&amp;lt;/script&amp;gt;</p>";
		assertEquals(expectedHtml, markDownParser.markdownToHtml("\\<script\\>" + script + "\\</script\\>"));
	}

	@Ignore
	@Test
	public void testCodeblock() {
		String codeBlock =
				"```" + "\n" +
				"System.out.println(\"Hello World!\");" + "\n" +
				"```";
		String expectedHtml =
				"<p><code>\n" +
				"System.out.println(\"Hello World!\");\n" +
				"</code></p>";

		assertEquals(expectedHtml, markDownParser.markdownToHtml(codeBlock));
	}
}