package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

public class PullRequestOverViewView extends AuthenticatedView {

	public PullRequestOverViewView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertTrue(currentPathContains("/pull/"));
	}

	/**
	 * Check if the pull request is open.
	 * @return Returns true if the pull request is open.
     */
	public boolean isOpen() {
		return getDriver().findElement(By.id("btn-close")) != null;
	}

	/**
	 * Close the pull request.
	 */
	public void close() {
		getDriver().findElement(By.id("btn-close")).click();
	}

	/**
	 * Merge the pull request.
	 */
	public void merge() {
		getDriver().findElement(By.id("btn-merge")).click();
	}

	/**
	 * Remove the branch.
	 */
	public void removeBranch() {
		getDriver().findElement(By.id("btn-remove-branch")).click();
	}

}
