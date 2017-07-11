package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.issues.AbstractIssue;
import nl.tudelft.ewi.devhub.server.web.templating.Translator;

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
public abstract class AbstractIssueNotification extends Notification implements RepositoryNotification, HasWatchable {

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "repository_id", referencedColumnName = "repository_id"),
            @JoinColumn(name = "issue_id", referencedColumnName = "issue_id")
    })
    private AbstractIssue issue;

    @Override
    public String getDescription(Translator translator) {
        return Strings.nullToEmpty(issue.getDescription());
    }

    @Override
    public URI getURI() {
        return getIssue().getURI();
    }

    @Override
    public RepositoryEntity getRepositoryEntity() {
        return getIssue().getRepository();
    }

    @Override
    public Watchable getWatchable() {
        return getIssue();
    }
}
