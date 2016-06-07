package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DiffInCommitView;
import org.junit.Test;
import org.openqa.selenium.By;

public class CommentTest extends WebTest {

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
//        this.waitForCondition(5, commitView.listDiffs().get(0).getElement().findElement(By.cssSelector()));

        System.out.println("\n\n\n\n=============\n" + commitView.listDiffs());
    }
}
