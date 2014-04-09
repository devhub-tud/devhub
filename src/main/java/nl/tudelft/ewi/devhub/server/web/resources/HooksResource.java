package nl.tudelft.ewi.devhub.server.web.resources;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.build.jaxrs.models.BuildRequest;
import nl.tudelft.ewi.build.jaxrs.models.GitSource;
import nl.tudelft.ewi.build.jaxrs.models.MavenBuildInstruction;
import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.database.controllers.BuildResults;
import nl.tudelft.ewi.devhub.server.database.controllers.Groups;
import nl.tudelft.ewi.devhub.server.database.entities.BuildResult;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthentication;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.models.BranchModel;
import nl.tudelft.ewi.git.models.DetailedRepositoryModel;

import org.jboss.resteasy.plugins.guice.RequestScoped;

import com.google.inject.persist.Transactional;

@Slf4j
@Path("hooks")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class HooksResource {

	@Data
	private static class GitPush {
		private String repository;
	}

	private final BuildsBackend buildBackend;
	private final GitServerClient client;
	private final BuildResults buildResults;
	private final Groups groups;

	@Inject
	HooksResource(BuildsBackend buildBackend, GitServerClient client, BuildResults buildResults, Groups groups) {
		this.buildBackend = buildBackend;
		this.client = client;
		this.buildResults = buildResults;
		this.groups = groups;
	}

	@POST
	@Path("git-push")
	public void onGitPush(@Context HttpServletRequest request, GitPush push) {
		log.info("Received git-push event: {}", push);
		
		DetailedRepositoryModel repository = client.repositories().retrieve(push.getRepository());
		
		MavenBuildInstruction instruction = new MavenBuildInstruction();
		instruction.setWithDisplay(true);
		instruction.setPhases(new String[] { "package" });

		Group group = groups.findByRepoName(push.getRepository());
		for (BranchModel branch : repository.getBranches()) {
			if (branch.getSimpleName().equals("HEAD")) {
				continue;
			}
			if (buildResults.exists(group, branch.getCommit())) {
				continue;
			}
			
			log.info("Submitting a build for branch: {} of repository: {}", branch.getName(), repository.getName());
			
			GitSource source = new GitSource();
			source.setRepositoryUrl(repository.getUrl());
			source.setBranchName(branch.getName());
			source.setCommitId(branch.getCommit());
	
			BuildRequest buildRequest = new BuildRequest();
			buildRequest.setCallbackUrl(DevhubServer.getHostUrl(request) + "/hooks/build-result");
			buildRequest.setInstruction(instruction);
			buildRequest.setSource(source);
	
			buildBackend.offerBuild(buildRequest);
			buildResults.persist(BuildResult.newBuildResult(group, branch.getCommit()));
		}
	}

	@POST
	@Path("build-result")
	@RequireAuthentication
	@Transactional
	public void onBuildResult(nl.tudelft.ewi.build.jaxrs.models.BuildResult buildResult) {
		for (String line : buildResult.getLogLines()) {
			log.info("LOG: " + line);
		}
		log.info("STATUS: " + buildResult.getStatus());
	}
	
}
