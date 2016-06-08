package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DiffElement;
import nl.tudelft.ewi.devhub.webtests.views.DiffInCommitView;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class DiffInCommitViewTest extends WebTest {

    private static final By INLINE_DIFF_COMMENT = By.cssSelector("div.panel-comment");
    private static final By COMMENT_BODY = By.className("panel-body");
    private static final By INLINE_COMMENT_BUTTON = By.className("btn-comment");
    private static final By DIFF_COMMENT_LIST = By.id("comment-list");

    @Test
    public void testEmojiCommentInline() {

        // Go to first commit (and implicitly assume it exists)
        DiffInCommitView commitView = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView()
                .listMyProjects()
                .get(0).click()
                .listCommits()
                .get(0).click();

        List<DiffElement> diffs = commitView.listDiffs();
        int diffLines = diffs.get(0).getElement().findElements(INLINE_COMMENT_BUTTON).size();

        assertThat("Diff should have at least 2 lines changed for tests to work", diffLines >= 2);

        commitView = commitView.postInlineComment(0, 0, ":+1:");
        this.waitForCondition(5, webDriver -> {
            WebElement comment = webDriver.findElement(INLINE_DIFF_COMMENT);
            String commentText = comment.findElement(COMMENT_BODY).getText();
            return commentText.charAt(0) == '\uD83D' && commentText.charAt(1) == '\uDC4D';
        });

        commitView.postInlineComment(diffs.size() / 2, diffLines / 2, ":grinning:");
        this.waitForCondition(5, webDriver -> {
            WebElement comment = webDriver.findElements(INLINE_DIFF_COMMENT).get(1);
            String commentText = comment.findElement(COMMENT_BODY).getText();
            return commentText.charAt(0) == '\uD83D' && commentText.charAt(1) == '\uDE00';
        });
    }

    @Test
    public void testEmojiCommentOnDiff() {
        // Go to first commit (and implicitly assume it exists)
        DiffInCommitView commitView = openLoginScreen()
                .login(NET_ID, PASSWORD)
                .toCoursesView()
                .listMyProjects()
                .get(0).click()
                .listCommits()
                .get(0).click();

        commitView = commitView.postCommentOnDiff(":+1:");
        this.waitForCondition(5, webDriver -> {
            WebElement comment = webDriver.findElement(DIFF_COMMENT_LIST).findElement(COMMENT_BODY);
            return comment.getText().charAt(0) == '\uD83D' && comment.getText().charAt(1) == '\uDC4D';
        });

        commitView.postCommentOnDiff(":grinning:");
        this.waitForCondition(5, webDriver -> {
            WebElement comment = webDriver.findElement(DIFF_COMMENT_LIST).findElements(COMMENT_BODY).get(1);
            return comment.getText().charAt(0) == '\uD83D' && comment.getText().charAt(1) == '\uDE00';
        });
    }
}
