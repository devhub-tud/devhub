package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.*;

import java.util.List;

import lombok.Data;
import nl.tudelft.ewi.git.models.DiffContext;
import nl.tudelft.ewi.git.models.DiffLine;
import nl.tudelft.ewi.git.models.DiffModel;
import nl.tudelft.ewi.git.models.DiffModel.Type;

import org.apache.directory.api.util.Strings;
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
		assertTrue(currentPathStartsWith("/projects"));
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
		
		private final DiffModel diffModel;
		
		public void assertEqualTo(DiffModel expected) {
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
			
			assertEquals(expected.getDiffContexts(), diffModel.getDiffContexts());
		}
		
		/**
		 * Build a {@link DiffElement} from a {@link WebElement} in the {@link DiffView}
		 * @param element the {@link WebElement} to be converted into a {@link DiffElement}
		 * @return the created {@link DiffElement}
		 */
		public static DiffElement build(WebElement element) {
			final DiffModel model = new DiffModel();
			
			
			WebElement header = element.findElement(By.tagName("h5"));
			String typeStr = header.findElement(By.tagName("span")).getText();
			Type type = getTypeFor(typeStr);
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
			
			model.setDiffContexts(getDiffContextsFor(element.findElements(By.tagName("tbody"))));
			return new DiffElement(element, model);
		}
		
		private static List<DiffContext> getDiffContextsFor(List<WebElement> tableBodies) {
			List<DiffContext> result = Lists.<DiffContext> newArrayList();

			for(WebElement tbody : tableBodies) {
				DiffContext context = new DiffContext();
				List<WebElement> rows = tbody.findElements(By.tagName("tr"));
				assertTrue("Rows should not be empty", !rows.isEmpty());
				List<DiffLine> diffLines = Lists.<DiffLine> newArrayList();
				context.setDiffLines(diffLines);

				for(WebElement tr : rows) {
					List<WebElement> columns = tr.findElements(By.tagName("td"));
					assertEquals("Expected three columns per row", 3, columns.size());

					String oldLineNumber = columns.get(0).getText();
					String newLineNumber = columns.get(1).getText();
					String content = columns.get(2).getText();
					DiffLine.Type type = getModifierFor(columns.get(0));
					diffLines.add(new DiffLine(type, content));

					if(Strings.isNotEmpty(oldLineNumber)) {
						int value = Integer.parseInt(oldLineNumber);
						if(context.getOldStart() == null)
							context.setOldStart(value);
						context.setOldEnd(value);
					}
					
					if(Strings.isNotEmpty(newLineNumber)) {
						int value = Integer.parseInt(newLineNumber);
						if(context.getNewStart() == null)
							context.setNewStart(Integer.parseInt(newLineNumber));
						context.setNewEnd(value);
					}
				}

				result.add(context);
			}
			return result;
		}
		
		private static DiffLine.Type getModifierFor(final WebElement column) {
			String styles = column.getAttribute("class");
			
			if(styles.contains("add")) {
				return DiffLine.Type.ADDED;
			}
			else if (styles.contains("delete")) {
				return DiffLine.Type.REMOVED;
			}
			else {
				return DiffLine.Type.CONTEXT;
			}
		}
		
		private static DiffModel.Type getTypeFor(final String value) {
			if(value.equalsIgnoreCase("Created")) {
				return DiffModel.Type.ADD;
			}
			else if (value.equalsIgnoreCase("Copied")) {
				return DiffModel.Type.COPY;
			}
			else if (value.equalsIgnoreCase("Deleted")) {
				return DiffModel.Type.DELETE;
			}
			else if (value.equalsIgnoreCase("Modified")) {
				return DiffModel.Type.MODIFY;
			}
			else if (value.equalsIgnoreCase("Moved")) {
				return DiffModel.Type.RENAME;
			}
			throw new IllegalArgumentException("Unkown type " + value);
		}
		
	}
	
}
