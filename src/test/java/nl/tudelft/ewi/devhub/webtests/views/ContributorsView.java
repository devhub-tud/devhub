package nl.tudelft.ewi.devhub.webtests.views;

import java.util.List;

import lombok.Data;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertNotNull;

public class ContributorsView extends ProjectSidebarView {

private static final By CONTRIBUTORS_HEADER = By.xpath("//h4[starts-with(normalize-space(.), 'Contributors')]");
	
	public ContributorsView(WebDriver driver) {
		super(driver);
	}

	@Override
	protected void invariant() {
		super.invariant();
		assertNotNull(getDriver().findElement(CONTRIBUTORS_HEADER));
	}

	public List<Contributor> listContributors() {
		invariant();
		WebElement table = getDriver().findElement(By.className("table-bordered"));
		return listContributors(table);
	}
	
	private List<Contributor> listContributors(WebElement table) {
		List<WebElement> entries = table.findElements(By.tagName("tr"));
		List<Contributor> contributors = Lists.newArrayList();

		boolean first = true;
		
		for (WebElement entry : entries) {
			if(!first){
				List<WebElement> entries2 = entry.findElements(By.tagName("td"));
			
				String netID = "netID", name = "name", email = "email";
				int i=0;
				for(WebElement entry2 : entries2){
					if(i==0)
						netID = entry2.getText();
					if(i==1)
						name = entry2.getText();
					if(i==2)
						email = entry2.getText();
					i++;
				}
			
				contributors.add(new Contributor(netID, name, email));
			}
			first = false;
		}
		
		return contributors;
		
	}
	
	@Data
	public class Contributor {
		
		private final String netID, name, email;

	}
	
	
}
