package nl.tudelft.ewi.devhub.webtests;

import nl.tudelft.ewi.devhub.webtests.utils.WebTest;
import nl.tudelft.ewi.devhub.webtests.views.DiffElement;
import nl.tudelft.ewi.devhub.webtests.views.DiffInCommitView;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

public class DiffInCommitViewTest extends WebTest {

    private static final By INLINE_DIFF_COMMENT = By.cssSelector("div.panel-comment");
    private static final By COMMENT_BODY = By.className("panel-body");
    private static final By INLINE_COMMENT_BUTTON = By.className("btn-comment");
    private static final By DIFF_COMMENT_LIST = By.id("comment-list");
    private static final String EXPECTED_IMG_URL_FIRST_COMMENT = "http://twemoji.maxcdn.com/16x16/1f44d.png";
    private static final String EXPECTED_IMG_URL_SECOND_COMMENT = "http://twemoji.maxcdn.com/16x16/1f600.png";
    private static final String FIRST_COMMENT_TEXT = ":+1:";
    private static final String SECOND_COMMENT_TEXT = ":grinning:";

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

        commitView = commitView.postInlineComment(0, 0, FIRST_COMMENT_TEXT);
        waitForCondition(5, webDriver -> {
            String imgUrl = webDriver.findElement(INLINE_DIFF_COMMENT)
                    .findElement(COMMENT_BODY)
                    .findElement(By.tagName("img"))
                    .getAttribute("src");
            return imgUrl.equals(EXPECTED_IMG_URL_FIRST_COMMENT);
        });

        commitView.postInlineComment(diffs.size() / 2, diffLines / 2, SECOND_COMMENT_TEXT);
        waitForCondition(5, webDriver -> {
            String imgUrl = webDriver.findElements(INLINE_DIFF_COMMENT).get(1)
                    .findElement(COMMENT_BODY)
                    .findElement(By.tagName("img"))
                    .getAttribute("src");
            return imgUrl.equals(EXPECTED_IMG_URL_SECOND_COMMENT);
        });

        commitView.reloadPage();
        String imgUrl = getDriver().findElement(INLINE_DIFF_COMMENT)
                .findElement(COMMENT_BODY)
                .findElement(By.tagName("img"))
                .getAttribute("src");
        assertEquals(EXPECTED_IMG_URL_FIRST_COMMENT, imgUrl);

        imgUrl = getDriver().findElements(INLINE_DIFF_COMMENT).get(1)
                .findElement(COMMENT_BODY)
                .findElement(By.tagName("img"))
                .getAttribute("src");
        assertEquals(EXPECTED_IMG_URL_SECOND_COMMENT, imgUrl);
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

        commitView = commitView.postCommentOnDiff(FIRST_COMMENT_TEXT);
        waitForCondition(5, webDriver -> {
            String imgUrl = webDriver.findElement(DIFF_COMMENT_LIST)
                    .findElement(COMMENT_BODY)
                    .findElement(By.tagName("img"))
                    .getAttribute("src");
            return imgUrl.equals(EXPECTED_IMG_URL_FIRST_COMMENT);
        });

        commitView.postCommentOnDiff(SECOND_COMMENT_TEXT);
        waitForCondition(5, webDriver -> {
            String imgUrl = webDriver.findElement(DIFF_COMMENT_LIST)
                    .findElements(COMMENT_BODY).get(1)
                    .findElement(By.tagName("img"))
                    .getAttribute("src");
            return imgUrl.equals(EXPECTED_IMG_URL_SECOND_COMMENT);
        });

        commitView.reloadPage();
        String imgUrl = getDriver().findElement(DIFF_COMMENT_LIST)
                .findElement(COMMENT_BODY)
                .findElement(By.tagName("img"))
                .getAttribute("src");
        assertEquals(EXPECTED_IMG_URL_FIRST_COMMENT, imgUrl);

        imgUrl = getDriver().findElement(DIFF_COMMENT_LIST)
                .findElements(COMMENT_BODY).get(1)
                .findElement(By.tagName("img"))
                .getAttribute("src");
        assertEquals(EXPECTED_IMG_URL_SECOND_COMMENT, imgUrl);
    }
}
