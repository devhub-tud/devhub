package nl.tudelft.ewi.devhub.webtests.views;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import nl.tudelft.ewi.devhub.webtests.utils.Dom;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

public class CourseEditionView extends AuthenticatedView {
	
	private static final By TABLE_XPATH = By.xpath("//table");

	public CourseEditionView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();

		WebElement groupTable = getDriver().findElements(TABLE_XPATH).get(0);
		assertEquals(Dom.prevSibling(groupTable).getText(), "Groups");

		WebElement Assignmenttable = getAssignmentTable();
		assertEquals(Dom.prevSibling(Assignmenttable).getText(), "Assignments");
	}
	private WebElement getAssignmentTable() {
		return getDriver().findElements(TABLE_XPATH).get(1);
	}
	
	/**
	 * @return A list of visible groups
	 */
	public List<Group> listGroups() {
		invariant();
		WebElement myGroupTable = getDriver().findElements(TABLE_XPATH).get(0);
		return listGroupsInTable(myGroupTable);
	}
	
	/**
	 * @return A list of visible assignments
	 */
	public List<Assignment> listAssignments() {
		invariant();
		WebElement assignmentTable = getAssignmentTable();
		return listAssignmentsInTable(assignmentTable);
	}
	
	/**
	 * Adds an assignment
	 * @param dueDate		deadline of the assignment
	 * @param name			name of the assignment
	 * @param description	a summary of the assignment
	 */
	public void addAssignment(Date dueDate, String name, String description) {
		invariant();
		WebElement assignmentTable = getAssignmentTable();
		assignmentTable.findElement(By.xpath("..")).findElement(By.className("btn")).click();
		
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");

		getDriver().findElement(By.id("due-date")).sendKeys(ft.format(dueDate));		
		getDriver().findElement(By.id("name")).sendKeys(name);
		getDriver().findElement(By.id("summary")).sendKeys(description);

		getDriver().findElement(By.className("col-sm-offset-2")).findElement(By.className("btn-primary")).click();
		
		invariant();
	}
	
	private List<Group> listGroupsInTable(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("td"));
		if (entries.size() == 1) {
			if (!Dom.isVisible(entries.get(0), By.tagName("a"))) {
				return Lists.newArrayList();
			}
		}

		List<Group> groups = Lists.newArrayList();
		for (WebElement entry : entries) {
			WebElement projectLink = entry.findElement(By.tagName("a"));
			Group project = new Group(projectLink.getText(), projectLink);
			groups.add(project);
		}
		return groups;
	}

	private List<Assignment> listAssignmentsInTable(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("tr"));
		if (entries.size() == 1) {
			if (!Dom.isVisible(entries.get(0), By.tagName("a"))) {
				return Lists.newArrayList();
			}
		}

		SimpleDateFormat ft = new SimpleDateFormat("EE, dd LL yyyy HH:mm");
		
		List<Assignment> assignments = Lists.newArrayList();
		for (WebElement entry : entries) {
			List<WebElement> assignmentLinks = entry.findElements(By.tagName("td"));
			if(assignmentLinks.isEmpty())
				continue;
			Date dueDate;
			try {
				dueDate = ft.parse(assignmentLinks.get(2).getText());
			} catch (ParseException e) {
				dueDate = new Date(0);
			}

			Assignment assignment = new Assignment(
					Integer.parseInt(assignmentLinks.get(0).getText()), 
					assignmentLinks.get(1).getText(),
					dueDate,
					assignmentLinks.get(2).findElement(By.className("btn")));

			assignments.add(assignment);
		}
		return assignments;
	}
	
	@Data
	public class Group {
		private final String name;

		@Getter(AccessLevel.NONE)
		private final WebElement anchor;

		public CommitsView click() {
			anchor.click();
			return new CommitsView(getDriver());
		}
	}

	@Data
	public class Assignment {
		private final int number;
		private final String name;
		private final Date dueDate;

		@Getter(AccessLevel.NONE)
		private final WebElement anchor;

		public AuthenticatedView click() {
			anchor.click();
			return new AuthenticatedView(getDriver());
		}

		/**
		 * Shortcut method to edit an assignment.
		 *
		 * @param dueDate    	deadline 	(leave null for no change)
		 * @param name			name		(leave null for no change)
		 * @param description	description (leave null for no change)
		 */
		public CourseEditionView edit(Date dueDate, String name, String description) {
			click();

			if(dueDate != null)
			{
				SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
				WebElement dateEle = getDriver().findElement(By.id("due-date"));
				dateEle.clear();
				dateEle.sendKeys(ft.format(dueDate));
			}
			if(name != null)
			{
				WebElement nameEle = getDriver().findElement(By.id("name"));
				nameEle.clear();
				nameEle.sendKeys(name);
			}
			if(description != null)
			{
				WebElement descEle = getDriver().findElement(By.id("summary"));
				descEle.clear();
				descEle.sendKeys(description);
			}

			getDriver().findElement(By.className("col-sm-offset-2")).findElement(By.className("btn-primary")).click();

			invariant();
			return CourseEditionView.this;
		}

	}

}

