package nl.tudelft.ewi.devhub.web;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Dom {
	
	public static WebElement parent(WebElement element) {
		return element.findElement(By.xpath("ancestor::*"));
	}
	
	public static List<WebElement> parent(List<WebElement> elements) {
		Set<WebElement> parents = Sets.newLinkedHashSet();
		for (WebElement element : elements) {
			parents.add(parent(element));
		}
		return Lists.newArrayList(parents);
	}
	
	public static WebElement nextSibling(WebElement element) {
		return nextSibling(element, "*");
	}
	
	public static WebElement nextSibling(WebElement element, String tag) {
		return element.findElement(By.xpath("following-sibling::" + tag));
	}
	
	public static WebElement prevSibling(WebElement element) {
		return prevSibling(element, "*");
	}
	
	public static WebElement prevSibling(WebElement element, String tag) {
		return element.findElement(By.xpath("preceding-sibling::" + tag));
	}
	
	public static boolean isVisible(SearchContext context, By selector) {
		try {
			context.findElement(selector);
			return true;
		}
		catch (NoSuchElementException e) {
			return false;
		}
	}

}
