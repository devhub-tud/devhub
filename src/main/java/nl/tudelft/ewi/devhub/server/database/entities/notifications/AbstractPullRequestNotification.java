package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import java.net.URI;

/**
 * Created by jgmeligmeyling on 28/06/2017.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractPullRequestNotification extends Notification implements RepositoryNotification, HasWatchable {

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "repository_id", referencedColumnName = "repository_id"),
            @JoinColumn(name = "issue_id", referencedColumnName = "issue_id")
    })
    private PullRequest pullRequest;

    @Override
    public URI getURI() {
        return getPullRequest().getURI();
    }

    @Override
    public RepositoryEntity getRepositoryEntity() {
        return getPullRequest().getRepository();
    }

    @Override
    public Watchable getWatchable() {
        return getPullRequest();
    }
}
