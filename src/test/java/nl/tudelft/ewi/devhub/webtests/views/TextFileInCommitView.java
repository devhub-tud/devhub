package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class TextFileInCommitView extends ProjectInCommitView {

	private static final By TABLE_DIFFS = By.xpath(".//table[@class='table diffs']");

	public TextFileInCommitView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertNotNull(getTableDiffs());
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

	protected WebElement getTableDiffs() {
		return getDriver().findElement(TABLE_DIFFS);
	}

	/**
	 * @return the contents of this text file
	 */
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		WebElement tableDiffs = getTableDiffs();
		List<WebElement> lines = tableDiffs.findElements(By.tagName("pre"));
		sb.ensureCapacity(lines.size() * 10);

		boolean newLine = false;

		for (WebElement line : lines) {
			if (newLine)
				sb.append('\n');
			sb.append(line.getText());
			newLine = true;
		}

		return sb.toString();
	}

}
