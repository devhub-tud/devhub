package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.jaxrs.models.BuildResult.Status;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend.BuildSubmitter;
import nl.tudelft.ewi.devhub.server.backend.mail.BuildResultMailer;
import nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleWarningGenerator;
import nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleWarningGenerator.CheckStyleReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.FindBugsWarningGenerator;
import nl.tudelft.ewi.devhub.server.backend.warnings.FindBugsWarningGenerator.FindBugsReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.PMDWarningGenerator;
import nl.tudelft.ewi.devhub.server.backend.warnings.PMDWarningGenerator.PMDReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.SuccessiveBuildFailureGenerator;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CheckstyleWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.FindbugsWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.SuccessiveBuildFailure;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedBuildServer;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Set;

import static java.net.URLDecoder.decode;

@Slf4j
@RequestScoped
@Path("hooks")
@ValidateRequest
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + Resource.UTF8_CHARSET)
public class HooksResource extends Resource {

	private final BuildResults buildResults;
	private final RepositoriesController repositoriesController;
	private final BuildResultMailer mailer;
	private final Commits commits;
	private final Warnings warnings;
	private final PMDWarningGenerator pmdWarningGenerator;
	private final CheckstyleWarningGenerator checkstyleWarningGenerator;
	private final FindBugsWarningGenerator findBugsWarningGenerator;
	private final SuccessiveBuildFailureGenerator successiveBuildFailureGenerator;
	private final EventBus asyncEventBus;
	private final BuildSubmitter buildSubmitter;

	@Inject
	public HooksResource(BuildResults buildResults,
	                     Commits commits,
	                     Warnings warnings,
	                     BuildResultMailer mailer,
	                     PMDWarningGenerator pmdWarningGenerator,
	                     RepositoriesController repositoriesController,
	                     FindBugsWarningGenerator findBugsWarningGenerator,
	                     CheckstyleWarningGenerator checkstyleWarningGenerator,
	                     SuccessiveBuildFailureGenerator successiveBuildFailureGenerator,
	                     EventBus asyncEventBus,
	                     BuildSubmitter buildSubmitter) {
		this.mailer = mailer;
		this.commits = commits;
		this.warnings = warnings;
		this.asyncEventBus = asyncEventBus;
		this.buildResults = buildResults;
		this.pmdWarningGenerator = pmdWarningGenerator;
		this.repositoriesController = repositoriesController;
		this.findBugsWarningGenerator = findBugsWarningGenerator;
		this.checkstyleWarningGenerator = checkstyleWarningGenerator;
		this.successiveBuildFailureGenerator = successiveBuildFailureGenerator;
		this.buildSubmitter = buildSubmitter;
	}

	/**
	 * Git push hook implementation.
	 *
	 * @param push GitPush request.
	 */
	@POST
	@Path("git-push")
	public void onGitPush(@Valid GitPush push) {
		log.info("Received git-push event: {}", push);
		asyncEventBus.post(push);
	}


	@POST
	@Path("build-result")
	@Transactional
	@RequireAuthenticatedBuildServer
	public void onBuildResult(@QueryParam("repository") @NotEmpty String repository,
							  @QueryParam("commit") @NotEmpty String commitId,
			nl.tudelft.ewi.build.jaxrs.models.BuildResult buildResult) throws UnsupportedEncodingException {

		log.info("Retrieved build result for {} at {}", commitId, repository);
		String repoName = decode(repository, "UTF-8");
		RepositoryEntity repositoryEntity = repositoriesController.find(repoName);
		Commit commit = commits.ensureExists(repositoryEntity, commitId);

		BuildResult result;
		try {
			result = buildResults.find(repositoryEntity, commitId);
			result.setSuccess(buildResult.getStatus() == Status.SUCCEEDED);
			result.setLog(Joiner.on('\n')
				.join(buildResult.getLogLines()));

			buildResults.merge(result);
		}
		catch (EntityNotFoundException e) {
			result = BuildResult.newBuildResult(commit);
			result.setSuccess(buildResult.getStatus() == Status.SUCCEEDED);
			result.setLog(Joiner.on('\n')
				.join(buildResult.getLogLines()));

			buildResults.persist(result);
		}

		if (!result.getSuccess()) {
			mailer.sendFailedBuildResult(Lists.newArrayList(Locale.ENGLISH), result);
		}

		try {
			Set<SuccessiveBuildFailure> swarns = successiveBuildFailureGenerator.generateWarnings(commit, result);
			warnings.persist(repositoryEntity, swarns);
		}
		catch (Exception e) {
			log.warn("Failed to persist sucessive build failure for {}", e, result);
		}

		try {
			// Make the build submitter aware that there may be capacity
			synchronized (buildSubmitter) {
				buildSubmitter.notify();
			}
		}
		catch (Exception e) {
			log.warn("Failed to notify build submitter: " + e.getMessage(), e);
		}

		asyncEventBus.post(result);
	}

