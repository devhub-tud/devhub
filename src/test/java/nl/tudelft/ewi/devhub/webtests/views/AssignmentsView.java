package nl.tudelft.ewi.devhub.webtests.views;

import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

import static org.junit.Assert.*;

public class AssignmentsView extends ProjectSidebarView {

	private static final By RECENT_ASSIGNMENTS_HEADER = By.xpath("//h4[starts-with(normalize-space(.), 'Assignments')]");
	
	public AssignmentsView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertNotNull(getDriver().findElement(RECENT_ASSIGNMENTS_HEADER));
	}

	/**
	 * @return A list of assignments.
	 */
	public List<Assignment> listAssignments() {
		invariant();
		WebElement table = getDriver().findElement(By.className("table-bordered"));
		return listAssignments(table);
	}
	
	private List<Assignment> listAssignments(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("tr"));
		List<Assignment> assignments = Lists.newArrayList();

		boolean first = true;
		
		for (WebElement entry : entries) {
			if(!first){
				WebElement diffLink = entry.findElement(By.tagName("a"));
				List<WebElement> entries2 = entry.findElements(By.tagName("td"));
			
				String number = "number", name = "name";
				int i=0;
				for(WebElement entry2 : entries2){
					if(i==0)
						number = entry2.getText();
					if(i==1)
						name = entry2.getText();
					i++;
				}
			
				assignments.add(new Assignment(number, name, diffLink));
			}
			first = false;
		}
		
		return assignments;
		
	}
	
	@Data
	public class Assignment {
		
		private final String number, name;
		@Getter(AccessLevel.NONE)
		private final WebElement anchor;
		
		public DiffInCommitView click() {
			anchor.click();
			return new DiffInCommitView(getDriver());
		}
	}
	
	
}
