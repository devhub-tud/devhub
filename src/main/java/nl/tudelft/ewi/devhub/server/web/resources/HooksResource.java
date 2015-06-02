package nl.tudelft.ewi.devhub.server.web.resources;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
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
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.controllers.PullRequests;
import nl.tudelft.ewi.devhub.server.database.controllers.Warnings;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.PullRequest;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CheckstyleWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.CommitWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.FindbugsWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.PMDWarning;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.SuccessiveBuildFailure;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthenticatedBuildServer;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;
import nl.tudelft.ewi.git.client.GitClientException;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.BranchModel;
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
	private final GitServerClient client;
	private final BuildResults buildResults;
	private final Groups groups;
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
	HooksResource(BuildsBackend buildBackend, GitServerClient client, BuildResults buildResults,
			Groups groups, BuildResultMailer mailer, PullRequests pullRequests, PullRequestBackend pullRequestBackend,
			Commits commits, Warnings warnings, PMDWarningGenerator pmdWarningGenerator, CheckstyleWarningGenerator checkstyleWarningGenerator,
			FindBugsWarningGenerator findBugsWarningGenerator, SuccessiveBuildFailureGenerator successiveBuildFailureGenerator,
			Set<CommitPushWarningGenerator> pushWarningGenerators) {

		this.buildBackend = buildBackend;
		this.client = client;
		this.buildResults = buildResults;
		this.groups = groups;
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
	public void onGitPush(@Context HttpServletRequest request, @Valid GitPush push) throws UnsupportedEncodingException, GitClientException {
		log.info("Received git-push event: {}", push);

		Repositories repositories = client.repositories();
		Repository repository = repositories.retrieve(push.getRepository());
		Group group = groups.findByRepoName(push.getRepository());

		Set<Commit> notBuildCommits = repository.getBranches().stream()
			.map(BranchModel::getCommit)
			.map(commitModel -> commits.ensureExists(group, commitModel.getCommit()))
			.filter(commit -> !buildResults.exists(commit))
			.collect(Collectors.toSet());

		notBuildCommits.forEach(buildBackend::buildCommit);

		repository.getBranches().stream()
			.flatMap(branchModel -> findOpenPullRequests(group, branchModel))
			.forEach(pullRequest -> updatePullRequest(repository, pullRequest));

		notBuildCommits.forEach(commit -> triggerWarnings(group, commit, push));
	}

	@POST
	@Transactional
	@RequireAuthenticatedBuildServer
	@Path("trigger-warning-generation")
	public void triggerWarnings(@QueryParam("repository") @NotEmpty String repository,
								@QueryParam("commit") @NotEmpty String commitId) {

		Group group = groups.findByRepoName(repository);
		Commit commit = commits.ensureExists(group, commitId);

		Preconditions.checkNotNull(group);
		Preconditions.checkNotNull(commit);

		GitPush gitPush = new GitPush();
		gitPush.setRepository(repository);
		triggerWarnings(group, commit, gitPush);
	}

	protected void triggerWarnings(final Group group, final Commit commit, final GitPush gitPush) {
		assert group != null;
		assert commit != null;
		assert gitPush != null;

		Set<? extends CommitWarning> pushWarnings = pushWarningGenerators.stream()
			.flatMap(generator -> {
				try {
					Set<CommitWarning> commitWarningList = generator.generateWarnings(commit, gitPush);
					return commitWarningList.stream();
				}
				catch (Exception e) {
					log.warn("Failed to generate warnings with {} for {} ", generator, commit);
					log.warn(e.getMessage(), e);
					return Stream.empty();
				}
			})
			.collect(Collectors.toSet());

		Set<? extends CommitWarning> persistedWarnings = warnings.persist(group, pushWarnings);
		log.info("Persisted {} of {} push warnings for {}", persistedWarnings.size(),
				pushWarnings.size(), group);
	}

	@SneakyThrows
	private void updatePullRequest(Repository repository, PullRequest pullRequest) {
		pullRequestBackend.updatePullRequest(repository, pullRequest);
	}

	private Stream<PullRequest> findOpenPullRequests(Group group, BranchModel branch) {
		PullRequest pullRequest = pullRequests.findOpenPullRequest(group, branch.getName());
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
		Group group = groups.findByRepoName(repoName);

		BuildResult result;
		try {
			result = buildResults.find(group, commitId);
			result.setSuccess(buildResult.getStatus() == Status.SUCCEEDED);
			result.setLog(Joiner.on('\n')
				.join(buildResult.getLogLines()));

			buildResults.merge(result);
		}
		catch (EntityNotFoundException e) {
			result = BuildResult.newBuildResult(group, commitId);
			result.setSuccess(buildResult.getStatus() == Status.SUCCEEDED);
			result.setLog(Joiner.on('\n')
				.join(buildResult.getLogLines()));

			buildResults.persist(result);
		}

		if (!result.getSuccess()) {
			mailer.sendFailedBuildResult(Lists.newArrayList(Locale.ENGLISH), result);
		}

		try {
			Commit commit = commits.retrieve(group, commitId);
			Set<SuccessiveBuildFailure> swarns = successiveBuildFailureGenerator.generateWarnings(commit, result);
			warnings.persist(group, swarns);
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
		Group group = groups.findByRepoName(repoName);
		Commit commit = commits.ensureExists(group, commitId);
		Set<PMDWarning> pmdWarnings = pmdWarningGenerator.generateWarnings(commit, report);
		Set<PMDWarning> persistedWarnings = warnings.persist(group, pmdWarnings);
		log.info("Persisted {} of {} PMD warnings for {}", persistedWarnings.size(),
				pmdWarnings.size(), group);
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
		Group group = groups.findByRepoName(repoName);
		Commit commit = commits.ensureExists(group, commitId);
		Set<CheckstyleWarning> checkstyleWarnings = checkstyleWarningGenerator.generateWarnings(commit, report);
		Set<CheckstyleWarning> persistedWarnings = warnings.persist(group, checkstyleWarnings);
		log.info("Persisted {} of {} Checkstyle warnings for {}", persistedWarnings.size(),
				checkstyleWarnings.size(), group);
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
		Group group = groups.findByRepoName(repoName);
		Commit commit = commits.ensureExists(group, commitId);
		Set<FindbugsWarning> findbugsWarnings = findBugsWarningGenerator.generateWarnings(commit, report);
		Set<FindbugsWarning> persistedWarnings = warnings.persist(group, findbugsWarnings);
		log.info("Persisted {} of {} FindBugs warnings for {}", persistedWarnings.size(),
				findbugsWarnings.size(), group);
	}

}
