package nl.tudelft.ewi.devhub.server.database.entities.notifications;

import lombok.Data;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import java.net.URI;

/**
 * Created by jgmeligmeyling on 11/07/2017.
 */
@Data
public class BuildNotification extends Notification implements RepositoryNotification, HasWatchable {

    @OneToOne
    @JoinColumns(value = {
            @JoinColumn(name = "commit_id", referencedColumnName = "commit_id"),
            @JoinColumn(name = "repository_id", referencedColumnName = "repository_id")
    })
    private BuildResult buildResult;

    @Override
    public Watchable getWatchable() {
        return getBuildResult().getRepository();
    }

    @Override
    public URI getURI() {
        return getBuildResult().getURI();
    }

    @Override
    public RepositoryEntity getRepositoryEntity() {
        return getBuildResult().getRepository();
    }

    @Override
    protected String getTitleResourceKey() {
        return Boolean.TRUE.equals(getBuildResult().getSuccess()) ? "notifications.build-result.success" : "notifications.build-result.failed";
    }

    @Override
    protected Object[] getTitleParameters() {
        return new Object[] {
                getBuildResult().getCommit().getCommitId()
        };
    }

    @Override
    protected String getDescriptionResourceKey() {
        return "notifications.build-result.description";
    }
}
