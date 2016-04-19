package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.persist.UnitOfWork;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.jaxrs.models.BuildResult.Status;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.PullRequestBackend;
import nl.tudelft.ewi.devhub.server.backend.RunnableInUnitOfWork;
import nl.tudelft.ewi.devhub.server.backend.mail.BuildResultMailer;
import nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleWarningGenerator;
import nl.tudelft.ewi.devhub.server.backend.warnings.CheckstyleWarningGenerator.CheckStyleReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.CommitPushWarningGenerator;
import nl.tudelft.ewi.devhub.server.backend.warnings.FindBugsWarningGenerator;
import nl.tudelft.ewi.devhub.server.backend.warnings.FindBugsWarningGenerator.FindBugsReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.PMDWarningGenerator;
import nl.tudelft.ewi.devhub.server.backend.warnings.PMDWarningGenerator.PMDReport;
import nl.tudelft.ewi.devhub.server.backend.warnings.SuccessiveBuildFailureGenerator;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.Commits;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.RepositoriesController;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CheckstyleWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.FindbugsWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.SuccessiveBuildFailure;
import nl.tudelft.ewi.devhub.server.util.CommitIterator;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedBuildServer;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.models.BranchModel;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
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
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private final GitPushHandlerWorkerFactory gitPushHandlerWorkerFactory;
	private final PMDWarningGenerator pmdWarningGenerator;
	private final CheckstyleWarningGenerator checkstyleWarningGenerator;
	private final FindBugsWarningGenerator findBugsWarningGenerator;
	private final SuccessiveBuildFailureGenerator successiveBuildFailureGenerator;
	private final ExecutorService executor;

	@Inject
	public HooksResource(BuildResults buildResults,
	                     Commits commits,
	                     Warnings warnings,
	                     BuildResultMailer mailer,
	                     ExecutorService executor,
	                     PMDWarningGenerator pmdWarningGenerator,
	                     GitPushHandlerWorkerFactory gitPushHandlerWorkerFactory,
	                     RepositoriesController repositoriesController,
	                     FindBugsWarningGenerator findBugsWarningGenerator,
	                     CheckstyleWarningGenerator checkstyleWarningGenerator,
	                     SuccessiveBuildFailureGenerator successiveBuildFailureGenerator) {
		this.mailer = mailer;
		this.commits = commits;
		this.warnings = warnings;
		this.executor = executor;
		this.buildResults = buildResults;
		this.pmdWarningGenerator = pmdWarningGenerator;
		this.gitPushHandlerWorkerFactory = gitPushHandlerWorkerFactory;
		this.repositoriesController = repositoriesController;
		this.findBugsWarningGenerator = findBugsWarningGenerator;
		this.checkstyleWarningGenerator = checkstyleWarningGenerator;
		this.successiveBuildFailureGenerator = successiveBuildFailureGenerator;
	}

	/**
	 * Git push hook implementation.
	 *
	 * @param push GitPush request.
	 * @see GitPushHandlerWorker#onGitPush(GitPush)
	 */
	@POST
	@Path("git-push")
	public void onGitPush(@Valid GitPush push) {
		log.info("Received git-push event: {}", push);
		GitPushHandlerWorker gitPushHandler = gitPushHandlerWorkerFactory.create(push);
		executor.submit(gitPushHandler);
	}

	public static class GitPushHandlerWorker extends RunnableInUnitOfWork {

		private final Provider<GitPushHandler> gitPushHandlerProvider;
		private final GitPush gitPush;

		@Inject
		public GitPushHandlerWorker(Provider<UnitOfWork> workProvider, Provider<GitPushHandler> gitPushHandlerProvider, @Assisted GitPush gitPush) {
			super(workProvider);
			this.gitPushHandlerProvider = gitPushHandlerProvider;
			this.gitPush = gitPush;
		}

		@Override
		@Transactional
		protected void runInUnitOfWork() {
			this.gitPushHandlerProvider.get().handle(gitPush);
		}

	}

	/**
	 * The GitPushHandler ensures that the {@code git-push} API call is non blocking and
	 * the operations are performed within an unit of work.
	 */
	public static class GitPushHandler {

		private final Commits commits;
		private final Warnings warnings;
		private final PullRequests pullRequests;
		private final BuildsBackend buildBackend;
		private final RepositoriesApi repositoriesApi;
		private final PullRequestBackend pullRequestBackend;
		private final RepositoriesController repositoriesController;
		private final Set<CommitPushWarningGenerator> pushWarningGenerators;

		@Inject
		public GitPushHandler(
			Warnings warnings,
			Set<CommitPushWarningGenerator> pushWarningGenerators,
			Commits commits, PullRequests pullRequests,
			BuildsBackend buildBackend,
			RepositoriesApi repositoriesApi,
			PullRequestBackend pullRequestBackend,
			RepositoriesController repositoriesController
		) {
			this.warnings = warnings;
			this.commits = commits;
			this.pullRequests = pullRequests;
			this.buildBackend = buildBackend;
			this.repositoriesApi = repositoriesApi;
			this.pullRequestBackend = pullRequestBackend;
			this.pushWarningGenerators = pushWarningGenerators;
			this.repositoriesController = repositoriesController;
		}

		@Transactional
		public void handle(GitPush gitPush) {
			RepositoryApi repositoryApi = repositoriesApi.getRepository(gitPush.getRepository());
			DetailedRepositoryModel repositoryModel = repositoryApi.getRepositoryModel();
			RepositoryEntity repositoryEntity = repositoriesController.find(gitPush.getRepository());

			Set<Commit> commitsToBeBuilt =
				// For every branch
				repositoryModel.getBranches().stream().sequential()
					// Get the head
					.map(BranchModel::getCommit)
					// Ensure the head commit exists in the database
					.map(commitModel -> commits.ensureExists(repositoryEntity, commitModel.getCommit()))
					// Pick the first 3 unbuild commits using BFS
					.flatMap(commit -> CommitIterator.stream(commit, Commit::hasNoBuildResult).limit(3))
					// Limit the results
					.limit(20)
					// Get the unique commits
					.collect(Collectors.toSet());

			log.info("Building commits {}", commitsToBeBuilt);
			commitsToBeBuilt.stream()
				.forEach(buildBackend::buildCommit);

			log.info("Find open pull requests for repository {}", repositoryEntity);
			pullRequests.findOpenPullRequests(repositoryEntity)
				.forEach(pullRequest -> pullRequestBackend.updatePullRequest(repositoryApi, pullRequest));

			log.info("Generating warnings for commits {}", commitsToBeBuilt);
			commitsToBeBuilt.forEach(commit -> triggerWarnings(repositoryEntity, commit, gitPush));
		}


		protected void triggerWarnings(final RepositoryEntity repositoryEntity, final Commit commit, final GitPush gitPush) {
			assert repositoryEntity != null;
			assert commit != null;
			assert gitPush != null;

			Set<? extends CommitWarning> pushWarnings = pushWarningGenerators.stream()
				.flatMap(generator -> {
					try {
						Set<? extends CommitWarning> commitWarningList = generator.generateWarnings(commit, gitPush);
						return commitWarningList.stream();
					}
					catch (Exception e) {
						log.warn("Failed to generate warnings with {} for {} ", generator, commit);
						log.warn(e.getMessage(), e);
						return Stream.empty();
					}
				})
				.collect(Collectors.toSet());

			Set<? extends CommitWarning> persistedWarnings = warnings.persist(repositoryEntity, pushWarnings);
			log.info("Persisted {} of {} push warnings for {}", persistedWarnings.size(),
				pushWarnings.size(), repositoryEntity);
		}

	}

	/**
	 * Assisted inject factory for the {@link nl.tudelft.ewi.devhub.server.web.resources.HooksResource.GitPushHandler}.
	 */
	public interface GitPushHandlerWorkerFactory {

		/**
		 * Create a new {@link nl.tudelft.ewi.devhub.server.web.resources.HooksResource.GitPushHandler}
		 * @param gitPush {@link GitPush} to trigger.
		 * @return A new instance.
		 */
		GitPushHandlerWorker create(GitPush gitPush);

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
