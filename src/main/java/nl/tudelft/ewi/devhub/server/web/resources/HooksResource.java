package nl.tudelft.ewi.devhub.server.web.resources;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.jaxrs.models.BuildResult.Status;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.backend.PullRequestBackend;
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
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CheckstyleWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.FindbugsWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.SuccessiveBuildFailure;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedBuildServer;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.models.BranchModel;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.resteasy.plugins.validation.hibernate.ValidateRequest;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Set;
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

	private final BuildsBackend buildBackend;
	private final RepositoriesApi repositoriesApi;
	private final BuildResults buildResults;
	private final RepositoriesController repositoriesController;
	private final BuildResultMailer mailer;
	private final PullRequests pullRequests;
	private final PullRequestBackend pullRequestBackend;
	private final Commits commits;
	private final Warnings warnings;
	private final Set<CommitPushWarningGenerator> pushWarningGenerators;
	private final PMDWarningGenerator pmdWarningGenerator;
	private final CheckstyleWarningGenerator checkstyleWarningGenerator;
	private final FindBugsWarningGenerator findBugsWarningGenerator;
	private final SuccessiveBuildFailureGenerator successiveBuildFailureGenerator;

	@Inject
	HooksResource(BuildsBackend buildBackend, RepositoriesApi repositoriesApi, BuildResults buildResults,
		    RepositoriesController repositoriesController, BuildResultMailer mailer, PullRequests pullRequests, PullRequestBackend pullRequestBackend,
			Commits commits, Warnings warnings, PMDWarningGenerator pmdWarningGenerator, CheckstyleWarningGenerator checkstyleWarningGenerator,
			FindBugsWarningGenerator findBugsWarningGenerator, SuccessiveBuildFailureGenerator successiveBuildFailureGenerator,
			Set<CommitPushWarningGenerator> pushWarningGenerators) {

		this.buildBackend = buildBackend;
		this.repositoriesApi = repositoriesApi;
		this.buildResults = buildResults;
		this.repositoriesController = repositoriesController;
		this.mailer = mailer;
		this.pullRequests = pullRequests;
		this.pullRequestBackend = pullRequestBackend;
		this.commits = commits;
		this.warnings = warnings;
		this.pmdWarningGenerator = pmdWarningGenerator;
		this.checkstyleWarningGenerator = checkstyleWarningGenerator;
		this.findBugsWarningGenerator = findBugsWarningGenerator;
		this.successiveBuildFailureGenerator = successiveBuildFailureGenerator;
		this.pushWarningGenerators = pushWarningGenerators;
	}

	@POST
	@Path("git-push")
	public void onGitPush(@Context HttpServletRequest request, @Valid GitPush push) throws UnsupportedEncodingException {
		log.info("Received git-push event: {}", push);

		RepositoryApi repositoryApi = repositoriesApi.getRepository(push.getRepository());
		DetailedRepositoryModel repositoryModel = repositoryApi.getRepositoryModel();
		RepositoryEntity repositoryEntity = repositoriesController.find(push.getRepository());

		Set<Commit> commitsToBeBuilt = repositoryModel.getBranches().stream()
			.map(BranchModel::getCommit)
			.flatMap(commitModel -> findCommitsToBeBuilt(repositoryEntity, repositoryApi, commitModel.getCommit()).stream())
			.collect(Collectors.toSet());

		commitsToBeBuilt.stream()
			.forEach(buildBackend::buildCommit);

		repositoryModel.getBranches().stream()
			.flatMap(branchModel -> findOpenPullRequests(repositoryEntity, branchModel))
			.forEach(pullRequest -> updatePullRequest(repositoryApi, pullRequest));

		commitsToBeBuilt.forEach(commit -> triggerWarnings(repositoryEntity, commit, push));
	}

	protected Set<Commit> findCommitsToBeBuilt(final RepositoryEntity repositoryEntity, final RepositoryApi repository,
											   final String commitId) {
		Set<Commit> commits = Sets.newHashSet();
		CommitModel commitModel = retrieveCommit(repository, commitId);
		findCommitsToBeBuilt(repositoryEntity, repository, commitModel, commits);
		return commits;
	}

	protected void findCommitsToBeBuilt(final RepositoryEntity repositoryEntity, final RepositoryApi repositoryApi,
										final CommitModel commitModel,
										final Set<Commit> results) {
		final Commit commit = commits.ensureExists(repositoryEntity, commitModel.getCommit());

		if(!buildResults.exists(commit)) {
			results.add(commit);
			Stream.of(commitModel.getParents())
				.map(commitId -> retrieveCommit(repositoryApi, commitId))
				.forEach(a -> findCommitsToBeBuilt(repositoryEntity, repositoryApi, a, results));
		}
	}

	@SneakyThrows
	protected CommitModel retrieveCommit(RepositoryApi repository, String commitId) {
		return repository.getCommit(commitId).get();
	}

	@POST
	@Transactional
	@RequireAuthenticatedBuildServer
	@Path("trigger-warning-generation")
	public void triggerWarnings(@QueryParam("repository") @NotEmpty String repositoryName,
								@QueryParam("commit") @NotEmpty String commitId) {

		RepositoryEntity repositoryEntity = repositoriesController.find(repositoryName);
		Commit commit = commits.ensureExists(repositoryEntity, commitId);

		Preconditions.checkNotNull(repositoryEntity);
		Preconditions.checkNotNull(commit);

		GitPush gitPush = new GitPush();
		gitPush.setRepository(repositoryName);
		triggerWarnings(repositoryEntity, commit, gitPush);
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

	@SneakyThrows
	private void updatePullRequest(RepositoryApi repositoryApi, PullRequest pullRequest) {
		pullRequestBackend.updatePullRequest(repositoryApi, pullRequest);
	}

	private Stream<PullRequest> findOpenPullRequests(RepositoryEntity repositoryEntity, BranchModel branch) {
		PullRequest pullRequest = pullRequests.findOpenPullRequest(repositoryEntity, branch.getName());
		return pullRequest == null ? Stream.empty() : Stream.of(pullRequest);
	}

	@POST
	@Path("build-result")
	@RequireAuthenticatedBuildServer
	@Transactional
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
