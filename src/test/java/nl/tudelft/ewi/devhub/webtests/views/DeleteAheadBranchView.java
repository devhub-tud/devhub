package nl.tudelft.ewi.devhub.webtests.views;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.webtests.utils.Dom;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by fastjur on 13-6-16.
 */
public class DeleteAheadBranchView extends ProjectSidebarView {

    private static final By RECENT_COMMITS_HEADER = By.xpath("//h4[starts-with(normalize-space(.), 'Recent commits')]");

    public DeleteAheadBranchView(WebDriver driver) {
        super(driver);
    }

    @Override
    protected void invariant() {
        super.invariant();
        assertNotNull(getDriver().findElement(RECENT_COMMITS_HEADER));
    }

    public DeleteAheadBranchView typeBranchName(String branchName) {
        getDriver().findElement(By.cssSelector("input[name=branchNameConf]"))
                .sendKeys(branchName);
        return new DeleteAheadBranchView(getDriver());
    }

    public DeleteAheadBranchView clickRemoveAheadBranch() {
        getDriver().findElement(By.cssSelector("button[type=submit]")).click();
        return new DeleteAheadBranchView(getDriver());
    }

    /**
     * @return A {@link List} of all {@link DeleteAheadBranchView.Branch}es in the Branch selection dropdown.
     */
    public List<DeleteAheadBranchView.Branch> listBranches(){
        invariant();
        WebElement dropdown = getDriver().findElement(By.cssSelector("ul.dropdown-menu"));
        return listBranches(dropdown);

    }

    private List<DeleteAheadBranchView.Branch> listBranches(WebElement dropdown) {
        List<WebElement> entries = dropdown.findElements(By.tagName("li"));
        List<DeleteAheadBranchView.Branch> branches = Lists.newArrayList();

        for(WebElement entry : entries){
            WebElement anchor = entry.findElement(By.tagName("a"));
            String name = anchor.getAttribute("text").split("\n")[1].trim();
            branches.add(new Branch(name, anchor));
        }
        return branches;
    }

    /**
     * @return A {@link List} of all {@link DeleteAheadBranchView.Commit}s in the "Recent commits" section
     */
    public List<DeleteAheadBranchView.Commit> listCommits() {
        invariant();
        WebElement table = getDriver().findElement(By.id("table-commits"));
        return listCommits(table);
    }

    private List<DeleteAheadBranchView.Commit> listCommits(WebElement table) {
        List<WebElement> entries = table.findElements(By.tagName("td"));
        List<DeleteAheadBranchView.Commit> commits = Lists.newArrayList();

        if (entries.size() == 1) {
            if (!Dom.isVisible(entries.get(0), By.tagName("a"))) {
                return commits;
            }
        }

        for (WebElement entry : entries) {
            WebElement diffLink = entry.findElement(By.tagName("a"));
            String message = entry.findElement(By.className("comment")).getText();
            String author = entry.findElement(By.className("committer")).getText();
            commits.add(new DeleteAheadBranchView.Commit(message, author, null, diffLink));
        }

        return commits;

    }

    @Data
    public class Branch {

        private final String name;

        private final WebElement anchor;

        @SneakyThrows
        public DeleteAheadBranchView click() {
            // Open dropdown
            getDriver().findElement(By.cssSelector("div.pull-right button.dropdown-toggle")).click();
            // Wait for the animation to complete
            Thread.sleep(100);
            anchor.click();
            return new DeleteAheadBranchView(getDriver());
        }

    }

    @Data
    public class Commit {

        private final String message, author;

        private final List<String> tagNames;

        @Getter(AccessLevel.NONE)
        private final WebElement anchor;

        public DiffInCommitView click() {
            anchor.click();
            return new DiffInCommitView(getDriver());
        }

    }
}
