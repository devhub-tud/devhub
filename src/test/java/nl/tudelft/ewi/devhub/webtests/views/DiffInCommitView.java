package nl.tudelft.ewi.devhub.webtests.views;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Data;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffLine;
import nl.tudelft.ewi.git.models.ChangeType;
import nl.tudelft.ewi.git.models.DiffBlameModel;
import nl.tudelft.ewi.git.models.DiffBlameModel.DiffBlameLine;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiffInCommitView extends ProjectInCommitView {

	public DiffInCommitView(WebDriver driver) {
		super(driver);
	}

	/**
	 * @return the {@link DiffElement DiffElements} in this {@code DiffView}
	 */
	public List<DiffElement> listDiffs() {
		invariant();
		WebElement container = getDriver().findElement(By.className("container"));
		List<WebElement> elements = container.findElements(By.xpath("//div[@class='diff box']"));
		return Lists.transform(elements, DiffElement::build);
	}

	@Data
	public static class DiffElement {

		private final WebElement element;

		private final DiffFile<DiffContext<DiffBlameLine>> diffModel;

		public void assertEqualTo(DiffFile<? extends DiffContext<? extends DiffBlameModel.DiffBlameLine>> expected) {
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

			assertEquals(expected.getContexts().size(), diffModel.getContexts().size());
			for (int i = 0, l = expected.getContexts().size(); i < l; i++) {
				DiffContext<DiffBlameLine> actualContext = diffModel.getContexts().get(i);
				DiffContext<? extends DiffBlameLine> expectedContext = expected.getContexts().get(i);
				assertEquals(expectedContext.getLines().size(), actualContext.getLines().size());

				for (int j = 0, m = expectedContext.getLines().size(); i < m; i++) {
					DiffLine expectedLine = expectedContext.getLines().get(j);
					DiffLine actualLine = actualContext.getLines().get(j);

					assertEquals(expectedLine.getContent(), actualLine.getContent());
					assertEquals(expectedLine.getOldLineNumber(), actualLine.getOldLineNumber());
					assertEquals(expectedLine.getNewLineNumber(), actualLine.getNewLineNumber());
				}
			}
		}

		/**
		 * Build a {@link DiffElement} from a {@link WebElement} in the {@link DiffInCommitView}
		 * @param element the {@link WebElement} to be converted into a {@link DiffElement}
		 * @return the created {@link DiffElement}
		 */
		public static DiffElement build(WebElement element) {
			final DiffFile<DiffContext<DiffBlameModel.DiffBlameLine>> model = new DiffFile<>();


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

		private static List<DiffContext<DiffBlameModel.DiffBlameLine>> getDiffContextsFor(List<WebElement> tableBodies) {
			List<DiffContext<DiffBlameModel.DiffBlameLine>> result = Lists.newArrayList();

			for(WebElement tbody : tableBodies) {
				DiffContext<DiffBlameModel.DiffBlameLine> context = new DiffContext<>();
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
