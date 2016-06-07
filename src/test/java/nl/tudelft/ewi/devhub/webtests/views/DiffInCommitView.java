package nl.tudelft.ewi.devhub.webtests.views;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DiffInCommitView extends ProjectInCommitView {

    private static final By INLINE_COMMENT_BUTTON = By.className("btn-comment");
    private static final By INLINE_COMMENT_BOX = By.cssSelector("textarea[name=content]");
    private static final By POST_INLINE_COMMENT_BUTTON = By.cssSelector(".btn-primary");

    public DiffInCommitView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertNotNull(getBreadcrumb());
		assertNotNull(getHeaders());

		assertTrue(currentPathStartsWith("/courses"));
		assertTrue(currentPathContains("/groups"));
		assertTrue(currentPathContains("/commits"));
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
    
    public DiffInCommitView postInlineComment(String comment) {
        invariant();
        WebElement diffLine = listDiffs().get(0)
                .getElement();
        diffLine.findElement(INLINE_COMMENT_BUTTON)
                .click();
        diffLine.findElement(INLINE_COMMENT_BOX)
                .sendKeys(comment);
        diffLine.findElement(By.className("form-horizontal"))
                .findElement(POST_INLINE_COMMENT_BUTTON)
                .click();

        return this;
    }
	
}
