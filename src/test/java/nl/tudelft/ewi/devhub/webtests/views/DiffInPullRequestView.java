package nl.tudelft.ewi.devhub.webtests.views;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

public class DiffInPullRequestView extends PullRequestView {

	public DiffInPullRequestView(WebDriver driver) {
		super(driver);
	}

	public List<DiffElement> listDiffs() {
		invariant();
		WebElement container = getDriver().findElement(By.className("container"));
		List<WebElement> elements = container.findElements(By.xpath("//div[@class='diff box']"));
		return Lists.transform(elements, DiffElement::build);
	}

}
