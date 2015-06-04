package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.inject.Inject;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repository;

/**
 * @author Liam Clark
 */
public abstract class AbstractCommitWarningGenerator<T extends CommitWarning, A> implements CommitWarningGenerator<T, A> {

    protected final GitServerClient gitServerClient;

    @Inject
    public AbstractCommitWarningGenerator(GitServerClient gitServerClient) {
        this.gitServerClient = gitServerClient;
    }

    @SneakyThrows
    protected nl.tudelft.ewi.git.client.Commit getGitCommit(Commit commit) {
        return gitServerClient.repositories()
            .retrieve(commit.getRepository().getRepositoryName())
            .retrieveCommit(commit.getCommitId());
    }

    @SneakyThrows
    protected Repository getRepository(Commit commit) {
        return  gitServerClient.repositories()
            .retrieve(commit.getRepository().getRepositoryName());
    }

    /**
     * Get the property for a course
     * @param commit commit for the group
     * @param key key under which the property is stored
     * @param def default value
     * @return the value
     */
    protected String getProperty(final Commit commit, String key, final String def) {
        final String value = commit.getRepository()
            .getCourse()
            .getProperties()
            .get(key);
        if(value == null) {
            return def;
        }
        return value;
    }

    /**
     * Get the property for a course
     * @param commit commit for the group
     * @param key key under which the property is stored
     * @param def default value
     * @return the value
     */
    protected String[] getProperty(final Commit commit, String key, final String[] def) {
        final String value = commit.getRepository()
            .getCourse()
            .getProperties()
            .get(key);
        if(value == null) {
            return def;
        }
        return value.split(",");
    }
}
