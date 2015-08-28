package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TextFileView extends View {
	
	private static final By HEADERS = By.xpath("//span[@class='headers']");
	private static final By DROPDOWN_CARET = By.xpath("//button[contains(@class,'dropdown-toggle')]");
	private static final By MESSAGE_HEADER = By.xpath(".//h2[@class='header']");
	private static final By AUTHOR_SUB_HEADER = By.xpath(".//h5[@class='subheader']");
	private static final By TABLE_DIFFS = By.xpath(".//table[@class='table diffs']");

	private final WebElement headers;
	
	public TextFileView(WebDriver driver) {
		super(driver);
		this.headers = getDriver().findElement(HEADERS);
		assertInvariant();
	}
	
	private void assertInvariant() {
		assertTrue(currentPathStartsWith("/courses"));
		WebElement headers = getDriver().findElement(HEADERS);
		
		assertNotNull(headers);
		assertNotNull(getDriver().findElement(DROPDOWN_CARET));
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		String path = getPath();
		return path.substring(path.lastIndexOf("/") + 1);
	}
	
	/**
	 * @return the path
	 */
	public String getPath() {
		return getDriver().findElement(By.cssSelector("div.header > h5")).getText().replace(" /", "/").replace("/ ", "/");
	}
	
	/**
	 * @return the text content of the author header
	 */
	public String getAuthorHeader() {
		return headers.findElement(AUTHOR_SUB_HEADER).getText();
	}
	
	/**
	 * @return the text content of the message header
	 */
	public String getMessageHeader() {
		return headers.findElement(MESSAGE_HEADER).getText();
	}
	
	/**
	 * @return the contents of this text file
	 */
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		WebElement tableDiffs = getDriver().findElement(TABLE_DIFFS);
		List<WebElement> lines = tableDiffs.findElements(By.tagName("pre"));
		sb.ensureCapacity(lines.size() * 10);
		
		boolean newLine = false;
		
		for (WebElement line : lines) {
			if(newLine)
				sb.append('\n');
			sb.append(line.getText());
			newLine = true;
		}
		
		return sb.toString();
	}
}