	@POST
	@Path("pmd-result")
	@RequireAuthenticatedBuildServer
	@Consumes(MediaType.APPLICATION_XML)
	@Transactional
	public void onPmdResult(@QueryParam("repository") @NotEmpty String repository,
							@QueryParam("commit") @NotEmpty String commitId,
							final PMDReport report) throws UnsupportedEncodingException {

		log.info("Retrieved PMD result for {} at {}", commitId, repository);
		String repoName = decode(repository, "UTF-8");
		RepositoryEntity repositoryEntity = repositoriesController.find(repoName);

		Commit commit = commits.ensureExists(repositoryEntity, commitId);
		Set<PMDWarning> pmdWarnings = pmdWarningGenerator.generateWarnings(commit, report);
		Set<PMDWarning> persistedWarnings = warnings.persist(repositoryEntity, pmdWarnings);
		log.info("Persisted {} of {} PMD warnings for {}", persistedWarnings.size(),
				pmdWarnings.size(), repositoryEntity);
	}

	@POST
	@Path("checkstyle-result")
	@RequireAuthenticatedBuildServer
	@Consumes(MediaType.APPLICATION_XML)
	@Transactional
	public void onCheckstyleResult(@QueryParam("repository") @NotEmpty String repository,
								   @QueryParam("commit") @NotEmpty String commitId,
								   final CheckStyleReport report) throws UnsupportedEncodingException {

		log.info("Retrieved Checkstyle result for {} at {}", commitId, repository);
		String repoName = decode(repository, "UTF-8");
		RepositoryEntity repositoryEntity = repositoriesController.find(repoName);

		Commit commit = commits.ensureExists(repositoryEntity, commitId);
		Set<CheckstyleWarning> checkstyleWarnings = checkstyleWarningGenerator.generateWarnings(commit, report);
		Set<CheckstyleWarning> persistedWarnings = warnings.persist(repositoryEntity, checkstyleWarnings);
		log.info("Persisted {} of {} Checkstyle warnings for {}", persistedWarnings.size(),
				checkstyleWarnings.size(), repositoryEntity);
	}

	@POST
	@Path("findbugs-result")
	@RequireAuthenticatedBuildServer
	@Consumes(MediaType.APPLICATION_XML)
	@Transactional
	public void onFindBugsResult(@QueryParam("repository") @NotEmpty String repository,
								 @QueryParam("commit") @NotEmpty String commitId,
								 final FindBugsReport report) throws UnsupportedEncodingException {

		log.info("Retrieved Findbugs result for {} at {}", commitId, repository);
		String repoName = decode(repository, "UTF-8");
		RepositoryEntity repositoryEntity = repositoriesController.find(repoName);

		Commit commit = commits.ensureExists(repositoryEntity, commitId);
		Set<FindbugsWarning> findbugsWarnings = findBugsWarningGenerator.generateWarnings(commit, report);
		Set<FindbugsWarning> persistedWarnings = warnings.persist(repositoryEntity, findbugsWarnings);
		log.info("Persisted {} of {} FindBugs warnings for {}", persistedWarnings.size(),
				findbugsWarnings.size(), repositoryEntity);
	}

}
