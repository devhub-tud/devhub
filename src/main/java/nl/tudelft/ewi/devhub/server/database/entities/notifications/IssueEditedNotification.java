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
public class IssueEditedNotification extends AbstractIssueNotification {

    @Override
    protected String getTitleResourceKey() {
        return "notifications.issueEdit.title";
    }

}
