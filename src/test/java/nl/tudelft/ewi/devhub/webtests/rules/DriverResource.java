package nl.tudelft.ewi.devhub.webtests.rules;

import org.junit.rules.ExternalResource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class DriverResource extends ExternalResource {

	private final AtomicReference<WebDriver> driverRef;

	public DriverResource() {
		this.driverRef = new AtomicReference<>();
	}

	@Override
	protected void before() throws Throwable {
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driverRef.set(driver);
	}

	@Override
	protected void after() {
		getDriver().close();
	}

	public WebDriver getDriver() {
		return driverRef.get();
	}
}
