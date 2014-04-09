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
import nl.tudelft.ewi.build.jaxrs.models.BuildResult;
import nl.tudelft.ewi.build.jaxrs.models.GitSource;
import nl.tudelft.ewi.build.jaxrs.models.MavenBuildInstruction;
import nl.tudelft.ewi.devhub.server.DevhubServer;
import nl.tudelft.ewi.devhub.server.backend.BuildsBackend;
import nl.tudelft.ewi.devhub.server.web.filters.RequireAuthentication;

import org.jboss.resteasy.plugins.guice.RequestScoped;

@Slf4j
@Path("hooks")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class HooksResource {

	@Data
	private static class GitPush {
		private String repositoryUrl;
		private String branch;
		private String commitId;
	}

	private final BuildsBackend buildBackend;

	@Inject
	HooksResource(BuildsBackend buildBackend) {
		this.buildBackend = buildBackend;
	}

	@POST
	@Path("git-push")
	public void onGitPush(@Context HttpServletRequest request, GitPush push) {
		log.info("Received git-push event: {}", push);
		
		MavenBuildInstruction instruction = new MavenBuildInstruction();
		instruction.setWithDisplay(true);
		instruction.setPhases(new String[] { "package" });

		GitSource source = new GitSource();
		source.setRepositoryUrl(push.getRepositoryUrl());
		source.setBranchName(push.getBranch());
		source.setCommitId(push.getCommitId());

		BuildRequest buildRequest = new BuildRequest();
		buildRequest.setCallbackUrl(DevhubServer.getHostUrl(request) + "/hooks/build-result");
		buildRequest.setInstruction(instruction);
		buildRequest.setSource(source);

		buildBackend.offerBuild(buildRequest);
	}

	@POST
	@Path("build-result")
	@RequireAuthentication
	public void onBuildResult(BuildResult buildResult) {
		log.info("STATUS: " + buildResult.getStatus());
		for (String line : buildResult.getLogLines()) {
			log.info("LOG: " + line);
		}
	}
	
}
