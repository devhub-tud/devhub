package nl.tudelft.ewi.devhub.webtests.views;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.assertTrue;

@Slf4j
public class PullRequestOverViewView extends PullRequestView {

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
		try {
			WebElement closeButton = getDriver().findElement(By.id("btn-close"));
			return closeButton.isEnabled();
		} catch (NoSuchElementException e) {
			log.warn(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Check if the pull request is closed.
	 * @return Returns true if the pull request is open.
     */
	public boolean isClosed() {
		try {
			WebElement closeButton = getDriver().findElement(By.id("btn-close"));
			return !closeButton.isEnabled();
		} catch (NoSuchElementException e) {
			log.warn(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Check if the pull request is closed.
	 * @return Returns true if the pull request is open.
     */
	public boolean isMerged() {
		try {
			WebElement mergeButton = getDriver().findElement(By.id("btn-merge"));
			return !mergeButton.isEnabled();
		} catch (NoSuchElementException e) {
			log.warn(e.getMessage(), e);
			return false;
		}
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
