package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import lombok.Data;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.EntryType;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class FolderView extends View {

	private static final By HEADERS = By.xpath("//span[@class='headers']");
	private static final By DROPDOWN_CARET = By.xpath("//button[contains(@class,'dropdown-toggle')]");
	private static final By MESSAGE_HEADER = By.xpath(".//h2[@class='header']");
	private static final By AUTHOR_SUB_HEADER = By.xpath(".//h5[@class='subheader']");
	private static final By TABLE_FILES = By.xpath(".//table[@class='table files']"); 
	
	private final CommitModel commit;
	
	public FolderView(WebDriver driver, CommitModel commit) {
		super(driver);
		this.commit = commit;
		assertInvariant();
	}
	
	private void assertInvariant() {
		assertTrue(currentPathStartsWith("/projects"));
		WebElement headers = getDriver().findElement(HEADERS);
		
		assertNotNull(headers);
		assertNotNull(getDriver().findElement(DROPDOWN_CARET));
		assertNotNull(commit);
		
		assertEquals(commit.getAuthor(), headers.findElement(AUTHOR_SUB_HEADER).getText());
		assertEquals(commit.getMessage(), headers.findElement(MESSAGE_HEADER).getText());
	}
	
	/**
	 * @return get the folder path
	 */
	public String getPath() {
		return getDriver().findElement(By.cssSelector("div.header > h5")).getText().replace(" /", "/").replace("/ ", "/");
	}
	
	/**
	 * @return A {@code Map} containing the entry names and types
	 */
	public Map<String, EntryType> getDirectoryEntries() {
		ImmutableMap.Builder<String, EntryType> builder = ImmutableMap.builder();
		for(DirectoryElement entry : getDirectoryElements()) {
			builder.put(entry.getName(), entry.getType());
		}
		return builder.build();
	}
	
	/**
	 * @return A {@code List} containing the {@link DirectoryElement}s 
	 */
	public List<DirectoryElement> getDirectoryElements() {
		ImmutableList.Builder<DirectoryElement> result = ImmutableList.builder();
		for(WebElement element : getDriver().findElement(TABLE_FILES).findElements(By.tagName("td"))) {
			DirectoryElement entry = new DirectoryElement(element);
			result.add(entry);
		}
		return result.build();
	}
	
	@Data
	public class DirectoryElement {
		
		private final WebElement element;
		private final EntryType type;
		private final String name;
		
		public DirectoryElement(WebElement rowElement) {
			this.element = rowElement.findElement(By.tagName("a"));
			this.name = rowElement.getText();
			String iconStyles = rowElement.findElement(By.tagName("i")).getAttribute("class");
			this.type = iconStyles.contains("glyphicon-folder-open") ? EntryType.FOLDER
					: iconStyles.contains("glyphicon-file") ? EntryType.TEXT
							: EntryType.BINARY;
		}
		
		public TextFileView click() {
			element.click();
			return new TextFileView(getDriver(), commit);
		}
		
	}

}
