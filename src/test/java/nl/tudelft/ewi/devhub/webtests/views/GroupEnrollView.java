package nl.tudelft.ewi.devhub.webtests.views;

import com.google.common.collect.Lists;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.webtests.utils.Dom;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class GroupEnrollView extends AuthenticatedView {

    private static final By MEMBER2_FIELD = By.name("member-2");
    private static final By NEXT_BUTTON = By.name("next");
    private static final By GROUP_MEMBERS_LABEL = By.xpath("//label[starts-with(normalize-space(.), 'Group members')]");
    private static final String TABLE_NETID_CLASSNAME = "truncate";


    public GroupEnrollView(WebDriver driver) {
        super(driver);
    }

    @Override
    protected void invariant() {
        super.invariant();
        assertTrue(currentPathStartsWith("/courses"));
        assertTrue(currentPathContains("/enroll"));
    }

    /**
     * This method fills in the given <code>student net-id</code> into the second member input box.
     *
     * @param studentNetId The student id to fill in.
     * @return The current {@link GroupEnrollView}.
     */
    public GroupEnrollView setMember2Field(String studentNetId) {
        invariant();
        WebElement member2Field = getDriver().findElement(MEMBER2_FIELD);
        member2Field.sendKeys(studentNetId);
        return this;
    }

    /**
     * This method clicks the next button.
     *
     * @return The current {@link GroupEnrollView}.
     */
    public GroupEnrollView clickNext() {
        invariant();
        WebElement next = getDriver().findElement(NEXT_BUTTON);
        next.click();
        return this;
    }

    public List<User> groupMembers() {
        invariant();
        WebElement label = getDriver().findElement(GROUP_MEMBERS_LABEL);
        WebElement table = Dom.nextSibling(label, "table");
        return listUsersFromTable(table);
    }

    private List<User> listUsersFromTable(WebElement table) {
        List<WebElement> entries = table.findElements(By.tagName("td"));
        if (entries.size() == 1) {
            if (!Dom.isVisible(entries.get(0), By.tagName("a"))) {
                return Lists.newArrayList();
            }
        }

        List<User> users = new ArrayList<>();
        for (WebElement entry : entries) {
            WebElement studentId = entry.findElement(By.className(TABLE_NETID_CLASSNAME));
            WebElement studentName = entry.findElement(By.tagName("b"));
            User user = new User();
            user.setName(studentName.getText());
            user.setNetId(studentId.getText());
            users.add(user);
        }
        return users;
    }

}
