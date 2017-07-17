package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * Created by jgmeligmeyling on 28/06/2017.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class IssueAssignedNotification extends AbstractIssueNotification {

    @Override
    protected String getTitleResourceKey() {
        return "notifications.issueAssign.title";
    }

    @Override
    protected Object[] getTitleParameters() {
        return new Object[] {
                getSender().getName(),
                getIssue().getIssueId(),
                getIssue().getTitle(),
                getAssignee()
        };
    }

    public String getAssignee() {
        return getRecipients().keySet().stream().findFirst().get().getName();
    }

}
