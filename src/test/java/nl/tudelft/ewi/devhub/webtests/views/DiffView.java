package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.*;

import java.util.List;

import com.google.common.base.Strings;
import lombok.Data;

import nl.tudelft.ewi.git.models.ChangeType;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.DiffModel.DiffContext;
import nl.tudelft.ewi.git.models.DiffModel.DiffLine;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class DiffView extends View {
	
	private static final By HEADERS = By.xpath("//span[@class='headers']");
	private static final By DROPDOWN_CARET = By.xpath("//button[contains(@class,'dropdown-toggle')]");
	private static final By MESSAGE_HEADER = By.xpath(".//h2[@class='header']");
	private static final By AUTHOR_SUB_HEADER = By.xpath(".//h5[@class='subheader']");
	private static final By VIEW_FILES_BUTTON = By.xpath("//a[starts-with(normalize-space(.), 'View files')]");

	private final WebElement headers;
	
	public DiffView(WebDriver driver) {
		super(driver);
		this.headers = getDriver().findElement(HEADERS);
		assertInvariant();
	}

	private void assertInvariant() {
		assertTrue(currentPathStartsWith("/courses"));
		assertNotNull(headers);
		assertNotNull(getDriver().findElement(DROPDOWN_CARET));
	}
	
	public FolderView viewFiles() {
		assertInvariant();

		WebElement container = getDriver().findElement(By.className("container"));
		WebElement dropdownCaret = container.findElement(DROPDOWN_CARET);
		dropdownCaret.click();
		WebElement viewFilesButton = container.findElement(VIEW_FILES_BUTTON);
		viewFilesButton.click();

		return new FolderView(getDriver());
	}

	/**
	 * @return the {@link DiffElement DiffElements} in this {@code DiffView}
	 */
	public List<DiffElement> listDiffs() {
		assertInvariant();
		WebElement container = getDriver().findElement(By.className("container"));
		return listDiffs(container);
	}

	private List<DiffElement> listDiffs(WebElement container) {
		List<WebElement> elements = container.findElements(By.xpath("//div[@class='diff box']"));

		return Lists.transform(elements, new Function<WebElement, DiffElement>() {

			@Override
			public DiffElement apply(WebElement element) {
				return DiffElement.build(element);
			}

		});
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

	@Data
	public static class DiffElement {

		private final WebElement element;

		private final DiffBlameModel.DiffBlameFile diffModel;

		public void assertEqualTo(DiffBlameModel.DiffBlameFile expected) {
			assertEquals(expected.getType(), diffModel.getType());

			switch(diffModel.getType()){
			case DELETE:
				assertEquals(expected.getOldPath(), diffModel.getOldPath());
				break;
			case ADD:
			case MODIFY:
				assertEquals(expected.getNewPath(), diffModel.getNewPath());
				break;
			default:
				assertEquals(expected.getOldPath(), diffModel.getOldPath());
				assertEquals(expected.getNewPath(), diffModel.getNewPath());
				break;

			}

			assertEquals(expected.getContexts(), diffModel.getContexts());
		}

		/**
		 * Build a {@link DiffElement} from a {@link WebElement} in the {@link DiffView}
		 * @param element the {@link WebElement} to be converted into a {@link DiffElement}
		 * @return the created {@link DiffElement}
		 */
		public static DiffElement build(WebElement element) {
			final DiffBlameModel.DiffBlameFile model = new DiffBlameModel.DiffBlameFile();


			WebElement header = element.findElement(By.tagName("h5"));
			String typeStr = header.findElement(By.tagName("span")).getText();
			ChangeType type = getTypeFor(typeStr);
			model.setType(type);

			String headerText = header.getText();
			headerText = headerText.substring(headerText.indexOf(" ") + 1);

			switch (type) {
				case ADD:
				case MODIFY:
					model.setNewPath(headerText);
					break;
				case DELETE:
					model.setOldPath(headerText);
					break;
				default:
					String[] split = headerText.split(" -> ");
					model.setOldPath(split[0]);
					model.setNewPath(split[1]);
					break;
			}

			model.setContexts(getDiffContextsFor(element.findElements(By.tagName("tbody"))));
			return new DiffElement(element, model);
		}

		private static List<DiffBlameModel.DiffBlameContext> getDiffContextsFor(List<WebElement> tableBodies) {
			List<DiffBlameModel.DiffBlameContext> result = Lists.<DiffBlameModel.DiffBlameContext> newArrayList();

			for(WebElement tbody : tableBodies) {
				DiffBlameModel.DiffBlameContext context = new DiffBlameModel.DiffBlameContext();
				List<WebElement> rows = tbody.findElements(By.tagName("tr"));
				assertTrue("Rows should not be empty", !rows.isEmpty());
				List<DiffBlameModel.DiffBlameLine> diffLines = Lists.<DiffBlameModel.DiffBlameLine> newArrayList();
				context.setLines(diffLines);

				for(WebElement tr : rows) {
					List<WebElement> columns = tr.findElements(By.tagName("td"));
					assertEquals("Expected three columns per row", 3, columns.size());

					String oldLineNumberStr = columns.get(0).getText();
					String newLineNumberStr = columns.get(1).getText();
					Integer oldLineNumber = Strings.isNullOrEmpty(oldLineNumberStr) ? null : Integer.parseInt(oldLineNumberStr);
					Integer newLineNumber = Strings.isNullOrEmpty(newLineNumberStr) ? null : Integer.parseInt(newLineNumberStr);

					String sourceCommitId = tr.getAttribute("data-source-commit");
					Integer sourceLineNumber = Integer.parseInt(tr.getAttribute("data-source-line-number"));
					String sourcePath = tr.getAttribute("data-source-file-name");
					String content = columns.get(2).getText();
					DiffBlameModel.DiffBlameLine line = new DiffBlameModel.DiffBlameLine();

					line.setSourceCommitId(sourceCommitId);
					line.setSourceFilePath(sourcePath);
					line.setSourceLineNumber(sourceLineNumber);
					line.setOldLineNumber(oldLineNumber);
					line.setNewLineNumber(newLineNumber);
					line.setContent(content);
					diffLines.add(line);
				}

				result.add(context);
			}
			return result;
		}

		private static ChangeType getTypeFor(final String value) {
			if(value.equalsIgnoreCase("Created")) {
				return ChangeType.ADD;
			}
			else if (value.equalsIgnoreCase("Copied")) {
				return ChangeType.COPY;
			}
			else if (value.equalsIgnoreCase("Deleted")) {
				return ChangeType.DELETE;
			}
			else if (value.equalsIgnoreCase("Modified")) {
				return ChangeType.MODIFY;
			}
			else if (value.equalsIgnoreCase("Moved")) {
				return ChangeType.RENAME;
			}
			throw new IllegalArgumentException("Unknown type " + value);
		}

	}
	
}
