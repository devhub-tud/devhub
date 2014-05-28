package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import nl.tudelft.ewi.git.models.DiffModel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

public class DiffView extends View {
	
	private static final By HEADERS = By.xpath("//span[@class='headers']");

	public DiffView(WebDriver driver) {
		super(driver);
		assertInvariant();
	}

	private void assertInvariant() {
		assertTrue(currentPathStartsWith("/projects"));
		assertNotNull(getDriver().findElement(HEADERS));
	}
	
	public List<DiffElement> listDiffs() {
		assertInvariant();
		WebElement container = getDriver().findElement(By.className("container"));
		return listDiffs(container);
	}
	
	private List<DiffElement> listDiffs(WebElement container) {
		List<WebElement> elements = container.findElements(By.xpath("//div[@class='diff box']"));
		List<DiffElement> diffElements = Lists.newArrayList();
		
		for(WebElement element : elements) {
			diffElements.add(DiffElement.build(element));
		}
		
		return diffElements;
	}

	@Data
	@ToString(callSuper=true)
	@EqualsAndHashCode(callSuper=true)
	public static class DiffElement extends DiffModel {
		
		private final WebElement element;
		
		public boolean fold() {
			return true;
		}
		
		/**
		 * Build a {@link DiffElement} from a {@link WebElement} in the {@link DiffView}
		 * @param element the {@link WebElement} to be converted into a {@link DiffElement}
		 * @return the created {@link DiffElement}
		 */
		public static DiffElement build(WebElement element) {
			DiffElement result = new DiffElement(element);
			
			WebElement header = element.findElement(By.tagName("h5"));
			String typeStr = header.findElement(By.tagName("span")).getText();
			Type type = getTypeFor(typeStr);
			result.setType(type);
			
			String headerText = header.getText();
			headerText = headerText.substring(headerText.indexOf(" ") + 1);
			
			switch (type) {
			case ADD:
			case MODIFY:
				result.setNewPath(headerText);
				break;
			case DELETE:
				result.setOldPath(headerText);
				break;
			default:
				String[] split = headerText.split(" -> ");
				result.setOldPath(split[0]);
				result.setNewPath(split[1]);
				break;
			}
			
//			List<WebElement> lines = element.findElements(By.xpath("//div[@class='code']"));
//			String[] raw = new String[lines.size()];
//			int i = 0;
//			for(WebElement line : lines) {
//				raw[i++] = line.getText();
//			}
//			result.setRaw(raw);
			
			return result;
		}
		
		private static Type getTypeFor(String value) {
			if(value.equalsIgnoreCase("Created")) {
				return Type.ADD;
			} else if (value.equalsIgnoreCase("Copied")) {
				return Type.COPY;
			} else if (value.equalsIgnoreCase("Deleted")) {
				return Type.DELETE;
			} else if (value.equalsIgnoreCase("Modified")) {
				return Type.MODIFY;
			} else if (value.equalsIgnoreCase("Moved")) {
				return Type.RENAME;
			}
			return null;
		}
		
	}
	
}
