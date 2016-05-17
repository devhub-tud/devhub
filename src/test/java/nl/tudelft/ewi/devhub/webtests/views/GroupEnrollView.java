package nl.tudelft.ewi.devhub.webtests.views;

import com.google.common.collect.Lists;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.webtests.utils.Dom;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class GroupEnrollView extends AuthenticatedView {

    private static final String MEMBER_FIELD_CLASS_PREFIX = "member-";
    private static final By NEXT_BUTTON = By.name("next");
    private static final By CREATE_GROUP_BUTTON = By.name("finish");
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
     * @param number The field to set
     * @param studentNetId The student id to fill in.
     * @return The current {@link GroupEnrollView}.
     */
    public GroupEnrollView setMemberField(int number, String studentNetId) {
        invariant();
        String fullName = MEMBER_FIELD_CLASS_PREFIX + number;
        WebDriver driver = getDriver();
        JavascriptExecutor jsExec = (JavascriptExecutor) driver;
        jsExec.executeScript("document.getElementsByName('"+ fullName +"')[0].removeAttribute('readonly');");
        WebElement memberField = getDriver().findElement(By.name(fullName));
        memberField.clear();
        memberField.sendKeys(studentNetId);
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

    /**
     * This method clicks the create group button.
     *
     * @return The current {@link GroupEnrollView}.
     */
    public GroupEnrollView clickCreateGroup() {
        invariant();
        WebElement next = getDriver().findElement(CREATE_GROUP_BUTTON);
        next.click();
        return this;
    }

    /**
     * This method lists the members form the group that is about to be created.
     *
     * @return The current {@link GroupEnrollView}.
     */
    public List<User> groupMembers() {
        invariant();
        WebElement label = getDriver().findElement(GROUP_MEMBERS_LABEL);
        WebElement table = Dom.nextSibling(label, "table");
        return listUsersFromTable(table);
    }

    public boolean memberFieldContainsError(int id) {
        invariant();
        WebElement field = getDriver().findElement(By.name(MEMBER_FIELD_CLASS_PREFIX + id));
        String formClass = field.findElement(By.xpath("..")).getAttribute("class");
        return formClass.contains("has-error");
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

    /**
     * Navigate to the contributors view.
     * @return A {@link ContributorsView} instance.
     */
    public ContributorsView toContributorsView() {
        getDriver().findElement(By.linkText("Contributors")).click();
        return new ContributorsView(getDriver());
    }

}
