package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AuthenticatedView extends View {

	private static final By COURSES_VIEW = By.xpath("//div[@class='container']//a[text()='CourseEditions']");
	
	public AuthenticatedView(WebDriver driver) {
		super(driver);
	}
	
	public CoursesView toCoursesView() {
		getDriver().findElement(COURSES_VIEW).click();
		return new CoursesView(getDriver());
	}
	
}
