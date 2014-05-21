package nl.tudelft.ewi.devhub.web;

import java.util.List;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class View {

	private final WebDriver driver;

	View(WebDriver driver) {
		this.driver = driver;
	}

	/**
	 * Terminates the browser.
	 */
	public void terminateBrowser() {
		driver.close();
	}

	protected WebDriver getDriver() {
		return driver;
	}

	/**
	 * @return The current URL of the browser.
	 */
	public String getCurrentUrl() {
		return driver.getCurrentUrl();
	}

	/**
	 * This method verifies that the specified <code>path</code> matches the path
	 * described by the current URL.
	 * 
	 * @param path
	 *            The path to compare to the current URL.
	 * @return True of the paths match, or false otherwise.
	 */
	public boolean currentPathEquals(String path) {
		for (int i = 0; i < 100; i++) {
			String currentUrl = driver.getCurrentUrl();
			if (currentUrl.endsWith(path) || currentUrl.contains(path + "?")) {
				return true;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.warn(e.getMessage(), e);
			}
		}
		return false;

	}

	/**
	 * This method blocks (for at most 10 seconds) until the current URL changes to the
	 * specified <code>url</code>. If they do not match exactly this method will throw an
	 * {@link IllegalStateException} after 10 seconds.
	 * 
	 * @param url
	 *            The URL to compare to the current URL. If they match this method will
	 *            automatically return.
	 */
	public void currentUrlEquals(String url) {
		for (int i = 0; i < 100; i++) {
			String currentUrl = driver.getCurrentUrl();
			if (currentUrl.equals(url)) {
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.warn(e.getMessage(), e);
			}
		}
		throw new IllegalStateException("Current URL did not match after 10 seconds: " + url);
	}

	/**
	 * This method blocks (for at most 10 seconds) until the current URL changes to
	 * something else than the specified <code>url</code>. If they remain an exact match
	 * this method will throw an {@link IllegalStateException} after 10 seconds.
	 * 
	 * @param url
	 *            The URL to compare to the current URL. If they mismatch this method will
	 *            automatically return.
	 */
	public void waitUntilCurrentUrlDiffersFrom(String url) {
		for (int i = 0; i < 100; i++) {
			if (!url.equals(driver.getCurrentUrl())) {
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.warn(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
		throw new IllegalStateException("Current URL did not mismatch after 10 seconds: " + url);
	}

	/**
	 * @return A {@link List} of all currently displayed {@link Alert}s on the current
	 *         page.
	 */
	public List<Alert> listAlerts() {
		List<Alert> alerts = Lists.newArrayList();
		List<WebElement> elements = driver.findElements(By.xpath("//div[contains(concat(' ', @class, ' '), ' alert ')]"));
		for (WebElement element : elements) {
			String classAttribute = element.getAttribute("class");
			String[] cssClasses = classAttribute.split(" ");
			alerts.add(new Alert(element.getText(), Alert.Type.deriveType(cssClasses)));
		}
		return alerts;
	}

	@Data
	public static class Alert {

		public static enum Type {
			ERROR("alert-danger"), WARNING("alert-warning"), INFO("alert-info"), SUCCESS("alert-success");

			@Getter
			private final String className;

			private Type(String className) {
				this.className = className;
			}

			public static Type deriveType(String... classNames) {
				for (String className : classNames) {
					for (Type type : values()) {
						if (type.getClassName().equals(className)) {
							return type;
						}
					}
				}
				return null;
			}
		}

		private final String message;
		private final Type type;
	}

}
