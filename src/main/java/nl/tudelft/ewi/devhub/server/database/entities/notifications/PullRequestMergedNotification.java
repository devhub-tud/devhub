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
public class PullRequestMergedNotification extends AbstractPullRequestNotification {
    @Override
    protected String getTitleResourceKey() {
        return "notifications.branch.deleted.title";
    }

    @Override
    protected String getDescriptionResourceKey() {
        return "notifications.pull-request.empty-message";
    }

    @Override
    protected Object[] getTitleParameters() {
        return new Object[] { getSender().getName(), getPullRequest().getBranchName() };
    }
}
