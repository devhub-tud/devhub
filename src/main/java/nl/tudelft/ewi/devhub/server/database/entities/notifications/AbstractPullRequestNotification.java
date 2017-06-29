package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.issues.AbstractIssue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;

import javax.persistence.Entity;

/**
 * Created by jgmeligmeyling on 28/06/2017.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractPullRequestNotification extends AbstractIssueNotification implements RepositoryNotification, HasWatchable {

    @Override
    public void setIssue(AbstractIssue issue) {
        setPullRequest((PullRequest) issue);
    }

    @Override
    public PullRequest getIssue() {
        return (PullRequest) super.getIssue();
    }

    public void setPullRequest(PullRequest pullRequest) {
        super.setIssue(pullRequest);
    }

    public PullRequest getPullRequest() {
        return getIssue();
    }

}
