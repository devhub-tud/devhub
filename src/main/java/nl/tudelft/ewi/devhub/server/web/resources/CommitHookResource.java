package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.collect.Lists;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.GitUsageWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@RequestScoped
@Path("courses/{courseCode}/groups/{groupNumber}/commits/{commitId}/generate-warnings")
@Produces(MediaType.APPLICATION_JSON)
public class CommitHookResource extends Resource {

    private final Group group;
    private final Commits commits;
    private final Warnings warnings;

    @Inject
    CommitHookResource(final @Named("current.user") User currentUser,
                       final @Named("current.group") Group group,
                       final Commits commits,
                       final Warnings warnings) {

        this.group = group;
        this.commits = commits;
        this.warnings = warnings;
    }

    @POST
    @Transactional
    public List<CommitWarning> showCommitOverview(@PathParam("commitId") String commitId) {

        Commit commit = commits.ensureExists(group, commitId);
        GitUsageWarning warning = new GitUsageWarning();
        warning.setRepository(group);
        warning.setCommit(commit);
        warnings.persist(warning);

        PMDWarning anotherWarning = new PMDWarning();
        anotherWarning.setCommit(commit);
        anotherWarning.setRepository(group);
        anotherWarning.setSource(new Source(commit, 7, "pom.xml"));
        anotherWarning.setMessage("File contains tab characters (this is the first instance)");
        anotherWarning.setPriority(1);
        anotherWarning.setRule("idontknow");
        warnings.persist(anotherWarning);

        return warnings.getWarningsFor(group, commitId);
    }

}
