package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DiffInCommitView;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class DiffInCommitViewTest extends WebTest {

    @Test
    public void testCommentInline() {

        // Go to first commit (and implicitly assume it exists)
        DiffInCommitView commitView = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView()
                .listMyProjects()
                .get(0).click()
                .listCommits()
                .get(0).click();

        commitView = commitView.postInlineComment(":grinning:");
        this.waitForCondition(5, webDriver -> {
            WebElement comment = getDriver().findElement(By.cssSelector("div.panel-comment"));
            String commentText = comment.findElement(By.className("panel-body")).getText();
            return commentText.charAt(0) == '\uD83D' && commentText.charAt(1) == '\uDE00';
        });

        System.out.println("\n\n\n\n=============\n" + commitView.listDiffs());
    }
}
